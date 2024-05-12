package util

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class UtilsTest {

    @Test
    fun `test getFileNameFromUrl Extracts Filename From Modrinth Url`() {
        assertEquals(
            "sodium-fabric-0.5.8+mc1.20.6.jar",
            getFileNameFromUrl("https://cdn.modrinth.com/data/AANobbMI/versions/IZskON6d/sodium-fabric-0.5.8%2Bmc1.20.6.jar")
        )
    }

    @Test
    fun `test getFileNameFromUrl Extracts Filename From CurseForge Url`() {
        assertEquals(
            "Gobber2-Fabric-1.20.6-2.9.37.jar",
            getFileNameFromUrl("https://mediafilez.forgecdn.net/files/5333/269/Gobber2-Fabric-1.20.6-2.9.37.jar")
        )
    }

    @Test
    fun `test getFileNameFromUrl Extracts Filename From Github Url`() {
        assertEquals(
            "BOMD-1.8.2-1.20.4.jar",
            getFileNameFromUrl("https://raw.githubusercontent.com/example/fantastic-craft/main/BOMD-1.8.2-1.20.4.jar")
        )
    }

    @Test
    fun `test getFileNameFromUrl Throws Exception For Empty Url`() {
        assertThrows<IllegalArgumentException> { getFileNameFromUrl("") }
    }

    @Test
    fun `test getSHA256Checksum`() {
        assertEquals(
            "ecd6e096b3c54ada81bd9174f3d150116259f51b53a6aae576cc00996ede1f40",
            getSHA256Checksum(
                getResourceURL("Punji-1.20.6-Fabric-1.0.1.jar").path,
            ),
        )
    }

    @Test
    fun `test getSHA512Checksum`() {
        assertEquals(
            "4e23be8cee226e92dfd0f7ef408219bdb4edfcbcc738d11b6e2ae7b4f84a89c4037130" +
                    "4a84ae6a68074239d743251c45aa1ca3c1caf0d17b97e32c13c3fb7b38",
            getSHA512Checksum(
                getResourceURL("Punji-1.20.6-Fabric-1.0.1.jar").path,
            ),
        )
    }
}
