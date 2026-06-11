# PlakatRadar

PlakatRadar ist eine Android-App fuer Wahlkampf-Teams.

Mit der App kann ein Team festhalten, **wo Plakate haengen**, **in welchem Zustand sie sind** und **was noch kontrolliert oder abgenommen werden muss**.

Die App ist noch in Entwicklung. Sie ist fuer interne Tests gedacht und noch keine fertige Play-Store-Version.

## Wofuer ist die App gedacht?

PlakatRadar hilft bei der Plakatierung.

Typische Fragen sind:

- Wo haengt welches Plakat?
- Welches Plakat wurde schon kontrolliert?
- Welches Plakat ist kaputt oder fehlt?
- Welche Plakate muessen wieder abgenommen werden?
- Welche Daten kann man an die Stadtverwaltung weitergeben?

## Was kann die App aktuell?

- Plakat mit Foto speichern
- Standort per GPS speichern
- Adresse oder Standort-Hinweis anzeigen
- Status setzen, zum Beispiel: haengt, kontrolliert, kaputt, fehlt oder entfernt
- Plakate auf einer Karte anzeigen
- Plakate in der Naehe anzeigen
- Team ueber QR-Code verbinden
- Daten lokal auf dem Handy speichern
- Daten mit Teamgeraeten in der Naehe abgleichen
- Sync-Paket teilen und importieren
- Liste fuer die Stadtverwaltung exportieren

## Warum ist das praktisch?

Ohne App landen Plakatstandorte oft in Zetteln, WhatsApp-Chats oder im Kopf einzelner Personen.

Mit PlakatRadar sieht das Team an einem Ort:

- wo Plakate stehen,
- was noch offen ist,
- was erledigt wurde,
- und wo etwas fehlt oder kaputt ist.

Das spart Zeit und verhindert Chaos beim Abhaengen.

## Team-Modus

Ein Teamleiter erstellt ein Team.

Danach koennen Teammitglieder per QR-Code beitreten.

Der QR-Code ist wichtig, damit nicht einfach fremde Geraete in das Team kommen.

## Ohne QR-Code nutzen

Die App kann auch ohne Team-QR genutzt werden.

Dann kann man Plakate lokal auf dem eigenen Handy erfassen.

Ohne QR-Code geht aber kein Team-Sync.

## Daten und Datenschutz

Die App arbeitet bewusst ohne zentralen Cloud-Server.

Die Daten liegen lokal auf den Geraeten. Teamdaten koennen ueber lokalen Sync oder ueber ein Sync-Paket geteilt werden.

Das ist fuer interne Wahlkampf-Arbeit gedacht.

## Geplante Erweiterungen

Spaeter soll die App auch bei anderen Ausseneinsaetzen helfen.

Geplant sind zum Beispiel:

- Wege-Tracking fuer Ausseneinsaetze
- Anzeige, welche Strassen oder Gebiete schon bearbeitet wurden
- Hilfe beim Flyerverteilen
- optionaler Schritt- oder Streckenzaehler

Damit koennte ein Team spaeter sehen, welche Strassen schon abgelaufen wurden und wo noch Luecken sind.

## App installieren

Die App ist aktuell eine Testversion.

Die fertige Test-APK findet man ueber GitHub Actions:

https://github.com/privatdavidgottschall-sudo/Plakat-Radar/actions/workflows/android-debug-apk.yml

Dort den neuesten gruenen Lauf oeffnen und unten das Artefakt herunterladen:

```text
PlakatRadar-debug-apk
```

Die ZIP-Datei entpacken und die APK auf dem Android-Handy installieren.

Android fragt dabei oft nach der Erlaubnis, Apps aus unbekannten Quellen zu installieren. Diese Erlaubnis muss man fuer den jeweiligen Browser oder Dateimanager aktivieren.

## Projekt herunterladen

GitHub-Projekt:

https://github.com/privatdavidgottschall-sudo/Plakat-Radar

Projekt als ZIP:

https://github.com/privatdavidgottschall-sudo/Plakat-Radar/archive/refs/heads/main.zip

## Fuer Entwickler

Die App ist mit Kotlin und Jetpack Compose gebaut.

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

Zum lokalen Bauen braucht man normalerweise:

- Android Studio
- Java JDK 17
- Android SDK 35

Der GitHub-Workflow baut automatisch eine Debug-APK.
