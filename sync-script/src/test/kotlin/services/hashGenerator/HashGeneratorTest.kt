package services.hashGenerator

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import utils.getResourceAsFileOrThrow
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class HashGeneratorTest {
    private fun getTestFile() = getResourceAsFileOrThrow("Punji-1.20.6-Fabric-1.0.1.jar")

    private val hashGenerator: HashGenerator = HashGeneratorInstance

    @Test
    fun `test generateMD5 from text`() {
        val textToUse = "https://raw.githubusercontent.com/user/repository-name/main/sync-info.json"
        assertEquals(
            "41bf8e6df2a8d03241d3f53f297b789c",
            runBlocking { hashGenerator.generateMD5(text = textToUse).getOrThrow() },
        )
        assertNotEquals(
            "invalid-hash-value",
            runBlocking { hashGenerator.generateMD5(text = textToUse).getOrThrow() },
        )
    }

    @Test
    fun `test generateMD5 from file`() {
        assertEquals(
            "b957e0476916680c926a9342231305ce",
            runBlocking { hashGenerator.generateMD5(getTestFile()).getOrThrow() },
        )
        assertNotEquals(
            "invalid-hash-value",
            runBlocking { hashGenerator.generateMD5(getTestFile()).getOrThrow() },
        )
    }

    @Test
    fun `test generateSHA1`() {
        assertEquals(
            "a2f6bd4fb8a9f7b49ac7dda7a67d028227d8389e",
            runBlocking { hashGenerator.generateSHA1(getTestFile()).getOrThrow() },
        )
        assertNotEquals(
            "invalid-hash-value",
            runBlocking { hashGenerator.generateSHA1(getTestFile()).getOrThrow() },
        )
    }

    @Test
    fun `test generateSHA256`() {
        assertEquals(
            "ecd6e096b3c54ada81bd9174f3d150116259f51b53a6aae576cc00996ede1f40",
            runBlocking { hashGenerator.generateSHA256(getTestFile()).getOrThrow() },
        )
        assertNotEquals(
            "invalid-hash-value",
            runBlocking { hashGenerator.generateSHA256(getTestFile()).getOrThrow() },
        )
    }

    @Test
    fun `test generateSHA512`() {
        assertEquals(
            "4e23be8cee226e92dfd0f7ef408219bdb4edfcbcc738d11b6e2ae7b4f84a89c4037130" +
                "4a84ae6a68074239d743251c45aa1ca3c1caf0d17b97e32c13c3fb7b38",
            runBlocking { hashGenerator.generateSHA512(getTestFile()).getOrThrow() },
        )
        assertNotEquals(
            "invalid-hash-value",
            runBlocking { hashGenerator.generateSHA512(getTestFile()).getOrThrow() },
        )
    }
}
