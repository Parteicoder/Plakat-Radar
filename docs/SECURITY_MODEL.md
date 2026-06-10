# PlakatRadar Sicherheitsmodell

## Kurzfazit

PlakatRadar ist ein lokales Team-Werkzeug ohne zentrale Cloud. Das ist gut fuer Datenschutz und einfache Nutzung, bedeutet aber: Es gibt keine perfekte serverseitige Zugangskontrolle.

Die App ist fuer vertraute Wahlkampf-Teams gedacht, nicht fuer oeffentliche oder anonyme Gruppen.

## Aktueller Schutz

- Teambeitritt erfolgt ueber Teamleiter-QR.
- Neue Team-QRs enthalten eine Ablaufzeit.
- Alte QR-Formate werden abgelehnt.
- Sync-Pakete werden nur angenommen, wenn Team-ID und Team-Schluessel passen.
- Lokal blockierte Geraete duerfen nicht mehr syncen oder Sync-Pakete teilen.

## Bekannte Grenzen

Ohne Server gibt es keinen globalen Sofort-Widerruf. Wenn jemand einen alten Team-Schluessel bereits besitzt, kann nur ein neuer Team-Schluessel diesen Zugriff sauber abschneiden. Das muss dann organisatorisch an die aktiven Teammitglieder verteilt werden.

Ein eingegebener Name ist kein Identitaetsnachweis. Die App weiss nur, welches lokale Geraet und welcher Team-Schluessel verwendet werden.

## Naechste technische Schritte

1. Team-Schluessel rotieren: Teamleiter erzeugt einen neuen Team-Schluessel und verteilt ihn nur an aktive Mitglieder.
2. Geraete-Sperrliste deutlicher in der UI verwalten und mit Sync-Paketen verteilen.
3. QR-Code nur als kurzlebige Einladung nutzen, danach geraetespezifische Mitgliedsdaten verwenden.
4. Tests fuer AccessPolicy, TeamInvite und SyncMerge ergaenzen.

## Praktische Empfehlung

- QR-Code nur kurz anzeigen.
- QR-Code nicht in offene Chats posten.
- Bei Teamwechsel oder Streit neuen Team-Schluessel erzeugen.
- Sync-Pakete nicht dauerhaft in fremden Messengern liegen lassen.
