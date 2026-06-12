# PlakatRadar

PlakatRadar ist eine einfache Android-App für interne Plakat-Teams.

Die App hilft dabei, Plakatstandorte zu speichern, zu kontrollieren und später wiederzufinden.

## Was kann die App?

- Plakat mit Foto speichern
- Standort per GPS speichern
- Adresse oder Hinweis zum Standort anzeigen
- Status setzen: hängt, kontrolliert, beschädigt, fehlt oder entfernt
- Plakate auf einer Karte anzeigen
- Plakate in der Nähe anzeigen
- Team über QR-Code verbinden
- Daten lokal auf dem Handy speichern
- Daten mit anderen Teamgeräten abgleichen
- Verwaltungs-ZIP exportieren

## Warum ist das praktisch?

Ohne App landen Standorte oft in Zetteln, Chats oder im Kopf einzelner Personen.

Mit PlakatRadar sieht das Team an einem Ort:

- wo Plakate hängen,
- was noch offen ist,
- was erledigt wurde,
- und wo etwas fehlt oder beschädigt ist.

Das spart Zeit und verhindert Chaos beim Abhängen.

## Team-Modus

Ein Teamleiter erstellt ein Team.

Teammitglieder können per QR-Code beitreten.

Ohne QR-Code kann die App auch allein genutzt werden. Dann gibt es aber keinen Team-Abgleich.

## Verwaltungs-Export

Die App kann eine ZIP-Datei für die Verwaltung erstellen.

Die ZIP enthält:

- `plakatliste.csv`
- Fotos, soweit sie beim Erfassen gemacht wurden

Der Export ist für die Weitergabe an Verwaltung oder interne Koordination gedacht.

## Datenschutz

Die Daten liegen lokal auf den Geräten.

Es gibt keinen zentralen Cloud-Server.

Team-Sync und Sync-Pakete sind für interne Teamnutzung gedacht.

## Interner Release

Die APK wird über GitHub Actions gebaut.

Das fertige Paket heißt:

```text
PlakatRadar-debug-apk
```

Vor der Weitergabe bitte die `RELEASE_CHECKLIST.md` prüfen.

## Geplante Erweiterungen

Später sollen weitere Funktionen dazukommen:

- Wege-Tracking für Außeneinsätze
- Anzeige, welche Straßen oder Gebiete schon bearbeitet wurden
- Hilfe beim Flyerverteilen
- optionaler Schritt- oder Streckenzähler

## Für Entwickler

Die App ist mit Kotlin und Jetpack Compose gebaut.

Wichtige Befehle:

```bash
sh ./gradlew :app:testDebugUnitTest
sh ./gradlew :app:assembleDebug
```
