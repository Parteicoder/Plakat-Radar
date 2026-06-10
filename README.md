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

Der QR-Code kann gesperrt werden. Wenn das Schloss aktiv ist, bleibt der aktuell sichtbare QR-Code erhalten. Der gespeicherte QR bleibt auch nach dem Schließen und erneuten Öffnen der App sichtbar.

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

- Plakat mit Foto, GPS, Standorttext, Typ und Notiz erfassen
- Plakatliste anzeigen
- Kartenansicht mit OpenStreetMap/osmdroid
- Plakate in der Nähe anzeigen
- Status ändern, zum Beispiel hängend, geprüft, beschädigt, fehlt, ersetzt oder entfernt
- Abnahme-Erinnerung für fällige Plakate
- Teamleiter-QR-Code mit Schloss-Funktion
- Ohne-QR-Modus für eingeschränkte lokale Nutzung
- lokaler Team-Sync in der Nähe über Nearby Connections
- verschlüsseltes Sync-Paket über Messenger, E-Mail, Signal, WhatsApp, Telegram oder Nearby Share
- Sync-Paket importieren
- Behördenexport als CSV für die Stadtverwaltung
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

## Einschränkungen

- Kein echter Internet-Live-Sync über weite Entfernung.
- Google-Service-Sync ist bisher nur ein Platzhalter.
- OpenStreetMap-Kartenkacheln brauchen Internet, sofern sie nicht bereits gecacht sind.
- GitHub-Actions-APKs sind Debug-/Test-Builds und keine finale Play-Store-Version.
- Für echte Updates ohne Deinstallation ist später eine feste Release-Signatur nötig.
- Der aktuelle Build-Workflow arbeitet mit Patch-Skripten unter `tools/`, die zur Build-Zeit UI- und Funktionskorrekturen anwenden.

## Wichtige Dateien

- `app/src/main/java/de/bsw/plakatradar/MainActivity.kt`
- `app/src/main/java/de/bsw/plakatradar/core/TeamInvite.kt`
- `app/src/main/java/de/bsw/plakatradar/core/AccessPolicy.kt`
- `app/src/main/java/de/bsw/plakatradar/core/SyncMerge.kt`
- `app/src/main/java/de/bsw/plakatradar/data/LocalRepository.kt`
- `app/src/main/java/de/bsw/plakatradar/sync/NearbySyncManager.kt`
- `app/src/main/java/de/bsw/plakatradar/sync/SyncBundleCodec.kt`
- `.github/workflows/android-debug-apk.yml`
- `tools/apply_fixes.py`
- `tools/fix_close_keyboard_scope.py`
- `tools/persist_qr_lock.py`
- `tools/large_top_navigation.py`
- `tools/start_screen_bottom_left_button.py`
- `tools/no_qr_notice.py`

## APK-Build

Der Debug-Build läuft über GitHub Actions:

`Build Android APK`

Das Artefakt heißt:

`PlakatRadar-debug-apk`

Die APK aus GitHub Actions ist für Tests gedacht. Für eine spätere öffentliche Version sollte ein Release-Build mit fester Signatur und sauberer Versionsnummer genutzt werden.

## Bedingungen

Weitere Nutzungshinweise und Grenzen stehen in:

`BEDINGUNGEN.md`
