# TODO PlakatRadar

Diese Liste sammelt die nächsten Baustellen. Immer nur kleine Schritte bauen und nach jedem Schritt den GitHub-Actions-Build prüfen.

## Kurzfristig

- [ ] Nächsten GitHub-Actions-Build prüfen und rote Fehler sofort beheben.
- [ ] Verwaltungs-ZIP auf dem Handy testen: Datei teilen, ZIP öffnen, CSV und Fotos prüfen.
- [ ] CSV im Verwaltungs-ZIP kontrollieren: Spalten, Umlaute, Koordinaten, Notizen und Status.
- [ ] Foto-Export prüfen: Fehlen Fotos, wenn ein Plakat ohne Foto erfasst wurde?
- [ ] README mit aktuellem Stand abgleichen, falls sich Funktionen ändern.

## App-Oberfläche

- [ ] Export-Button klarer benennen: „Export für Verwaltung“ statt allgemeiner Teilen-Text.
- [ ] Teamleiter-Ansicht verbessern: Mitglieder, Anzahl Plakate, letzte Meldung und QR-Code übersichtlicher zeigen.
- [ ] Statusänderung vereinfachen: Hängt, Kontrolliert, Kaputt, Fehlt, Entfernt.
- [ ] Bei „Kaputt“ und „Fehlt“ direkt Foto und Notiz anbieten.
- [ ] Texte für normale Nutzer vereinfachen, keine Technikbegriffe in Hauptbuttons.

## Karte

- [ ] Standort-Button auf der Karte prüfen oder verbessern.
- [ ] Marker nach Status deutlicher unterscheiden.
- [ ] Filter für Status einbauen: aktiv, kaputt, fehlt, entfernt.
- [ ] Plakate in der Nähe stärker hervorheben.
- [ ] Navigation zu einem Plakat einfacher erreichbar machen.

## Team und Sync

- [ ] Lokalen Sync mit zwei echten Geräten testen.
- [ ] Sync-Paket-Import und Sync-Paket-Export testen.
- [ ] Team-QR-Ablaufzeit und Schloss-Funktion prüfen.
- [ ] Geräte sperren und entsperren als Teamleiter-Funktion sichtbar machen.
- [ ] Team-Schlüssel erneuern verständlich erklären.

## Daten und Sicherheit

- [ ] Prüfen, ob alte entfernte Plakate wirklich nicht mehr im normalen Export stören.
- [ ] Interne Notizen klar vom Verwaltungs-Export trennen.
- [x] Export-Dateinamen sauber halten.
- [ ] Keine versteckten Cloud-Funktionen einbauen.
- [x] Keine Python- oder Build-Zeit-Patches mehr verwenden.

## Später

- [ ] Wege-Tracking für Außeneinsätze planen.
- [ ] Straßen oder Gebiete als erledigt markieren.
- [ ] Schritt- oder Streckenzähler für Flyerverteilung prüfen.
- [ ] Einsatzübersicht bauen: Wer war wo unterwegs?
- [ ] Screenshots für README ergänzen.

## Grundregel

Erst Stabilität, dann neue Funktionen. Kleine Commits, grüner Build, dann weiter.
