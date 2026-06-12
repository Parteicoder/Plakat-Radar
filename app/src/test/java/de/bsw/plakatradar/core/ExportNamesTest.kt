package de.bsw.plakatradar.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportNamesTest {
    @Test
    fun authorityZipNameUsesReadablePrefixAndZipExtension() {
        val name = ExportNames.authorityZipName("Eilenburg", nowMillis = 1_704_067_200_000L)

        assertTrue(name.startsWith("PlakatRadar_Verwaltung_Eilenburg_"))
        assertTrue(name.endsWith(".zip"))
        assertFalse(name.contains("1704067200000"))
    }

    @Test
    fun authorityZipNameCleansUnsafeMunicipalityCharacters() {
        val name = ExportNames.authorityZipName("Bad Name !!/\\", nowMillis = 1_704_067_200_000L)

        assertTrue(name.startsWith("PlakatRadar_Verwaltung_Bad_Name_"))
        assertFalse(name.contains(" "))
        assertFalse(name.contains("!"))
        assertFalse(name.contains("/"))
        assertFalse(name.contains("\\"))
    }

    @Test
    fun authorityZipNameFallsBackToKommuneForBlankName() {
        val name = ExportNames.authorityZipName("   ", nowMillis = 1_704_067_200_000L)

        assertTrue(name.startsWith("PlakatRadar_Verwaltung_Kommune_"))
    }
}
