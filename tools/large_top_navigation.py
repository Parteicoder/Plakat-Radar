from pathlib import Path

path = Path("app/src/main/java/de/bsw/plakatradar/MainActivity.kt")
text = path.read_text(encoding="utf-8")

old_tabs = '''        TabRow(selectedTabIndex = tabs.indexOf(tab).coerceAtLeast(0)) {
            Tab(tab == "home", { tab = "home" }, text = { Text("Start") })
            Tab(tab == "add", { tab = "add" }, text = { Text("Plakat") })
            Tab(tab == "map", { tab = "map" }, text = { Text("Karte") })
            Tab(tab == "near", { tab = "near" }, text = { Text("Nähe") })
            Tab(tab == "list", { tab = "list" }, text = { Text("Liste") })
        }
'''

new_tabs = '''        Column(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { tab = "home" }, modifier = Modifier.weight(1f).height(64.dp)) { Text(if (tab == "home") "✓ Start" else "Start") }
                Button(onClick = { tab = "add" }, modifier = Modifier.weight(1f).height(64.dp)) { Text(if (tab == "add") "✓ Plakat" else "Plakat") }
                Button(onClick = { tab = "map" }, modifier = Modifier.weight(1f).height(64.dp)) { Text(if (tab == "map") "✓ Karte" else "Karte") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { tab = "near" }, modifier = Modifier.weight(1f).height(64.dp)) { Text(if (tab == "near") "✓ Nähe" else "Nähe") }
                Button(onClick = { tab = "list" }, modifier = Modifier.weight(1f).height(64.dp)) { Text(if (tab == "list") "✓ Liste" else "Liste") }
            }
        }
'''

if old_tabs in text:
    text = text.replace(old_tabs, new_tabs)
else:
    print("Top tab block not found, no change applied")

path.write_text(text, encoding="utf-8")
print("large top navigation applied")
