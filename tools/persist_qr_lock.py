from pathlib import Path
import re

main_path = Path("app/src/main/java/de/bsw/plakatradar/MainActivity.kt")
text = main_path.read_text(encoding="utf-8")

new_card = '''@Composable
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
            prefs.edit()
                .putString("team_key", teamKey)
                .putBoolean("locked", true)
                .putString("qr_text", qrText)
                .apply()
        } else {
            qrText = vm.inviteText()
            prefs.edit()
                .putString("team_key", teamKey)
                .putBoolean("locked", false)
                .remove("qr_text")
                .apply()
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
            Text(if (locked) "🔒 Gespeicherter QR bleibt aktiv" else "🔓 Neuer QR in ${remaining}s")
            Switch(
                checked = locked,
                onCheckedChange = { isLocked ->
                    locked = isLocked
                    if (isLocked) {
                        prefs.edit()
                            .putString("team_key", teamKey)
                            .putBoolean("locked", true)
                            .putString("qr_text", qrText)
                            .apply()
                    } else {
                        prefs.edit()
                            .putString("team_key", teamKey)
                            .putBoolean("locked", false)
                            .remove("qr_text")
                            .apply()
                        refreshSeed += 1
                    }
                }
            )
        }
        Text(if (locked) "Schloss aktiv: Dieser QR bleibt auch nach dem Schließen der App erhalten." else "Ohne Schloss wird die Anzeige nach 1 Minute automatisch erneuert.")
        qrText?.let { QrCodeImage(it) }
    }
}

@Composable
fun TeamMembersCard'''

pattern = r'@Composable\s+fun TeamInviteQrCard\(vm: PlakatRadarViewModel\) \{.*?\n\}\n\n@Composable\nfun TeamMembersCard'
text, count = re.subn(pattern, new_card, text, flags=re.S)
if count == 0:
    raise SystemExit("TeamInviteQrCard block not found")

main_path.write_text(text, encoding="utf-8")

invite_path = Path("app/src/main/java/de/bsw/plakatradar/core/TeamInvite.kt")
invite_text = invite_path.read_text(encoding="utf-8")
invite_text = invite_text.replace(''').also { it.requireStillValid() }''', ''')''')
invite_path.write_text(invite_text, encoding="utf-8")

print("persistent QR lock applied")
