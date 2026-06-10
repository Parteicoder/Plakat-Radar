package de.bsw.plakatradar.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object OfficialExport {
    private val date = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)

    fun toCsv(state: LocalTeamState, municipality: String): String {
        val header = listOf(
            "Nr.",
            "Kommune",
            "Standortbeschreibung",
            "Plakatart",
            "Aktueller Status",
            "Erfasst am",
            "Abnahme geplant am",
            "Bemerkung für Stadtverwaltung",
            "GPS-Breitengrad",
            "GPS-Längengrad",
            "Google-Maps-Link"
        ).joinToString(";")

        val rows = state.posters.mapIndexed { index, poster ->
            val mapsLink = "https://www.google.com/maps/search/?api=1&query=${poster.latitude},${poster.longitude}"
            listOf(
                (index + 1).toString(),
                municipality,
                poster.addressHint.ifBlank { "Keine Standortbeschreibung eingetragen" },
                poster.type.toHumanText(),
                poster.status.toHumanText(),
                date.format(Date(poster.createdAt)),
                poster.plannedRemovalAt?.let { date.format(Date(it)) } ?: "Nicht eingetragen",
                poster.officialNote.ifBlank { "Keine Bemerkung" },
                poster.latitude.toString(),
                poster.longitude.toString(),
                mapsLink
            ).joinToString(";") { it.csv() }
        }

        // UTF-8 BOM helps older Excel versions open German umlauts correctly.
        return "\uFEFF" + (listOf(header) + rows).joinToString("\n")
    }

    private fun PosterType.toHumanText(): String = when (this) {
        PosterType.LAMP_POST -> "Laternenmast"
        PosterType.FENCE -> "Zaun"
        PosterType.BANNER -> "Banner"
        PosterType.TRIANGLE_STAND -> "Dreieckständer"
        PosterType.LARGE_FORMAT -> "Großformat / Großfläche"
        PosterType.OTHER -> "Sonstiges"
    }

    private fun PosterStatus.toHumanText(): String = when (this) {
        PosterStatus.HANGING -> "Hängt"
        PosterStatus.CHECKED -> "Kontrolliert"
        PosterStatus.DAMAGED -> "Beschädigt"
        PosterStatus.MISSING -> "Fehlt"
        PosterStatus.REPLACED -> "Ersetzt"
        PosterStatus.REMOVED -> "Entfernt"
    }

    private fun String.csv(): String = "\"" + replace("\"", "\"\"") + "\""
}
