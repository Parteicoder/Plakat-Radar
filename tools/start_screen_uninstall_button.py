from pathlib import Path

path = Path("app/src/main/java/de/bsw/plakatradar/MainActivity.kt")
text = path.read_text(encoding="utf-8")

# StartScreen needs a context so it can open Android's app info page.
if "fun StartScreen(vm: PlakatRadarViewModel)" in text:
    if "val context = LocalContext.current" not in text.split("fun StartScreen(vm: PlakatRadarViewModel)", 1)[1].split("fun DashboardScreen", 1)[0]:
        text = text.replace(
            '    fun closeKeyboard() { focusManager.clearFocus(force = true) }\n    val scanner = rememberLauncherForActivityResult',
            '    fun closeKeyboard() { focusManager.clearFocus(force = true) }\n    val context = LocalContext.current\n    val scanner = rememberLauncherForActivityResult'
        )
        text = text.replace(
            '    var myName by remember { mutableStateOf("") }\n    val scanner = rememberLauncherForActivityResult',
            '    var myName by remember { mutableStateOf("") }\n    val context = LocalContext.current\n    val scanner = rememberLauncherForActivityResult'
        )

# Add uninstall button directly under the team selection buttons on the first screen.
if 'Text("Deinstallieren")' not in text.split("fun StartScreen(vm: PlakatRadarViewModel)", 1)[1].split("fun DashboardScreen", 1)[0]:
    text = text.replace(
        '''        Button(onClick = { mode = "leader" }, modifier = Modifier.fillMaxWidth().height(72.dp)) { Text("Ich bin Teamleiter") }
        Button(onClick = { mode = "member" }, modifier = Modifier.fillMaxWidth().height(72.dp)) { Text("Ich bin Teammitglied") }
        Divider()''',
        '''        Button(onClick = { mode = "leader" }, modifier = Modifier.fillMaxWidth().height(72.dp)) { Text("Ich bin Teamleiter") }
        Button(onClick = { mode = "member" }, modifier = Modifier.fillMaxWidth().height(72.dp)) { Text("Ich bin Teammitglied") }
        Button(onClick = { openAppSettings(context) }, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text("Deinstallieren") }
        Divider()'''
    )

# Ensure helper exists even when the dashboard management patch did not add it yet.
if "fun openAppSettings(context: Context)" not in text:
    text = text.replace(
        '''fun openNavigation(context: Context, latitude: Double, longitude: Double, label: String) {''',
        '''fun openAppSettings(context: Context) {
    val uri = Uri.parse("package:${context.packageName}")
    val intent = Intent("android.settings.APPLICATION_DETAILS_SETTINGS", uri)
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        Toast.makeText(context, "App-Einstellungen konnten nicht geöffnet werden.", Toast.LENGTH_LONG).show()
    }
}

fun openNavigation(context: Context, latitude: Double, longitude: Double, label: String) {'''
    )

path.write_text(text, encoding="utf-8")
print("start screen uninstall button applied")
