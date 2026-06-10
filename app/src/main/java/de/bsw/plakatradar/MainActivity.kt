package de.bsw.plakatradar

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import de.bsw.plakatradar.core.*
import de.bsw.plakatradar.data.LocalRepository
import de.bsw.plakatradar.sync.NearbySyncManager
import de.bsw.plakatradar.sync.SyncBundleCodec
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // enableScreenshotProtection() // Vorläufig deaktiviert, Funktion bleibt erhalten.
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { PlakatRadarApp() } }
    }

    private fun enableScreenshotProtection() {
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) setRecentsScreenshotEnabled(false)
    }
}

data class AppUiState(
    val local: LocalTeamState,
    val syncActive: Boolean = false,
    val lastLog: String = "Bereit.",
    val error: String? = null,
    val pendingPhotoFileName: String? = null
)

class PlakatRadarViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = LocalRepository(app)
    private val codec = SyncBundleCodec(app, repo)
    private var sync: NearbySyncManager? = null

    var ui by mutableStateOf(AppUiState(repo.load()))
        private set

    fun createLeaderTeam(teamName: String, leaderName: String) {
        runCatching { repo.createLeaderTeam(teamName, leaderName) }
            .onSuccess { ui = ui.copy(local = it, lastLog = "Team erstellt. Du bist Teamleiter.") }
            .onFailure { fail(it) }
    }

    fun joinByQr(raw: String, memberName: String) {
        runCatching { repo.joinByInvite(TeamInvite.decode(raw), memberName) }
            .onSuccess { ui = ui.copy(local = it, lastLog = "Beigetreten. Du kannst jetzt Plakate sehen und erfassen.") }
            .onFailure { fail(it) }
    }

    fun enterWithoutQr(memberName: String) {
        val name = memberName.ifBlank { "Teammitglied" }
        val current = ui.local
        val member = DeviceRecord(current.deviceId, name, MemberRole.MEMBER, approved = true)
        val updated = current.copy(
            deviceName = name,
            role = MemberRole.MEMBER,
            teamId = "offline-${current.deviceId}",
            teamName = "Ohne Team-QR",
            teamSecret = null,
            devices = listOf(member)
        )
        repo.save(updated)
        ui = ui.copy(local = updated, lastLog = "Ohne Teamleiter-QR gestartet. Stadtverwaltungs-Export ist möglich, Sync braucht Team-QR.")
    }

    fun requireTeamQr() { ui = ui.copy(error = "Bitte Teamleiter-QR-Code scannen.") }

    fun inviteText(locked: Boolean = false): String? {
        val s = ui.local
        if (!AccessPolicy.canShowQr(s)) return null
        val expiresAt = if (locked) Instant.now().plus(3650, ChronoUnit.DAYS).toEpochMilli() else Instant.now().plusSeconds(TeamInvite.DEFAULT_TTL_SECONDS).toEpochMilli()
        return TeamInvite(
            teamId = s.teamId ?: return null,
            teamName = s.teamName ?: "Plakat-Team",
            leaderName = s.deviceName,
            leaderDeviceId = s.deviceId,
            teamSecret = s.teamSecret ?: return null,
            expiresAt = expiresAt
        ).encodeForQr()
    }

    fun startOrStopSync() {
        if (ui.syncActive) {
            sync?.stop()
            ui = ui.copy(syncActive = false, lastLog = "Lokaler Sync aus.")
            return
        }
        if (!AccessPolicy.canSync(ui.local)) {
            requireTeamQr()
            return
        }
        sync = NearbySyncManager(
            context = getApplication(),
            repo = repo,
            bundleCodec = codec,
            onLog = { ui = ui.copy(lastLog = it) },
            onIncomingBundle = { file -> importBundle(file) }
        )
        sync?.start(ui.local)
        ui = ui.copy(syncActive = true, lastLog = "Lokaler Sync an. Geräte in der Nähe werden gesucht.")
    }

    private fun importBundle(file: File) {
        runCatching { repo.mergeAndSave(ui.local, codec.importVerifiedBundle(file, ui.local)) }
            .onSuccess {
                ui = ui.copy(local = it, lastLog = "Daten mit Teamgerät abgeglichen.")
                sync?.sendCurrentBundleToAll()
            }
            .onFailure { fail(it) }
    }

    private fun fileProviderAuthority(): String = getApplication<Application>().packageName + ".fileprovider"

    fun preparePhotoFile(): Uri {
        val fileName = "poster_${UUID.randomUUID()}.jpg"
        val file = File(repo.photosDir, fileName)
        ui = ui.copy(pendingPhotoFileName = fileName)
        return FileProvider.getUriForFile(getApplication(), fileProviderAuthority(), file)
    }

    fun addPoster(lat: Double, lng: Double, address: String, type: PosterType, officialNote: String, internalNote: String, removalDays: Long) {
        val s = ui.local
        val teamId = s.teamId ?: return
        if (!AccessPolicy.canAddPoster(s)) {
            requireTeamQr()
            return
        }
        val plannedRemovalAt = Instant.now().plus(removalDays.coerceIn(1, 120), ChronoUnit.DAYS).toEpochMilli()
        val poster = Poster(
            teamId = teamId,
            latitude = lat,
            longitude = lng,
            addressHint = address,
            type = type,
            status = PosterStatus.HANGING,
            localPhotoFileName = ui.pendingPhotoFileName,
            createdByDeviceId = s.deviceId,
            createdByName = s.deviceName,
            plannedRemovalAt = plannedRemovalAt,
            officialNote = officialNote,
            internalNote = internalNote
        )
        val updated = repo.addPoster(s, poster)
        ui = ui.copy(local = updated, pendingPhotoFileName = null, lastLog = "Plakat gespeichert.")
        sync?.sendCurrentBundleToAll()
    }

    fun updateStatus(poster: Poster, status: PosterStatus) {
        val updated = repo.updateStatus(ui.local, poster, status)
        ui = ui.copy(local = updated, lastLog = "Status geändert.")
        sync?.sendCurrentBundleToAll()
    }

    fun deletePoster(poster: Poster) {
        val updated = repo.deletePoster(ui.local, poster)
        ui = ui.copy(local = updated, lastLog = "Plakat aus der Liste entfernt.")
        sync?.sendCurrentBundleToAll()
    }

    fun exportCsv(context: Context, municipality: String) {
        runCatching {
            val file = File(context.cacheDir, "Plakatliste_${municipality}_${System.currentTimeMillis()}.csv")
            file.writeText(OfficialExport.toCsv(ui.local, municipality), Charsets.UTF_8)
            val uri = FileProvider.getUriForFile(context, fileProviderAuthority(), file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, "Plakatliste teilen"))
        }.onFailure { fail(it) }
    }

    fun shareSyncBundle(context: Context) {
        runCatching {
            if (!AccessPolicy.canShareSyncBundle(ui.local)) error("Bitte Teamleiter-QR-Code scannen.")
            val file = codec.createBundle(repo.toSnapshot(ui.local), ui.local.teamSecret ?: error("Kein Team-Schlüssel."))
            val uri = FileProvider.getUriForFile(context, fileProviderAuthority(), file)
            val send = Intent(Intent.ACTION_SEND).apply {
                type = "application/octet-stream"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "PlakatRadar Sync-Paket")
                putExtra(Intent.EXTRA_TEXT, "Verschlüsseltes PlakatRadar Sync-Paket für ${ui.local.teamName ?: "das Team"}. Bitte in PlakatRadar importieren.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(send, "Sync-Paket teilen"))
            ui = ui.copy(lastLog = "Verschlüsseltes Sync-Paket wurde zum Teilen vorbereitet.")
        }.onFailure { fail(it) }
    }

    fun importSharedSyncBundle(uri: Uri) {
        runCatching {
            if (!AccessPolicy.canSync(ui.local)) error("Bitte Teamleiter-QR-Code scannen.")
            val file = codec.copyIncomingUriToBundle(uri)
            repo.mergeAndSave(ui.local, codec.importVerifiedBundle(file, ui.local))
        }.onSuccess {
            ui = ui.copy(local = it, lastLog = "Sync-Paket importiert. Daten wurden zusammengeführt.")
            sync?.sendCurrentBundleToAll()
        }.onFailure { fail(it) }
    }

    fun showGoogleServiceNotImplemented() {
        ui = ui.copy(
            lastLog = "Google-Service-Sync ist vorgesehen, aber noch nicht implementiert.",
            error = "Google-Service-Sync ist vorgesehen, aber noch nicht implementiert. Aktuell bitte lokalen Sync oder verschlüsselte Messenger-Sync-Pakete nutzen."
        )
    }

    fun clearError() { ui = ui.copy(error = null) }
    private fun fail(t: Throwable) { ui = ui.copy(error = t.message ?: t.toString()) }
}

@Composable
fun PlakatRadarApp(vm: PlakatRadarViewModel = viewModel()) {
    val s = vm.ui
    Surface(Modifier.fillMaxSize()) {
        if (s.local.role == null) StartScreen(vm) else DashboardScreen(vm)
    }
    s.error?.let {
        AlertDialog(
            onDismissRequest = vm::clearError,
            confirmButton = { Button(onClick = vm::clearError) { Text("OK") } },
            title = { Text("Hinweis") },
            text = { Text(it) }
        )
    }
}

@Composable
fun StartScreen(vm: PlakatRadarViewModel) {
    var mode by remember { mutableStateOf<String?>(null) }
    var showQrChoice by remember { mutableStateOf(false) }
    var teamName by remember { mutableStateOf("BSW Nordsachsen Plakatierung") }
    var myName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val closeKeyboard: () -> Unit = { focusManager.clearFocus(force = true) }
    val scanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { vm.joinByQr(it, myName.ifBlank { "Teammitglied" }) }
    }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp).padding(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("PlakatRadar", style = MaterialTheme.typography.headlineLarge)
            Text("Bitte auswählen. Es gibt nur zwei Wege:")
            Button(onClick = { mode = "leader" }, modifier = Modifier.fillMaxWidth().height(72.dp)) { Text("Ich bin Teamleiter") }
            Button(onClick = { mode = "member" }, modifier = Modifier.fillMaxWidth().height(72.dp)) { Text("Ich bin Teammitglied") }
            Divider()
            when (mode) {
                "leader" -> {
                    Text("Teamleiter erstellt das Team und zeigt später den QR-Code.")
                    OneLineField(teamName, { teamName = it }, "Teamname", closeKeyboard)
                    OneLineField(myName, { myName = it }, "Dein Name", closeKeyboard)
                    Button(onClick = { closeKeyboard(); vm.createLeaderTeam(teamName, myName.ifBlank { "Teamleiter" }) }, modifier = Modifier.fillMaxWidth().height(64.dp)) { Text("Team erstellen") }
                }
                "member" -> {
                    Text("Bitte zuerst deinen Namen eintragen.")
                    OneLineField(myName, { myName = it }, "Dein Name", closeKeyboard)
                    Button(onClick = { closeKeyboard(); showQrChoice = true }, modifier = Modifier.fillMaxWidth().height(64.dp)) { Text("Weiter") }
                }
            }
        }
        Button(onClick = { openAppSettings(context) }, modifier = Modifier.align(Alignment.BottomStart).padding(24.dp).height(56.dp)) { Text("Deinstallieren") }
    }

    if (showQrChoice) AlertDialog(
        onDismissRequest = { showQrChoice = false },
        title = { Text("Teamleiter-QR-Code scannen?") },
        text = { Text("Möchtest du jetzt den QR-Code vom Teamleiter scannen? Ohne QR kannst du die App öffnen. Sync braucht später den Teamleiter-QR.") },
        confirmButton = { Button(onClick = { showQrChoice = false; scanner.launch(ScanOptions().setPrompt("QR-Code vom Teamleiter scannen").setBeepEnabled(false)) }) { Text("Ja, scannen") } },
        dismissButton = { TextButton(onClick = { showQrChoice = false; vm.enterWithoutQr(myName.ifBlank { "Teammitglied" }) }) { Text("Nein, ohne QR") } }
    )
}

@Composable
fun OneLineField(value: String, onChange: (String) -> Unit, label: String, closeKeyboard: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { closeKeyboard() })
    )
}

@Composable
fun DashboardScreen(vm: PlakatRadarViewModel) {
    var tab by remember { mutableStateOf("home") }
    var showPermissionPopup by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        Toast.makeText(
            context,
            if (grants.values.all { it }) "Berechtigungen sind aktiv." else "Einige Berechtigungen fehlen noch. Manche Funktionen können eingeschränkt sein.",
            Toast.LENGTH_LONG
        ).show()
        showPermissionPopup = false
    }
    val missingPermissions = remember(showPermissionPopup) { missingAppPermissions(context) }
    if (showPermissionPopup && missingPermissions.isNotEmpty()) {
        PermissionStartupDialog(
            onAllow = { permissionLauncher.launch(missingPermissions) },
            onLater = { showPermissionPopup = false }
        )
    }

    Scaffold(bottomBar = { ModernBottomNav(tab) { tab = it } }) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            when (tab) {
                "home" -> HomeScreen(vm) { tab = it }
                "add" -> AddPosterScreen(vm)
                "list" -> PosterListScreen(vm)
                "map" -> PosterMapScreen(vm.ui.local.posters)
                "more" -> MoreScreen(vm) { tab = it }
                "near" -> NearbyPostersScreen(vm)
            }
        }
    }
}

@Composable
fun ModernBottomNav(tab: String, onTab: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(selected = tab == "home", onClick = { onTab("home") }, icon = { Text("⌂") }, label = { Text("Start") })
        NavigationBarItem(selected = tab == "add", onClick = { onTab("add") }, icon = { Text("+") }, label = { Text("Erfassen") })
        NavigationBarItem(selected = tab == "list", onClick = { onTab("list") }, icon = { Text("≡") }, label = { Text("Liste") })
        NavigationBarItem(selected = tab == "map", onClick = { onTab("map") }, icon = { Text("⌖") }, label = { Text("Karte") })
        NavigationBarItem(selected = tab == "more", onClick = { onTab("more") }, icon = { Text("…") }, label = { Text("Mehr") })
    }
}

@Composable
fun PermissionStartupDialog(onAllow: () -> Unit, onLater: () -> Unit) {
    AlertDialog(
        onDismissRequest = onLater,
        title = { Text("Berechtigungen für alle Funktionen") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Damit PlakatRadar richtig funktioniert, braucht die App einige Berechtigungen:")
                Text("• Kamera: QR-Code scannen und Plakatfoto aufnehmen")
                Text("• Standort: GPS-Punkt des Plakats speichern und Plakate in deiner Nähe anzeigen")
                Text("• Bluetooth/WLAN in der Nähe: lokaler Team-Sync ohne Cloud")
                Text("Du kannst die App auch ohne alles nutzen, aber dann funktionieren manche Dinge nur eingeschränkt.")
            }
        },
        confirmButton = { Button(onClick = onAllow) { Text("Berechtigungen erlauben") } },
        dismissButton = { TextButton(onClick = onLater) { Text("Später") } }
    )
}

@Composable
fun HomeScreen(vm: PlakatRadarViewModel, onNavigate: (String) -> Unit) {
    val s = vm.ui.local
    val hasTeamQr = AccessPolicy.hasTeamAccess(s)
    val qrScanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { vm.joinByQr(it, s.deviceName.ifBlank { "Teammitglied" }) }
    }
    val activePosters = s.posters.count { it.status != PosterStatus.REMOVED }
    val checkedPosters = s.posters.count { it.status == PosterStatus.CHECKED }

    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("PlakatRadar", style = MaterialTheme.typography.headlineMedium)
                    Text(s.teamName ?: "Ohne Team-QR")
                    Text(if (s.role == MemberRole.LEADER) "Teamleiter-Modus" else "Teammitglied-Modus")
                    Text("Aktive Plakate: $activePosters · Kontrolliert: $checkedPosters")
                    Text("Letzte Meldung: ${vm.ui.lastLog}")
                }
            }
        }
        if (!hasTeamQr) item { NoQrCard { qrScanner.launch(ScanOptions().setPrompt("QR-Code vom Teamleiter scannen").setBeepEnabled(false)) } }
        item { ModernActionCard("Plakat erfassen", "Foto aufnehmen, GPS speichern und Notizen ergänzen.", "Jetzt erfassen") { onNavigate("add") } }
        item { ModernActionCard("Plakate ansehen", "Liste mit Status, Foto-Vorschau und Weg dorthin.", "Liste öffnen") { onNavigate("list") } }
        item { ModernActionCard("Karte öffnen", "Alle Plakate auf der Karte sehen.", "Zur Karte") { onNavigate("map") } }
        item { RemovalReminderCard(s) }
        item { ModernActionCard("Teilen / Exportieren", "Stadtverwaltung, Sync-Paket und Import gebündelt an einer Stelle.", "Menü öffnen") { onNavigate("more") } }
    }
}

@Composable
fun MoreScreen(vm: PlakatRadarViewModel, onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    val s = vm.ui.local
    val hasTeamQr = AccessPolicy.hasTeamAccess(s)
    var showShareMenu by remember { mutableStateOf(false) }
    val permissions = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}
    val syncImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) vm.importSharedSyncBundle(uri) }
    val qrScanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        result.contents?.let { vm.joinByQr(it, s.deviceName.ifBlank { "Teammitglied" }) }
    }

    LazyColumn(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { Text("Mehr", style = MaterialTheme.typography.headlineMedium); Text("Team, Teilen und Einstellungen") }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Teilen / Exportieren", style = MaterialTheme.typography.titleLarge)
                    Text("Alle Datei-Funktionen liegen jetzt an einer Stelle.")
                    Button(onClick = { showShareMenu = true }, modifier = Modifier.fillMaxWidth().height(58.dp)) { Text("Teilen / Exportieren öffnen") }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Team & Sync", style = MaterialTheme.typography.titleLarge)
                    if (hasTeamQr) {
                        Button(onClick = vm::startOrStopSync, modifier = Modifier.fillMaxWidth().height(58.dp)) { Text(if (vm.ui.syncActive) "Lokalen Sync stoppen" else "Lokalen Sync starten") }
                        Text("Lokaler Sync funktioniert mit Teamgeräten in der Nähe.")
                    } else {
                        LockedTeamButton("Lokalen Sync starten", vm)
                        Button(onClick = { qrScanner.launch(ScanOptions().setPrompt("QR-Code vom Teamleiter scannen").setBeepEnabled(false)) }, modifier = Modifier.fillMaxWidth().height(58.dp)) { Text("Teamleiter-QR scannen") }
                    }
                }
            }
        }
        item { if (hasTeamQr) GoogleServicePlaceholderCard(vm) else LockedTeamButton("Google-Service nutzen", vm) }
        if (AccessPolicy.canShowQr(s)) {
            item { TeamInviteQrCard(vm) }
            item { TeamMembersCard(s) }
        }
        item { ModernActionCard("Plakate in meiner Nähe", "Sortiert nach Entfernung zum aktuellen Standort.", "Nähe öffnen") { onNavigate("near") } }
        item {
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Berechtigungen", style = MaterialTheme.typography.titleLarge)
                    Text("Kamera, Standort und Geräte in der Nähe aktivieren.")
                    Button(onClick = { permissions.launch(nearbyAndAppPermissions()) }, modifier = Modifier.fillMaxWidth().height(58.dp)) { Text("Berechtigungen prüfen") }
                }
            }
        }
        item { AppManagementCard(context) }
    }

    if (showShareMenu) {
        ShareExportDialog(
            hasTeamQr = hasTeamQr,
            canExportAuthority = AccessPolicy.canExportForAuthority(s),
            onDismiss = { showShareMenu = false },
            onAuthorityExport = { showShareMenu = false; vm.exportCsv(context, "Eilenburg") },
            onShareSync = { showShareMenu = false; vm.shareSyncBundle(context) },
            onImportSync = { showShareMenu = false; syncImportLauncher.launch(arrayOf("application/octet-stream", "application/zip", "*/*")) }
        )
    }
}

@Composable
fun ModernActionCard(title: String, subtitle: String, button: String, enabled: Boolean = true, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(subtitle)
            Button(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text(button) }
        }
    }
}

@Composable
fun ShareExportDialog(
    hasTeamQr: Boolean,
    canExportAuthority: Boolean,
    onDismiss: () -> Unit,
    onAuthorityExport: () -> Unit,
    onShareSync: () -> Unit,
    onImportSync: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Teilen / Exportieren") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Alle Datei-Funktionen sind gebündelt.")
                Button(onClick = onAuthorityExport, enabled = canExportAuthority, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Stadtverwaltungs-Export teilen") }
                Button(onClick = onShareSync, enabled = hasTeamQr, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Sync-Paket teilen") }
                Button(onClick = onImportSync, enabled = hasTeamQr, modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Sync-Paket importieren") }
                if (!hasTeamQr) Text("Sync-Pakete brauchen den Teamleiter-QR. Der Stadtverwaltungs-Export geht auch ohne QR-Code.")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Schließen") } }
    )
}

@Composable
fun NoQrCard(onScan: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Ohne Teamleiter-QR", style = MaterialTheme.typography.titleMedium)
            Text("Für Sync bitte Teamleiter-QR scannen. Der Stadtverwaltungs-Export bleibt möglich.")
            Button(onClick = onScan, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text("Teamleiter-QR-Code scannen") }
        }
    }
}

@Composable
fun LockedTeamButton(label: String, vm: PlakatRadarViewModel) {
    Button(
        onClick = vm::requireTeamQr,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
    ) { Text(label) }
}

@Composable
fun AppManagementCard(context: Context) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("App verwalten", style = MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { openAppSettings(context) }, modifier = Modifier.weight(1f).height(60.dp)) { Text("Deinstallieren") }
                Button(onClick = { openUpdatePage(context) }, modifier = Modifier.weight(1f).height(60.dp)) { Text("Update") }
            }
        }
    }
}

@Composable
fun GoogleServicePlaceholderCard(vm: PlakatRadarViewModel) {
    var checked by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Google-Service-Sync", style = MaterialTheme.typography.titleMedium)
            Text("Vorgesehen für eine spätere Online-Teilen-Funktion. Aktuell noch nicht aktiv.")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Google-Service nutzen")
                Switch(checked = checked, onCheckedChange = { wantsOn -> if (wantsOn) { checked = false; vm.showGoogleServiceNotImplemented() } })
            }
            Text("Der Schalter geht wieder aus, bis der Dienst implementiert ist.")
        }
    }
}

@Composable
fun TeamInviteQrCard(vm: PlakatRadarViewModel) {
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("plakatradar_qr_lock", Context.MODE_PRIVATE) }
    val teamKey = "${vm.ui.local.teamId ?: ""}|${vm.ui.local.teamSecret ?: ""}|${vm.ui.local.deviceName}"
    val savedTeamKey = prefs.getString("team_key", null)
    val savedQr = if (savedTeamKey == teamKey) prefs.getString("qr_text", null) else null
    var locked by remember(teamKey) { mutableStateOf(savedTeamKey == teamKey && prefs.getBoolean("locked", false)) }
    var refreshSeed by remember(teamKey) { mutableStateOf(0) }
    var remaining by remember(teamKey) { mutableStateOf(TeamInvite.DEFAULT_TTL_SECONDS.toInt()) }
    var qrText by remember(teamKey) { mutableStateOf(savedQr ?: vm.inviteText()) }

    LaunchedEffect(locked, refreshSeed, teamKey) {
        if (locked) {
            if (savedQr == null) qrText = vm.inviteText(true)
            prefs.edit().putString("team_key", teamKey).putBoolean("locked", true).putString("qr_text", qrText).apply()
        } else {
            qrText = vm.inviteText()
            prefs.edit().putString("team_key", teamKey).putBoolean("locked", false).remove("qr_text").apply()
            remaining = TeamInvite.DEFAULT_TTL_SECONDS.toInt()
            while (remaining > 0 && !locked) {
                kotlinx.coroutines.delay(1000)
                remaining -= 1
            }
            if (!locked) refreshSeed += 1
        }
    }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Team-QR-Code", style = MaterialTheme.typography.titleMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(if (locked) "🔒 Gespeicherter QR bleibt aktiv" else "🔓 Neuer QR in ${remaining}s")
                Switch(
                    checked = locked,
                    onCheckedChange = { isLocked ->
                        locked = isLocked
                        if (isLocked) {
                            val lockedQr = vm.inviteText(true)
                            qrText = lockedQr
                            prefs.edit().putString("team_key", teamKey).putBoolean("locked", true).putString("qr_text", lockedQr).apply()
                        } else {
                            prefs.edit().putString("team_key", teamKey).putBoolean("locked", false).remove("qr_text").apply()
                            refreshSeed += 1
                        }
                    }
                )
            }
            Text(if (locked) "Schloss aktiv: Dieser QR bleibt erhalten." else "Ohne Schloss wird die Anzeige nach 1 Minute erneuert.")
            qrText?.let { QrCodeImage(it) }
        }
    }
}

@Composable
fun TeamMembersCard(s: LocalTeamState) {
    val members = s.devices.filter { it.role == MemberRole.MEMBER && !it.blocked }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Teammitglieder", style = MaterialTheme.typography.titleMedium)
            if (members.isEmpty()) Text("Noch keine Teammitglieder synchronisiert.") else members.forEach { Text(it.displayName) }
        }
    }
}

@Composable
fun RemovalReminderCard(s: LocalTeamState) {
    val now = System.currentTimeMillis()
    val active = s.posters.filter { it.status != PosterStatus.REMOVED }
    val due = active.filter { it.plannedRemovalAt != null && it.plannedRemovalAt <= now }
    val soon = active.filter { it.plannedRemovalAt != null && it.plannedRemovalAt in (now + 1)..(now + 3L * 24L * 60L * 60L * 1000L) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Abnahme-Erinnerung", style = MaterialTheme.typography.titleMedium)
            Text("Noch nicht entfernt: ${active.size}")
            if (due.isNotEmpty()) Text("Überfällig: ${due.size} Plakate")
            if (soon.isNotEmpty()) Text("Bald fällig: ${soon.size} Plakate in den nächsten 3 Tagen")
            if (due.isEmpty() && soon.isEmpty()) Text("Aktuell keine dringende Abnahme offen.")
        }
    }
}

@Composable
fun AddPosterScreen(vm: PlakatRadarViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val closeKeyboard: () -> Unit = { focusManager.clearFocus(force = true) }
    var note by remember { mutableStateOf("") }
    var internal by remember { mutableStateOf("") }
    var removalDaysText by remember { mutableStateOf("14") }
    var type by remember { mutableStateOf(PosterType.LAMP_POST) }
    var photoTaken by remember { mutableStateOf(false) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> photoTaken = ok }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Plakat hinzufügen", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { camera.launch(vm.preparePhotoFile()) }, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text(if (photoTaken) "Foto neu aufnehmen" else "Foto aufnehmen") }
        Text("Standort wird automatisch per GPS ermittelt und als Straße gespeichert.")
        MultilineField(note, { note = it }, "Bemerkung für Stadtverwaltung", closeKeyboard)
        MultilineField(internal, { internal = it }, "Interne Notiz", closeKeyboard)
        OneLineField(removalDaysText, { removalDaysText = it.filter { ch -> ch.isDigit() }.take(3) }, "Abnahme in Tagen", closeKeyboard)
        PosterTypeDropdown(type, { type = it })
        Button(
            onClick = {
                closeKeyboard()
                val fused = LocationServices.getFusedLocationProviderClient(context)
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fused.lastLocation.addOnSuccessListener { loc ->
                        if (loc != null) {
                            vm.addPoster(
                                loc.latitude,
                                loc.longitude,
                                reverseGeocodeAddress(context, loc.latitude, loc.longitude),
                                type,
                                note,
                                internal,
                                removalDaysText.toLongOrNull() ?: 14L
                            )
                        } else {
                            Toast.makeText(context, "Standort konnte nicht ermittelt werden. Bitte kurz nach draußen gehen oder GPS aktivieren.", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Bitte zuerst die Standort-Berechtigung erlauben.", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(64.dp)
        ) { Text("Speichern") }
    }
}

@Composable
fun MultilineField(value: String, onChange: (String) -> Unit, label: String, closeKeyboard: () -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        minLines = 2,
        maxLines = 4,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { closeKeyboard() })
    )
}

@Composable
fun PosterTypeDropdown(value: PosterType, onChange: (PosterType) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Button(onClick = { open = true }) { Text("Art: ${posterTypeText(value)}") }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            PosterType.values().forEach { item ->
                DropdownMenuItem(text = { Text(posterTypeText(item)) }, onClick = { onChange(item); open = false })
            }
        }
    }
}

@Composable
fun PosterListScreen(vm: PlakatRadarViewModel) {
    val s = vm.ui.local
    val context = LocalContext.current
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(s.posters) { p ->
            val photoFile = p.localPhotoFileName?.let { File(context.filesDir, "photos/$it") }
            val photoBitmap = remember(p.localPhotoFileName) {
                photoFile?.takeIf { it.isFile }?.let { BitmapFactory.decodeFile(it.absolutePath) }
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(p.addressHint.ifBlank { "Standort ohne Text" }, style = MaterialTheme.typography.titleMedium)
                    Text("Status: ${statusText(p.status)} · von ${p.createdByName}")
                    Text("GPS: ${p.latitude}, ${p.longitude}")
                    Text("Abnahme bis: ${formatDate(p.plannedRemovalAt)}")
                    if (photoBitmap != null) {
                        Image(bitmap = photoBitmap.asImageBitmap(), contentDescription = "Plakatfoto", modifier = Modifier.fillMaxWidth().height(180.dp))
                        Text("Foto: ${p.localPhotoFileName}")
                    } else {
                        Text("Kein Foto zu diesem Eintrag gespeichert.")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { vm.updateStatus(p, PosterStatus.CHECKED) }) { Text("OK") }
                        Button(onClick = { vm.updateStatus(p, PosterStatus.DAMAGED) }) { Text("Kaputt") }
                        Button(onClick = { vm.updateStatus(p, PosterStatus.REMOVED) }) { Text("Entfernt") }
                    }
                    Button(onClick = { vm.deletePoster(p) }, modifier = Modifier.fillMaxWidth()) { Text("Aus Liste entfernen") }
                    Button(onClick = { openNavigation(context, p.latitude, p.longitude, p.addressHint.ifBlank { "Plakat" }) }) { Text("Weg dorthin") }
                }
            }
        }
    }
}

@Composable
fun NearbyPostersScreen(vm: PlakatRadarViewModel) {
    val context = LocalContext.current
    var currentLat by remember { mutableStateOf<Double?>(null) }
    var currentLng by remember { mutableStateOf<Double?>(null) }
    val sorted = remember(currentLat, currentLng, vm.ui.local.posters) {
        val lat = currentLat
        val lng = currentLng
        if (lat == null || lng == null) emptyList() else vm.ui.local.posters
            .filter { it.status != PosterStatus.REMOVED }
            .map { it to distanceMeters(lat, lng, it.latitude, it.longitude) }
            .sortedBy { it.second }
    }
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Plakate in meiner Nähe", style = MaterialTheme.typography.headlineMedium)
        Button(
            onClick = {
                val fused = LocationServices.getFusedLocationProviderClient(context)
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fused.lastLocation.addOnSuccessListener { loc ->
                        if (loc != null) {
                            currentLat = loc.latitude
                            currentLng = loc.longitude
                        } else {
                            Toast.makeText(context, "Standort konnte nicht ermittelt werden.", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Bitte zuerst Standort-Berechtigung erlauben.", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) { Text("Meinen Standort aktualisieren") }
        if (currentLat == null || currentLng == null) {
            Text("Tippe auf den Button, dann zeigt die App die nächstgelegenen Plakate.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(sorted) { item ->
                    val p = item.first
                    val meters = item.second
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${meters.roundToInt()} m · ${p.addressHint.ifBlank { "Standort ohne Text" }}", style = MaterialTheme.typography.titleMedium)
                            Text("Status: ${statusText(p.status)}")
                            Button(onClick = { openNavigation(context, p.latitude, p.longitude, p.addressHint.ifBlank { "Plakat" }) }) { Text("Weg dorthin") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PosterMapScreen(posters: List<Poster>) {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            Configuration.getInstance().userAgentValue = context.packageName
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(14.0)
                controller.setCenter(GeoPoint(posters.firstOrNull()?.latitude ?: 51.4592, posters.firstOrNull()?.longitude ?: 12.6331))
            }
        },
        update = { map ->
            map.overlays.clear()
            posters.forEach { p ->
                Marker(map).apply {
                    position = GeoPoint(p.latitude, p.longitude)
                    title = p.addressHint.ifBlank { statusText(p.status) }
                    snippet = "${statusText(p.status)} · ${p.createdByName} · Tippen: Weg dorthin"
                    icon = statusMarkerDrawable(context, p.status)
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { marker, _ ->
                        marker.showInfoWindow()
                        openNavigation(context, p.latitude, p.longitude, p.addressHint.ifBlank { "Plakat ${statusText(p.status)}" })
                        true
                    }
                    map.overlays.add(this)
                }
            }
            map.invalidate()
        }
    )
}

fun statusMarkerDrawable(context: Context, status: PosterStatus): Drawable = GradientDrawable().apply {
    shape = GradientDrawable.OVAL
    setColor(statusColor(status))
    setStroke(4, Color.WHITE)
    setSize(44, 44)
}
fun statusColor(status: PosterStatus): Int = when (status) {
    PosterStatus.HANGING -> Color.rgb(46, 160, 67)
    PosterStatus.CHECKED -> Color.rgb(31, 111, 235)
    PosterStatus.DAMAGED, PosterStatus.MISSING -> Color.rgb(218, 54, 51)
    PosterStatus.REPLACED -> Color.rgb(227, 179, 65)
    PosterStatus.REMOVED -> Color.rgb(120, 124, 130)
}
fun statusText(status: PosterStatus): String = when (status) {
    PosterStatus.HANGING -> "hängt"
    PosterStatus.CHECKED -> "kontrolliert"
    PosterStatus.DAMAGED -> "beschädigt"
    PosterStatus.MISSING -> "fehlt"
    PosterStatus.REPLACED -> "ersetzt"
    PosterStatus.REMOVED -> "entfernt"
}
fun posterTypeText(type: PosterType): String = when (type) {
    PosterType.LAMP_POST -> "Laternenmast"
    PosterType.FENCE -> "Zaun"
    PosterType.BANNER -> "Banner"
    PosterType.TRIANGLE_STAND -> "Dreieckständer"
    PosterType.LARGE_FORMAT -> "Großformat / Großfläche"
    PosterType.OTHER -> "Sonstiges"
}
fun reverseGeocodeAddress(context: Context, latitude: Double, longitude: Double): String {
    val fallback = "GPS: %.6f, %.6f".format(Locale.US, latitude, longitude)
    return runCatching {
        @Suppress("DEPRECATION")
        val found = Geocoder(context, Locale.GERMANY).getFromLocation(latitude, longitude, 1)
        val address = found?.firstOrNull()
        val street = address?.thoroughfare?.takeIf { it.isNotBlank() }?.let { streetName ->
            val number = address.subThoroughfare?.takeIf { it.isNotBlank() }
            if (number != null) "$streetName $number" else streetName
        }
        val city = address?.locality?.takeIf { it.isNotBlank() } ?: address?.subAdminArea?.takeIf { it.isNotBlank() }
        listOfNotNull(street, address?.postalCode?.takeIf { it.isNotBlank() }, city).joinToString(", ").ifBlank { address?.getAddressLine(0).orEmpty() }
    }.getOrNull()?.takeIf { it.isNotBlank() } ?: fallback
}
fun openAppSettings(context: Context) {
    val uri = Uri.parse("package:${context.packageName}")
    val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS", uri)
    runCatching { context.startActivity(intent) }.onFailure { Toast.makeText(context, "App-Einstellungen konnten nicht geöffnet werden.", Toast.LENGTH_LONG).show() }
}
fun openUpdatePage(context: Context) {
    val url = "https://github.com/privatdavidgottschall-sudo/Plakat-Radar/actions/workflows/android-debug-apk.yml"
    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }.onFailure { Toast.makeText(context, "Update-Seite konnte nicht geöffnet werden.", Toast.LENGTH_LONG).show() }
}
fun openNavigation(context: Context, latitude: Double, longitude: Double, label: String) {
    val encodedLabel = Uri.encode(label)
    val geoUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
    val geoIntent = Intent(Intent.ACTION_VIEW, geoUri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    runCatching { context.startActivity(geoIntent) }
        .recoverCatching {
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
        }
        .onFailure { Toast.makeText(context, "Keine Karten-App gefunden.", Toast.LENGTH_LONG).show() }
}
fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val earthRadius = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a = sin(dLat / 2).pow(2.0) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2.0)
    return earthRadius * 2 * atan2(sqrt(a), sqrt(1 - a))
}
fun formatDate(value: Long?): String {
    if (value == null) return "nicht gesetzt"
    return SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY).format(Date(value))
}

@Composable
fun QrCodeImage(text: String) {
    val bitmap = remember(text) { qrBitmap(text, 640, 640) }
    Image(bitmap.asImageBitmap(), contentDescription = "Team-QR-Code", modifier = Modifier.size(280.dp))
}
fun qrBitmap(text: String, width: Int, height: Int): Bitmap {
    val matrix: BitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) for (y in 0 until height) bmp.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
    return bmp
}
fun nearbyAndAppPermissions(): Array<String> = buildList {
    add(Manifest.permission.CAMERA)
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    if (Build.VERSION.SDK_INT >= 31) {
        add(Manifest.permission.BLUETOOTH_ADVERTISE)
        add(Manifest.permission.BLUETOOTH_CONNECT)
        add(Manifest.permission.BLUETOOTH_SCAN)
    }
    if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.NEARBY_WIFI_DEVICES)
}.toTypedArray()
fun missingAppPermissions(context: Context): Array<String> = nearbyAndAppPermissions().filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }.toTypedArray()
