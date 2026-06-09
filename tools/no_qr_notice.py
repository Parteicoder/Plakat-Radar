from pathlib import Path

path = Path("app/src/main/java/de/bsw/plakatradar/MainActivity.kt")
text = path.read_text(encoding="utf-8")

# Make locked actions show the exact requested hint.
text = text.replace(
    'if (!AccessPolicy.canSync(ui.local)) return',
    'if (!AccessPolicy.canSync(ui.local)) { requireTeamQr(); return }'
)
text = text.replace(
    'if (!AccessPolicy.canShareSyncBundle(ui.local)) error("Bitte erst Team-QR-Code nutzen. Ohne Teamzugang gibt es kein Sync-Paket.")',
    'if (!AccessPolicy.canShareSyncBundle(ui.local)) error("Bitte Teamleiter-QR-Code scannen.")'
)
text = text.replace(
    'if (!AccessPolicy.canSync(ui.local)) error("Bitte erst Team-QR-Code vom Teamleiter scannen.")',
    'if (!AccessPolicy.canSync(ui.local)) error("Bitte Teamleiter-QR-Code scannen.")'
)

# Add a QR scanner launcher to HomeScreen.
old = '''    val syncImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importSharedSyncBundle(uri)
    }

    LazyColumn'''
new = '''    val syncImportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.importSharedSyncBundle(uri)
    }
    val qrScanner = rememberLauncherForActivityResult(ScanContract()) { result ->
        val code = result.contents
        if (code != null) vm.joinByQr(code, s.deviceName.ifBlank { "Teammitglied" })
    }

    LazyColumn'''
if old in text and "val qrScanner = rememberLauncherForActivityResult(ScanContract())" not in text:
    text = text.replace(old, new)

# Add notice card under the dashboard header when no real team QR was scanned.
old = '''        item {
            Text(s.teamName ?: "Plakat-Team", style = MaterialTheme.typography.headlineMedium)
            Text(if (s.role == MemberRole.LEADER) "Du bist Teamleiter." else "Du bist Teammitglied.")
            Text("Letzte Meldung: ${vm.ui.lastLog}")
        }
'''
new = old + '''
        if (!AccessPolicy.hasTeamAccess(s)) {
            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ohne Teamleiter-QR", style = MaterialTheme.typography.titleMedium)
                        Text("Bitte Teamleiter-QR-Code scannen. Team- und Teilen-Funktionen sind bis dahin nicht freigegeben.")
                        Button(
                            onClick = { qrScanner.launch(ScanOptions().setPrompt("QR-Code vom Teamleiter scannen").setBeepEnabled(false)) },
                            modifier = Modifier.fillMaxWidth().height(60.dp)
                        ) { Text("Teamleiter-QR-Code scannen") }
                    }
                }
            }
        }
'''
if old in text and "Ohne Teamleiter-QR" not in text:
    text = text.replace(old, new)

# Give important team/share buttons a grey locked look without disabling the click.
grey = 'colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)'
lines = []
for line in text.splitlines():
    stripped = line.strip()
    indent = line[:len(line) - len(line.lstrip())]
    if stripped.startswith('Button(onClick = vm::startOrStopSync'):
        lines.append(indent + 'if (AccessPolicy.hasTeamAccess(s)) {')
        lines.append(indent + '    ' + stripped)
        lines.append(indent + '} else {')
        lines.append(indent + '    Button(onClick = vm::requireTeamQr, modifier = Modifier.fillMaxWidth().height(60.dp), ' + grey + ') { Text("Lokalen Sync starten") }')
        lines.append(indent + '}')
    elif 'vm.shareSyncBundle(context)' in line and 'Button(onClick' in line:
        lines.append(indent + 'if (AccessPolicy.hasTeamAccess(s)) {')
        lines.append(indent + '    ' + stripped)
        lines.append(indent + '} else {')
        lines.append(indent + '    Button(onClick = vm::requireTeamQr, modifier = Modifier.fillMaxWidth().height(60.dp), ' + grey + ') { Text("Sync-Paket teilen") }')
        lines.append(indent + '}')
    elif 'syncImportLauncher.launch' in line and 'Button(onClick' in line:
        lines.append(indent + 'if (AccessPolicy.hasTeamAccess(s)) {')
        lines.append(indent + '    ' + stripped)
        lines.append(indent + '} else {')
        lines.append(indent + '    Button(onClick = vm::requireTeamQr, modifier = Modifier.fillMaxWidth().height(60.dp), ' + grey + ') { Text("Sync-Paket importieren") }')
        lines.append(indent + '}')
    else:
        lines.append(line)
text = "\n".join(lines) + "\n"

path.write_text(text, encoding="utf-8")
print("no QR notice and grey team buttons applied")
