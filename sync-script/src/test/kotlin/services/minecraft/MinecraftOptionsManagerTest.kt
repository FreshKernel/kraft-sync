package services.minecraft

import constants.SyncScriptDotMinecraftFiles
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MinecraftOptionsManagerTest {
    companion object {
        const val FILE_PATH_THAT_DOES_NOT_EXIST = "non_existent_file.txt"
    }

    private var testsOptionsFilePath: Path = Paths.get(FILE_PATH_THAT_DOES_NOT_EXIST)
        set(value) {
            manager.setOptionsFilePathForTests(value)
            field = value
        }

    /**
     * Using [manager] to update the properties will also update [testsOptionsFilePath] as we're using [MinecraftOptionsManager.setOptionsFilePathForTests]
     * */
    private val manager = MinecraftOptionsManager

    @BeforeTest
    fun setUp() {
        testsOptionsFilePath =
            Files.createTempFile(
                SyncScriptDotMinecraftFiles.Options.file.nameWithoutExtension,
                ".${SyncScriptDotMinecraftFiles.Options.file.extension}",
            )

        // To make sure the next test doesn't use the properties from previous test
        manager.loadPropertiesFromFile().getOrThrow()
    }

    @AfterTest
    fun cleanUp() {
        testsOptionsFilePath.deleteIfExists()
    }

    private fun setOptionsFileText(value: String) {
        testsOptionsFilePath.writeText(value)
        manager.loadPropertiesFromFile().getOrThrow()
    }

    @Test
    fun `empty file should load empty properties`() {
        val properties = manager.getPropertiesForTests()
        assertTrue(
            properties.isEmpty(),
            "The properties is not empty when the file text is blank: $properties",
        )
    }

    @Test
    fun `file with properties should load not empty properties`() {
        val language = "en"
        manager.setProperty(property = MinecraftOptionsManager.Property.Lang, language)

        assertTrue(
            manager.getPropertiesForTests().isNotEmpty(),
            "The properties should be not empty when loading file with one property",
        )
        assertTrue(
            manager.getPropertiesForTests().size == 1,
            "The properties should have size of 1 when loading file with one property",
        )

        setOptionsFileText(
            buildString {
                appendLine(buildPropertyFileEntry(property = MinecraftOptionsManager.Property.Lang, value = language))
                appendLine(
                    buildPropertyFileEntry(
                        property = MinecraftOptionsManager.Property.ResourcePacks,
                        list = listOf("fabric"),
                    ),
                )
            },
        )

        // The same example with additional property
        manager.setResourcePacks(listOf(MinecraftOptionsManager.ResourcePack.BuiltIn("fabric")))

        assertTrue(
            manager.getPropertiesForTests().isNotEmpty(),
            "The properties should be not empty when loading file with two properties",
        )
        assertTrue(
            manager.getPropertiesForTests().size == 2,
            "The properties should have size of 2 when loading file with two properties",
        )
    }

    @Test
    fun `should throw exception for invalid file text format`() {
        assertThrows<IndexOutOfBoundsException> {
            // : is a separator between the value and key which is missing
            setOptionsFileText("${MinecraftOptionsManager.Property.Lang.key}value")
        }
    }

    @Test
    fun `should throw exception for file doesn't exist`() {
        assertTrue(testsOptionsFilePath.exists(), "The options file should exist")

        // Override the file with a file doesn't exist
        testsOptionsFilePath = Paths.get(FILE_PATH_THAT_DOES_NOT_EXIST)

        assertFalse(testsOptionsFilePath.exists(), "The options file exist which it shouldn't")
        assertThrows<IllegalArgumentException> {
            manager.loadPropertiesFromFile().getOrThrow()
        }
    }

    @Test
    fun `should not throw an exception for file exist`() {
        assertDoesNotThrow {
            manager.loadPropertiesFromFile().getOrThrow()
        }
    }

    @Test
    fun `reading a property doesn't exist should throw exception`() {
        manager.setProperty(property = MinecraftOptionsManager.Property.Lang, propertyValue = "de")

        assertThrows<IllegalArgumentException> {
            manager.readProperty(MinecraftOptionsManager.Property.ResourcePacks).getOrThrow()
        }
        assertNull(manager.readResourcePacks())
        assertNull(manager.readIncompatibleResourcePacks())
    }

    private fun buildPropertyFileEntry(
        property: MinecraftOptionsManager.Property,
        value: String,
    ): String = "${property.key}:$value"

    private fun buildPropertyFileEntry(
        property: MinecraftOptionsManager.Property,
        list: List<String>,
    ): String = buildPropertyFileEntry(property = property, value = Json.encodeToString<List<String>>(list))

    @Test
    fun `setting a property should update the file correctly`() {
        // Setting an existing value to read it and make sure it exists and hasn't modified later
        val currentIncompatibleResourcePacks =
            listOf(
                MinecraftOptionsManager.ResourcePack.BuiltIn("incompatible"),
                MinecraftOptionsManager.ResourcePack.File("incompatible2.zip"),
            )
        manager.setIncompatibleResourcePacks(currentIncompatibleResourcePacks)

        val inputResourcePacks =
            listOf(
                MinecraftOptionsManager.ResourcePack.BuiltIn("fabric"),
                MinecraftOptionsManager.ResourcePack.BuiltIn("quilt"),
            )
        val currentOptionsFileText = testsOptionsFilePath.readText()
        manager.setResourcePacks(inputResourcePacks).getOrThrow()
        val newOptionsFileText = testsOptionsFilePath.readText()

        val outputResourcePacks = manager.readResourcePacks()

        assertTrue(currentOptionsFileText != newOptionsFileText)
        assertEquals(outputResourcePacks, inputResourcePacks)
        assertNotEquals(outputResourcePacks, emptyList())
        assertNotEquals(
            outputResourcePacks,
            listOf(
                MinecraftOptionsManager.ResourcePack.File("custom.zip"),
                MinecraftOptionsManager.ResourcePack.BuiltIn("classic"),
            ),
        )

        assertNotNull(
            manager.readIncompatibleResourcePacks(),
            "Setting an a should not remove another key.",
        )

        assertEquals(
            manager.readIncompatibleResourcePacks(),
            currentIncompatibleResourcePacks,
            "Setting a key should not modify the value of another key",
        )
    }

    @Test
    fun `adding a new property should add it to the end of the file correctly`() {
        val inCompatibleResourcePacksLine =
            buildPropertyFileEntry(
                property = MinecraftOptionsManager.Property.IncompatibleResourcePacks,
                list = emptyList(),
            )
        setOptionsFileText(inCompatibleResourcePacksLine)

        // The property that doesn't exist yet
        val resourcePack = listOf<MinecraftOptionsManager.ResourcePack>()

        // Adding a new property
        manager.setResourcePacks(resourcePack)
        assertEquals(
            testsOptionsFilePath.readText(),
            buildString {
                appendLine(inCompatibleResourcePacksLine)
                appendLine(
                    buildPropertyFileEntry(
                        property = MinecraftOptionsManager.Property.ResourcePacks,
                        list = resourcePack.map { it.toValue() },
                    ),
                )
            },
        )
    }

    @Test
    fun `a property with incorrect list value should throw an exception`() {
        setOptionsFileText(
            buildPropertyFileEntry(
                property = MinecraftOptionsManager.Property.ResourcePacks,
                value = "[",
            ),
        )
        assertThrows<SerializationException> {
            manager.readResourcePacks()
        }
    }

    @Test
    fun `a property with valid list value should not throw an exception`() {
        val resourcePacks: List<String> = listOf("fabric")
        setOptionsFileText(
            buildPropertyFileEntry(
                property = MinecraftOptionsManager.Property.ResourcePacks,
                list = resourcePacks,
            ),
        )
        assertDoesNotThrow {
            manager.readResourcePacks()
        }
        assertEquals(
            manager.readResourcePacks()?.map { it.toValue() },
            resourcePacks,
        )
    }

    @Test
    fun `clearing the properties should clear the file too`() {
        manager.setResourcePacks(listOf())
        assertTrue(testsOptionsFilePath.readText().isNotEmpty())
        manager.clear()
        assertTrue(testsOptionsFilePath.readText().isEmpty())
    }

    @Test
    fun `reading or modifying properties before loading the file should throw exception`() {
        manager.unloadFileForTests()
        assertThrows<IllegalStateException> {
            manager.readProperty(property = MinecraftOptionsManager.Property.Lang).getOrThrow()
        }
        assertThrows<IllegalStateException> {
            manager.setProperty(property = MinecraftOptionsManager.Property.Lang, propertyValue = "en").getOrThrow()
        }
    }

    @Test
    fun `reading or modifying properties after loading the file should not throw exception`() {
        assertDoesNotThrow {
            manager.setProperty(property = MinecraftOptionsManager.Property.Lang, propertyValue = "en").getOrThrow()
        }
        assertDoesNotThrow {
            manager.readProperty(property = MinecraftOptionsManager.Property.Lang).getOrThrow()
        }
    }
}
