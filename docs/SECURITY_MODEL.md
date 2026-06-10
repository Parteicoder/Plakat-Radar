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

## Umgesetzt: Team-Schluessel rotieren

Der Teamleiter kann den lokalen Team-Schluessel erneuern. Danach werden neue Sync-Pakete mit dem neuen Schluessel erstellt und alte Team-Schluessel passen nicht mehr zu neuen Sync-Vorgaengen.

Wichtig:

- Teammitglieder muessen nach der Rotation einen neuen Teamleiter-QR-Code scannen.
- Alte Sync-Pakete bleiben technisch lesbar, wenn jemand den alten Schluessel bereits besitzt.
- Neue Sync-Pakete nach der Rotation sind mit dem neuen Schluessel geschuetzt.
- Ohne zentralen Server gibt es keinen sofortigen globalen Widerruf auf fremden Geraeten.

## Bekannte Grenzen

Ohne Server gibt es keinen globalen Sofort-Widerruf. Wenn jemand einen alten Team-Schluessel bereits besitzt, kann nur ein neuer Team-Schluessel diesen Zugriff fuer neue Sync-Daten sauber abschneiden. Das muss dann organisatorisch an die aktiven Teammitglieder verteilt werden.

Ein eingegebener Name ist kein Identitaetsnachweis. Die App weiss nur, welches lokale Geraet und welcher Team-Schluessel verwendet werden.

## Naechste technische Schritte

1. Team-Schluessel-Rotation in der Teamleiter-UI sichtbar machen.
2. Geraete-Sperrliste deutlicher in der UI verwalten und mit Sync-Paketen verteilen.
3. QR-Code nur als kurzlebige Einladung nutzen, danach geraetespezifische Mitgliedsdaten verwenden.
4. Tests fuer AccessPolicy, TeamInvite und SyncMerge ergaenzen.

## Praktische Empfehlung

- QR-Code nur kurz anzeigen.
- QR-Code nicht in offene Chats posten.
- Bei Teamwechsel oder Streit neuen Team-Schluessel erzeugen.
- Nach Schluesselwechsel nur noch neue Sync-Pakete akzeptieren.
- Sync-Pakete nicht dauerhaft in fremden Messengern liegen lassen.
