package de.bsw.plakatradar.core

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ExportNames {
    private val stamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.GERMANY)

    fun authorityZipName(municipality: String, nowMillis: Long = System.currentTimeMillis()): String {
        val cleanMunicipality = municipality
            .ifBlank { "Kommune" }
            .replace(Regex("[^A-Za-z0-9ÄÖÜäöüß_-]"), "_")
            .replace(Regex("_+"), "_")
            .trim('_')
            .ifBlank { "Kommune" }

        return "PlakatRadar_Verwaltung_${cleanMunicipality}_${stamp.format(Date(nowMillis))}.zip"
    }
}
