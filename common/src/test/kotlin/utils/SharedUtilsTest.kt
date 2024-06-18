package utils

import constants.SharedAssetConstants
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SharedUtilsTest {
    @Test
    fun `test getFileNameFromUrl Extracts Filename From Modrinth Url`() {
        assertEquals(
            "sodium-fabric-0.5.8+mc1.20.6.jar",
            getFileNameFromUrl("https://cdn.modrinth.com/data/AANobbMI/versions/IZskON6d/sodium-fabric-0.5.8%2Bmc1.20.6.jar").getOrThrow(),
        )
    }

    @Test
    fun `test getFileNameFromUrl Extracts Filename From CurseForge Url`() {
        assertEquals(
            "Gobber2-Fabric-1.20.6-2.9.37.jar",
            getFileNameFromUrl("https://mediafilez.forgecdn.net/files/5333/269/Gobber2-Fabric-1.20.6-2.9.37.jar").getOrThrow(),
        )
    }

    @Test
    fun `test getFileNameFromUrl Extracts Filename From Github Url`() {
        assertEquals(
            "BOMD-1.8.2-1.20.4.jar",
            getFileNameFromUrl("https://raw.githubusercontent.com/example/fantastic-craft/main/BOMD-1.8.2-1.20.4.jar").getOrThrow(),
        )
    }

    @Test
    fun `test getFileNameFromUrl Throws Exception For Empty Url`() {
        assertThrows<IllegalArgumentException> { getFileNameFromUrl("").getOrThrow() }
    }

    @Test
    fun `test isValidUrl`() {
        assertEquals(
            "http://example.com".isValidUrl(),
            true,
        )
        assertEquals(
            "https://example.com".isValidUrl(),
            true,
        )
        assertEquals(
            "invalid://example.com".isValidUrl(),
            false,
        )
        assertEquals(
            "https:////example.com".isValidUrl(),
            false,
        )
        assertEquals(
            "https:/example.com".isValidUrl(),
            false,
        )
        assertEquals(
            "https//example.com".isValidUrl(),
            false,
        )
    }

    @Test
    fun `test baseUrl`() {
        assertEquals(
            "https://cdn.modrinth.com/",
            "https://cdn.modrinth.com/data/nLlXyNIc/versions/TY9IJnhG/LetSleepingDogsLie-1.20.1-Forge-1.2.0.jar".baseUrl(),
        )
    }

    @Test
    fun `test getResourceAsFile`() {
        assertTrue(
            getResourceAsFileOrThrow(SharedAssetConstants.PROJECT_ICON_FILE_NAME).exists(),
        )
        assertThrows<IllegalStateException> {
            getResourceAsFileOrThrow("file-does-not-exist").exists()
        }
    }
}
