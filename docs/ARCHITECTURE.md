# PlakatRadar Architektur

Stand: 0.12.0

## Grundsatz

Der Kotlin-Quellcode ist die Wahrheit. Build-Zeit-Patches, die Kotlin-Dateien während des Builds verändern, sollen nicht mehr verwendet werden.

## Aktueller Refactoring-Pfad

1. Build-Zeit-Patches entfernen.
2. UI-Code direkt in Kotlin halten.
3. Danach schrittweise MVVM-Struktur mit klaren ViewModels und StateFlow einführen.
4. Kernlogik mit Unit-Tests absichern, besonders SyncMerge, AccessPolicy und SyncBundleCodec.
5. QR-Sicherheitsmodell später um Revoke/Roll-over erweitern.

## Nicht-Ziel dieser Stufe

Diese Stufe ist kein kompletter Neuaufbau. Ziel ist zuerst, die technische Schuld aus dem Build-Prozess zu entfernen, ohne die funktionierende App unnötig zu gefährden.
