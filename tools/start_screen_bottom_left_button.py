from pathlib import Path

path = Path("app/src/main/java/de/bsw/plakatradar/MainActivity.kt")
text = path.read_text(encoding="utf-8")

# Add context to StartScreen if missing.
start = text.split("fun StartScreen(vm: PlakatRadarViewModel)", 1)[1].split("fun DashboardScreen", 1)[0]
if "val context = LocalContext.current" not in start:
    text = text.replace(
        '    var myName by remember { mutableStateOf("") }\n',
        '    var myName by remember { mutableStateOf("") }\n    val context = LocalContext.current\n'
    )

# Add helper if missing.
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

# Replace only the simple outer Column opening with a Box + Column.
start = text.split("fun StartScreen(vm: PlakatRadarViewModel)", 1)[1].split("fun DashboardScreen", 1)[0]
if "Modifier.align(Alignment.BottomStart).padding(24.dp)" not in start:
    text = text.replace(
        '    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {',
        '    Box(Modifier.fillMaxSize()) {\n        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {'
    )
    marker = '''        }
    }
}

@Composable
fun DashboardScreen'''
    replacement = '''        }
        Button(
            onClick = { openAppSettings(context) },
            modifier = Modifier.align(Alignment.BottomStart).padding(24.dp).height(56.dp)
        ) { Text("Deinstallieren") }
    }
}

@Composable
fun DashboardScreen'''
    text = text.replace(marker, replacement, 1)

path.write_text(text, encoding="utf-8")
print("safe start screen bottom-left button applied")
