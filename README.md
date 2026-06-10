# PlakatRadar

Android/Kotlin-MVP für eine Plakat-Tracking-App ohne zentrale Cloud-Datenbank. Die App ist für lokale Teams gedacht, die Plakatstandorte erfassen, kontrollieren, synchronisieren und bei Bedarf als Liste exportieren möchten.

> Stand: aktueller MVP-Arbeitsstand. Die App wird noch aktiv weiterentwickelt und ist keine finale Store-Version.

## Grundprinzip

PlakatRadar arbeitet lokal auf dem Gerät. Teamzugang und Synchronisierung laufen über einen Teamleiter-QR-Code oder über verschlüsselte Sync-Pakete.

Es gibt zwei Hauptrollen:

- **Teamleiter**
  - erstellt das Team
  - zeigt den Teamleiter-QR-Code
  - kann die Stadtverwaltungs-/CSV-Liste exportieren
  - sieht die Teammitglieder

- **Teammitglied**
  - trägt zuerst den eigenen Namen ein
  - kann danach den Teamleiter-QR-Code scannen
  - kann ohne QR-Code in einen eingeschränkten lokalen Modus gehen

## Teamzugang mit QR-Code

Der normale Teamzugang läuft über den Teamleiter-QR-Code:

1. Teamleiter erstellt ein Team.
2. Teamleiter zeigt den QR-Code.
3. Teammitglied trägt den eigenen Namen ein.
4. Teammitglied bestätigt, dass es den QR-Code scannen möchte.
5. Kamera öffnet sich und scannt den Teamleiter-QR-Code.
6. Danach arbeitet das Gerät mit dem Team-Schlüssel.

### QR-Schloss

Der QR-Code kann gesperrt werden. Wenn das Schloss aktiv ist, erzeugt die App einen langlebigen QR-Code und speichert ihn lokal. Der gespeicherte QR bleibt auch nach dem Schließen und erneuten Öffnen der App sichtbar.

Wichtig: Der QR-Schloss-Modus ist für die praktische Teamarbeit gedacht. Wenn ein QR-Code bereits weitergegeben oder fotografiert wurde, sollte organisatorisch darauf geachtet werden, wer Zugriff erhält.

## Ohne-QR-Modus

Wenn ein Teammitglied keinen QR-Code scannen möchte oder gerade keinen Teamleiter vor Ort hat, kann es **ohne QR** in die App gehen.

Ablauf:

1. „Ich bin Teammitglied“ wählen.
2. Namen eintragen.
3. „Weiter“ drücken.
4. Abfrage erscheint: Teamleiter-QR-Code scannen?
5. Bei **Ja** öffnet sich die Kamera.
6. Bei **Nein** öffnet sich der normale App-Bereich im eingeschränkten Ohne-QR-Modus.

Im Ohne-QR-Modus gilt:

- lokale App-Nutzung ist möglich
- Team-/Share-/Sync-Funktionen sind gesperrt
- beim Drücken gesperrter Funktionen erscheint der Hinweis: **„Bitte Teamleiter-QR-Code scannen.“**
- im Startbereich erscheint ein Button zum späteren Scannen des Teamleiter-QR-Codes

Der Ohne-QR-Modus ist kein echter Teambeitritt. Ein richtiger Teamzugang entsteht erst durch den QR-Code des Teamleiters.

## Was funktioniert im MVP

- Plakat mit Foto, GPS, automatisch erkannter Standortbeschreibung, Typ und Notiz erfassen
- Plakatliste anzeigen
- Kartenansicht mit OpenStreetMap/osmdroid
- Plakate in der Nähe anzeigen
- Status ändern, zum Beispiel hängend, geprüft, beschädigt, fehlt, ersetzt oder entfernt
- Plakate vollständig aus der Liste entfernen
- Abnahme-Erinnerung für fällige Plakate
- Teamleiter-QR-Code mit Schloss-Funktion
- Ohne-QR-Modus für eingeschränkte lokale Nutzung
- lokaler Team-Sync in der Nähe über Nearby Connections
- verschlüsseltes Sync-Paket über Messenger, E-Mail, Signal, WhatsApp, Telegram oder Nearby Share
- Sync-Paket importieren
- Behördenexport als gut lesbare CSV für die Stadtverwaltung
- große, besser drückbare Navigation oben
- Deinstallieren-/App-Info-Button
- Update-Button, der zur GitHub-Actions-/APK-Seite führt
- Berechtigungsdialog für Kamera, Standort und Nearby-Funktionen
- Google-Service-Schalter als Platzhalter, aktuell noch nicht aktiv

## Navigation und Bedienung

Die obere Navigation ist bewusst groß gestaltet, damit sie auf dem Handy leichter zu drücken ist:

- Start
- Plakat
- Karte
- Nähe
- Liste

Der Startbildschirm ist tastaturfreundlich. Wenn die Tastatur offen ist, kann der Inhalt gescrollt werden, damit wichtige Buttons nicht verdeckt werden.

## Sync-Arten

### 1. Lokaler Nearby-Sync

Nearby-Sync funktioniert, wenn Teamgeräte in der Nähe sind, zum Beispiel im selben Raum, Büro, Auto, WLAN-Umfeld oder bei einem Stammtisch.

### 2. Messenger-Sync

Die Funktion „Sync-Paket teilen“ erzeugt eine verschlüsselte Paketdatei mit:

- `snapshot.json` für Plakatdaten, Geräte, Status und Ereignisse
- `photos/` mit vorhandenen Plakatfotos

Diese Datei kann über Messenger, E-Mail oder Nearby Share geteilt und auf einem anderen Gerät importiert werden. Die App prüft den Team-Schlüssel und führt nur Pakete desselben Teams zusammen.

Wichtig: Messenger-Sync ist kein automatischer Live-Sync. Es ist ein manueller Austauschweg, wenn Nearby-Sync gerade nicht möglich ist.

## Datenschutzgedanke

- Es gibt keine zentrale Cloud-Datenbank.
- Die App fragt keine Parteizugehörigkeit ab.
- Der Teamzugang läuft über den Teamleiter-QR-Code.
- Ohne Teamleiter-QR bleiben Team- und Teilen-Funktionen gesperrt.
- Daten liegen lokal auf dem Gerät und in bewusst erstellten Sync-Paketen.
- Wer ein Sync-Paket teilt, entscheidet selbst, an wen es weitergegeben wird.

## Berechtigungen

Die App kann folgende Android-Berechtigungen benötigen:

- Kamera: QR-Code scannen und Plakatfoto aufnehmen
- Standort: GPS-Punkt des Plakats speichern und Plakate in der Nähe anzeigen
- Bluetooth/WLAN/Nearby: lokaler Sync ohne Cloud
- Dateien/Teilen: Sync-Pakete importieren oder exportieren

Manche Funktionen laufen eingeschränkt, wenn Berechtigungen fehlen.

## App auf dem PC bauen

Dieser Abschnitt ist bewusst einfach geschrieben. Man muss kein Profi-Entwickler sein, aber ein paar Grundprogramme braucht der PC trotzdem.

### Was braucht man dafür?

Für den Bau der App werden benötigt:

1. **Ein PC mit Windows, macOS oder Linux**
2. **Internetverbindung**
3. **Java JDK 17**
4. **Android Studio** oder mindestens das **Android SDK**
5. **Android SDK 35**
6. **Android Build Tools 35.0.0**
7. Dieses GitHub-Projekt als heruntergeladener Ordner

Am einfachsten ist es, **Android Studio** zu installieren. Android Studio bringt die wichtigsten Android-Werkzeuge mit oder kann sie nachinstallieren.

### Einfache Variante mit Android Studio

1. Android Studio installieren.
2. Dieses Projekt herunterladen oder über Git klonen.
3. Android Studio öffnen.
4. **Open** anklicken und den Projektordner `Plakat-Radar` auswählen.
5. Warten, bis Android Studio das Projekt geladen hat.
6. Wenn Android Studio nach fehlenden SDKs oder Lizenzen fragt, diese installieren beziehungsweise bestätigen.
7. Oben im Menü **Build** anklicken.
8. Dann **Build Bundle(s) / APK(s)** anklicken.
9. Dann **Build APK(s)** auswählen.

Wenn alles klappt, zeigt Android Studio am Ende einen Hinweis wie **APK generated successfully** an.

Die fertige APK liegt dann ungefähr hier:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Diese APK ist eine **Debug-Version**. Sie ist zum Testen gedacht, nicht als finale Play-Store-Version.

### Variante über die Konsole

Im Projekt liegt ein lokales Gradle-Startskript. Es lädt beim ersten Start automatisch Gradle 8.10.2 herunter und benutzt danach diese Version.

#### Windows

Im Projektordner eine Eingabeaufforderung oder PowerShell öffnen und ausführen:

```bat
gradlew.bat :app:assembleDebug
```

#### macOS oder Linux

Im Projektordner ein Terminal öffnen und ausführen:

```bash
sh ./gradlew :app:assembleDebug
```

Wenn der Build erfolgreich ist, liegt die APK hier:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Was macht `gradlew`?

`gradlew` und `gradlew.bat` sind Startskripte für Gradle. Gradle ist das Werkzeug, das aus dem Kotlin-Code eine Android-App baut.

In diesem Projekt laden die Skripte beim ersten Start Gradle 8.10.2 herunter und speichern es lokal im Projektordner unter:

```text
.gradle/bootstrap/
```

Danach muss Gradle nicht jedes Mal neu geladen werden.

### Häufige Probleme

#### „Java nicht gefunden“

Dann fehlt wahrscheinlich das Java JDK 17 oder es ist nicht richtig eingerichtet.

Lösung:

- JDK 17 installieren
- danach den PC oder das Terminal neu starten
- in Android Studio prüfen, ob JDK 17 verwendet wird

#### „Android SDK not found“

Dann ist das Android SDK nicht installiert oder Android Studio findet es nicht.

Lösung:

- Android Studio öffnen
- SDK Manager öffnen
- Android SDK 35 installieren
- Android Build Tools 35.0.0 installieren

#### „SDK licenses not accepted“

Dann wurden die Android-Lizenzen noch nicht bestätigt.

Lösung in Android Studio:

- Android Studio öffnen
- SDK Manager öffnen
- fehlende Pakete installieren
- Lizenzabfragen bestätigen

#### „gradlew darf nicht ausgeführt werden“ auf macOS/Linux

Dann fehlt eventuell die Ausführungsberechtigung. Man kann trotzdem direkt bauen mit:

```bash
sh ./gradlew :app:assembleDebug
```

Oder einmalig ausführbar machen:

```bash
chmod +x gradlew
./gradlew :app:assembleDebug
```

#### „Download von Gradle schlägt fehl“

Dann fehlt Internetzugang oder eine Firewall blockiert den Download.

Lösung:

- Internet prüfen
- später erneut versuchen
- in einem anderen Netzwerk testen

### Wichtig für normale Nutzer

Man muss die App nicht selbst bauen, wenn man nur testen will. Dann reicht normalerweise die APK aus GitHub Actions.

Selbst bauen ist vor allem sinnvoll für:

- Entwickler
- Tester
- Leute, die Änderungen am Code prüfen wollen
- Community-Mitglieder, die helfen möchten

## Einschränkungen

- Kein echter Internet-Live-Sync über weite Entfernung.
- Google-Service-Sync ist bisher nur ein Platzhalter.
- OpenStreetMap-Kartenkacheln brauchen Internet, sofern sie nicht bereits gecacht sind.
- GitHub-Actions-APKs sind Debug-/Test-Builds und keine finale Play-Store-Version.
- Für echte Updates ohne Deinstallation ist später eine feste Release-Signatur nötig.
- Der Build arbeitet direkt mit dem Kotlin-Quellcode. Die früheren Python-Patch-Skripte werden nicht mehr im GitHub-Workflow ausgeführt.

## Wichtige Dateien

- `app/src/main/java/de/bsw/plakatradar/MainActivity.kt`
- `app/src/main/java/de/bsw/plakatradar/core/TeamInvite.kt`
- `app/src/main/java/de/bsw/plakatradar/core/AccessPolicy.kt`
- `app/src/main/java/de/bsw/plakatradar/core/OfficialExport.kt`
- `app/src/main/java/de/bsw/plakatradar/core/SyncMerge.kt`
- `app/src/main/java/de/bsw/plakatradar/data/LocalRepository.kt`
- `app/src/main/java/de/bsw/plakatradar/sync/NearbySyncManager.kt`
- `app/src/main/java/de/bsw/plakatradar/sync/SyncBundleCodec.kt`
- `.github/workflows/android-debug-apk.yml`
- `gradlew`
- `gradlew.bat`
- `gradle/wrapper/gradle-wrapper.properties`

## APK-Build über GitHub

Der Debug-Build läuft über GitHub Actions:

`Build Android APK`

Das Artefakt heißt:

`PlakatRadar-debug-apk`

Die APK aus GitHub Actions ist für Tests gedacht. Für eine spätere öffentliche Version sollte ein Release-Build mit fester Signatur und sauberer Versionsnummer genutzt werden.

## Bedingungen

Weitere Nutzungshinweise und Grenzen stehen in:

`BEDINGUNGEN.md`
