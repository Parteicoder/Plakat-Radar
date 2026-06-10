# PlakatRadar

PlakatRadar ist eine Android/Kotlin-App fuer lokale Teams. Die App hilft dabei, Plakatstandorte mit Foto, GPS, Status und Notiz zu erfassen, im Team abzugleichen und bei Bedarf als Stadtverwaltungs-ZIP zu exportieren.

> Status: MVP in aktiver Entwicklung. Die App ist fuer interne Tests gedacht und noch keine finale Store-Version.

## Grundprinzip

- lokale Datenspeicherung auf dem Geraet
- kein zentraler Cloud-Server
- Teamzugang ueber Teamleiter-QR-Code
- lokaler Nearby-Sync zwischen Teamgeraeten
- Sync-Pakete fuer Messenger, E-Mail oder Nearby Share
- Stadtverwaltungs-ZIP mit `plakatliste.csv` und Fotoordner

## Aktueller Funktionsumfang

- Plakat mit Foto, GPS, Adresse/Standorthinweis, Typ und Notiz erfassen
- Foto-Vorschau direkt im passenden Plakateintrag
- Plakatliste und Kartenansicht
- Plakate in der Naehe anzeigen
- Status aendern und Eintraege entfernen
- Teamleiter-QR-Code erzeugen
- zeitlich begrenzte QR-Einladung
- Team-Schluessel erneuern
- Geraete technisch sperren oder entsperren
- lokaler Nearby-Sync
- Sync-Paket teilen und importieren
- Stadtverwaltungs-ZIP exportieren
- Ohne-QR-Modus fuer lokale Erfassung und Stadtverwaltungs-Export
- moderne Dashboard-Navigation mit Start, Erfassen, Liste, Karte und Mehr

## Ohne-QR-Modus

Ohne QR-Code kann ein Geraet lokal arbeiten. Das ist praktisch, wenn vor Ort kein Teamleiter anwesend ist.

Im Ohne-QR-Modus moeglich:

- Plakate lokal erfassen
- Stadtverwaltungs-ZIP exportieren

Nicht moeglich:

- Team-Sync
- Sync-Paket teilen oder importieren
- Teamleiter-Funktionen

## Build und Tests

Das Projekt baut direkt aus dem Kotlin-Quellcode. Es gibt keine Python-Patch-Skripte, die zur Build-Zeit Kotlin-Code umschreiben. Der Quellcode im Repository ist die Wahrheit.

Wichtige Befehle:

```bash
sh ./gradlew :app:testDebugUnitTest
sh ./gradlew :app:assembleDebug
```

Unter Windows:

```bat
gradlew.bat :app:testDebugUnitTest
gradlew.bat :app:assembleDebug
```

Die Debug-APK liegt nach erfolgreichem Build hier:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## GitHub Actions

Der Workflow `Build Android APK` fuehrt zuerst die Unit-Tests aus und baut danach die Debug-APK. Das Artefakt heisst:

```text
PlakatRadar-debug-apk
```

## Wichtige Dateien

```text
app/src/main/java/de/bsw/plakatradar/MainActivity.kt
app/src/main/java/de/bsw/plakatradar/core/AccessPolicy.kt
app/src/main/java/de/bsw/plakatradar/core/TeamInvite.kt
app/src/main/java/de/bsw/plakatradar/core/SyncMerge.kt
app/src/main/java/de/bsw/plakatradar/core/SyncBundleCodec.kt
app/src/main/java/de/bsw/plakatradar/core/OfficialExport.kt
app/src/main/java/de/bsw/plakatradar/data/LocalRepository.kt
app/src/main/java/de/bsw/plakatradar/sync/NearbySyncManager.kt
app/src/test/java/de/bsw/plakatradar/core/AccessPolicyTest.kt
app/src/test/java/de/bsw/plakatradar/core/SyncMergeTest.kt
.github/workflows/android-debug-apk.yml
```

## Architekturstand

Der aktuelle MVP ist bewusst pragmatisch gebaut. Die naechsten sinnvollen Schritte sind:

- schrittweise MVVM/StateFlow-Struktur
- weitere Unit-Tests fuer Export, QR-Einladung und Sync-Pakete
- UI fuer Team-Schluessel-Erneuerung und Geraetesperre
- Release-Build mit fester Signatur
- Dokumentation in `docs/` buendeln

## Einschraenkungen

- kein echter Internet-Live-Sync ueber weite Entfernung
- keine zentrale Benutzerverwaltung
- OpenStreetMap-Kartenkacheln brauchen Internet, sofern sie nicht gecacht sind
- GitHub-Actions-APKs sind Debug-/Test-Builds
