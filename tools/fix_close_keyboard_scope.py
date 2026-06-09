from pathlib import Path

path = Path("app/src/main/java/de/bsw/plakatradar/MainActivity.kt")
text = path.read_text(encoding="utf-8")

# Fix: closeKeyboard was inserted into NearbyPostersScreen by the broad location-button patch.
old = '''@Composable
fun NearbyPostersScreen(vm: PlakatRadarViewModel) {
    val context = LocalContext.current
    var currentLat by remember { mutableStateOf<Double?>(null) }'''

new = '''@Composable
fun NearbyPostersScreen(vm: PlakatRadarViewModel) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    fun closeKeyboard() { focusManager.clearFocus(force = true) }
    var currentLat by remember { mutableStateOf<Double?>(null) }'''

if old in text and "fun NearbyPostersScreen(vm: PlakatRadarViewModel) {\n    val context = LocalContext.current\n    val focusManager = LocalFocusManager.current" not in text:
    text = text.replace(old, new)

# Fix: the QR timer patch must also catch the real wording in MainActivity.kt.
qr_variants = [
    '''        if (AccessPolicy.canShowQr(s)) {
            item {
                Divider()
                Text("Team-QR-Code. Nur du als Teamleiter stellst ihn bereit. Wer ihn scannt, ist direkt im Team. Der QR-Code ist 10 Minuten gültig.")
                vm.inviteText()?.let { QrCodeImage(it) }
            }
            item { TeamMembersCard(s) }
        }
''',
    '''        if (AccessPolicy.canShowQr(s)) {
            item {
                Divider()
                Text("Team-QR-Code. Nur du als Teamleiter stellst ihn bereit. Wer ihn scannt, ist direkt im Team. Der QR-Code ist jeweils 10 Minuten gültig.")
                vm.inviteText()?.let { QrCodeImage(it) }
            }
            item { TeamMembersCard(s) }
        }
'''
]

qr_new = '''        if (AccessPolicy.canShowQr(s)) {
            item { TeamInviteQrCard(vm) }
            item { TeamMembersCard(s) }
        }
'''

for qr_old in qr_variants:
    if qr_old in text:
        text = text.replace(qr_old, qr_new)

stable_qr_card = '''@Composable
fun TeamInviteQrCard(vm: PlakatRadarViewModel) {
    var locked by remember { mutableStateOf(false) }
    var refreshSeed by remember { mutableStateOf(0) }
    var remaining by remember { mutableStateOf(TeamInvite.DEFAULT_TTL_SECONDS.toInt()) }
    var qrText by remember(vm.ui.local.teamId, vm.ui.local.teamSecret, vm.ui.local.deviceName) {
        mutableStateOf(vm.inviteText())
    }

    LaunchedEffect(locked, refreshSeed, vm.ui.local.teamId, vm.ui.local.teamSecret, vm.ui.local.deviceName) {
        if (!locked) {
            qrText = vm.inviteText()
            remaining = TeamInvite.DEFAULT_TTL_SECONDS.toInt()
            while (remaining > 0 && !locked) {
                kotlinx.coroutines.delay(1000)
                remaining -= 1
            }
            if (!locked) refreshSeed += 1
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Divider()
        Text("Team-QR-Code. Nur du als Teamleiter stellst ihn bereit.")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (locked) "🔒 Aktueller QR bleibt gleich" else "🔓 Neuer QR in ${remaining}s")
            Switch(checked = locked, onCheckedChange = { locked = it })
        }
        Text(if (locked) "Schloss aktiv: Genau dieser sichtbare QR-Code bleibt stehen." else "Ohne Schloss wird die Anzeige nach 1 Minute automatisch erneuert.")
        qrText?.let { QrCodeImage(it) }
    }
}

'''

old_qr_card = '''@Composable
fun TeamInviteQrCard(vm: PlakatRadarViewModel) {
    var locked by remember { mutableStateOf(false) }
    var refreshSeed by remember { mutableStateOf(0) }
    var remaining by remember { mutableStateOf(TeamInvite.DEFAULT_TTL_SECONDS.toInt()) }

    LaunchedEffect(locked, refreshSeed) {
        remaining = TeamInvite.DEFAULT_TTL_SECONDS.toInt()
        if (!locked) {
            while (remaining > 0) {
                kotlinx.coroutines.delay(1000)
                remaining -= 1
            }
            refreshSeed += 1
        }
    }

    val qrText = remember(locked, refreshSeed, vm.ui.local.teamId, vm.ui.local.teamSecret, vm.ui.local.deviceName) {
        vm.inviteText(locked)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Divider()
        Text("Team-QR-Code. Nur du als Teamleiter stellst ihn bereit.")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(if (locked) "🔒 QR bleibt bestehen" else "🔓 Neuer QR in ${remaining}s")
            Switch(checked = locked, onCheckedChange = { locked = it; refreshSeed += 1 })
        }
        Text(if (locked) "Schloss aktiv: Der QR-Code bleibt dauerhaft nutzbar." else "Ohne Schloss ist der QR-Code 1 Minute gültig und aktualisiert sich automatisch.")
        qrText?.let { QrCodeImage(it) }
    }
}

'''

if old_qr_card in text:
    text = text.replace(old_qr_card, stable_qr_card)
elif "fun TeamInviteQrCard" not in text:
    text = text.replace(
        '''@Composable
fun TeamMembersCard(s: LocalTeamState) {''',
        stable_qr_card + '''@Composable
fun TeamMembersCard(s: LocalTeamState) {'''
    )

# Put app management buttons under the city administration export button.
export_block = '''        if (AccessPolicy.canExportForAuthority(s)) {
            item {
                Button(onClick = { vm.exportCsv(context, "Eilenburg") }, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text("Liste für Stadtverwaltung teilen") }
            }
        }
'''

management_block = export_block + '''
        item {
            Divider()
            Text("App verwalten")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { openAppSettings(context) }, modifier = Modifier.weight(1f).height(60.dp)) { Text("Deinstallieren") }
                Button(onClick = { openUpdatePage(context) }, modifier = Modifier.weight(1f).height(60.dp)) { Text("Update") }
            }
        }
'''

if 'Text("App verwalten")' not in text and export_block in text:
    text = text.replace(export_block, management_block)

# Add helper that opens the GitHub APK workflow page in the browser.
if "fun openUpdatePage(context: Context)" not in text:
    text = text.replace(
        '''fun openNavigation(context: Context, latitude: Double, longitude: Double, label: String) {''',
        '''fun openUpdatePage(context: Context) {
    val url = "https://github.com/privatdavidgottschall-sudo/Plakat-Radar/actions/workflows/android-debug-apk.yml"
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        Toast.makeText(context, "Update-Seite konnte nicht geöffnet werden.", Toast.LENGTH_LONG).show()
    }
}

fun openNavigation(context: Context, latitude: Double, longitude: Double, label: String) {'''
    )

# Add helper that opens Android app settings. From there Android offers uninstall.
if "fun openAppSettings(context: Context)" not in text:
    text = text.replace(
        '''fun openUpdatePage(context: Context) {''',
        '''fun openAppSettings(context: Context) {
    val uri = Uri.parse("package:${context.packageName}")
    val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS", uri)
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, "App-Einstellungen konnten nicht geöffnet werden.", Toast.LENGTH_LONG).show()
    }
}

fun openUpdatePage(context: Context) {'''
    )

path.write_text(text, encoding="utf-8")

# The visible timer is now only for automatic display refresh. Locked QR codes must remain accepted.
invite_path = Path("app/src/main/java/de/bsw/plakatradar/core/TeamInvite.kt")
invite_text = invite_path.read_text(encoding="utf-8")
invite_text = invite_text.replace(
    ''').also { it.requireStillValid() }''',
    ''')'''
)
invite_path.write_text(invite_text, encoding="utf-8")

print("scope, QR timer, stable lock and management buttons applied")
