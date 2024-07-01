package services.minecraft

import constants.SyncScriptDotMinecraftFiles
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.annotations.VisibleForTesting
import java.nio.file.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.forEachLine
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.io.path.writeText

/**
 * Helper class for reading [SyncScriptDotMinecraftFiles.Options] file which contains Minecraft settings
 * for reading and getting the properties or set them if it doesn't exist
 * */
object MinecraftOptionsManager {
    private var optionsFilePath = SyncScriptDotMinecraftFiles.Options.path
    private val properties: MutableMap<String, String> = mutableMapOf()

    private var isLoaded = false

    @VisibleForTesting
    fun setOptionsFilePathForTests(filePath: Path) {
        optionsFilePath = filePath
    }

    @VisibleForTesting
    fun getPropertiesForTests(): Map<String, String> = properties

    @VisibleForTesting
    fun unloadFileForTests() {
        isLoaded = false
        clear()
    }

    private fun throwIfNotLoaded() {
        if (isLoaded) {
            return
        }
        throw IllegalStateException("The options file should be loaded first before reading or modifying it.")
    }

    /**
     * Load the [properties] from [optionsFilePath]
     *
     * @throws IllegalArgumentException If [optionsFilePath] doesn't exist
     * @throws IndexOutOfBoundsException If the text of [optionsFilePath] is invalid
     * */
    fun loadPropertiesFromFile(createIfMissing: Boolean = false): Result<Unit> =
        try {
            if (createIfMissing && !optionsFilePath.exists()) {
                optionsFilePath.createFile()
            }
            require(
                optionsFilePath.exists(),
            ) { "The file ${optionsFilePath.name} doesn't exist in ${optionsFilePath.pathString}" }
            if (properties.isNotEmpty()) {
                properties.clear()
            }
            optionsFilePath.forEachLine { line ->
                val (key, value) = line.split(":", limit = 2)
                val trimmedKey = key.trim()
                val trimmedValue = value.trim()
                properties[trimmedKey] = trimmedValue
            }
            isLoaded = true
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    enum class Property(
        val key: String,
    ) {
        ResourcePacks("resourcePacks"),
        IncompatibleResourcePacks("incompatibleResourcePacks"),
        Lang("lang"),
    }

    /**
     * Should call [loadPropertiesFromFile] before calling this
     * @IllegalArgumentException If the property doesn't exist in [optionsFilePath]
     * */
    fun readProperty(property: Property): Result<String> =
        try {
            throwIfNotLoaded()

            val propertyKey = property.key
            val propertyValue = properties[propertyKey]
            requireNotNull(
                propertyValue,
            ) { "The key property $propertyKey doesn't exist in the ${optionsFilePath.name} in ${optionsFilePath.pathString}" }
            println(properties)
            Result.success(propertyValue)
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun readAsList(property: Property): List<String>? {
        val resourcePacksAsString = readProperty(property).getOrNull() ?: return null
        return Json.decodeFromString<List<String>>(resourcePacksAsString)
    }

    fun clear() {
        properties.clear()
        optionsFilePath.writeText(text = "")
    }

    fun setProperty(
        property: Property,
        propertyValue: String,
    ): Result<Unit> =
        try {
            throwIfNotLoaded()

            properties[property.key] = propertyValue

            optionsFilePath.bufferedWriter().use { bufferedWriter ->
                properties.forEach { (key, value) ->
                    bufferedWriter.appendLine("$key:$value")
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    private fun setPropertyAsList(
        property: Property,
        propertyValue: List<String>,
    ): Result<Unit> = setProperty(property, Json.encodeToString<List<String>>(propertyValue))

    sealed class ResourcePack(
        private val value: String,
    ) {
        data class File(
            val resourcePackZipFileName: String,
        ) : ResourcePack(value = resourcePackZipFileName)

        data class BuiltIn(
            val builtInResourcePackName: String,
        ) : ResourcePack(value = builtInResourcePackName)

        fun isFile() = this is File

        /**
         * Return Minecraft specific value of key [Property.ResourcePacks]
         * */
        fun toValue(): String =
            when (this) {
                is BuiltIn -> value
                is File -> "file/$value"
            }

        override fun toString(): String = toValue()

        companion object {
            /**
             * Get instance of [ResourcePack] by the Minecraft specific value of [Property.ResourcePacks]
             * */
            fun getByValue(value: String): ResourcePack {
                if (value.startsWith("file/", ignoreCase = true)) {
                    return File(resourcePackZipFileName = value.replaceFirst("file/", ""))
                }
                return BuiltIn(builtInResourcePackName = value)
            }
        }
    }

    fun readResourcePacks(): List<ResourcePack>? = readAsList(Property.ResourcePacks)?.map { ResourcePack.getByValue(it) }

    fun setResourcePacks(resourcePacks: List<ResourcePack>): Result<Unit> =
        setPropertyAsList(
            Property.ResourcePacks,
            resourcePacks.map {
                it.toValue()
            },
        )

    fun readIncompatibleResourcePacks(): List<ResourcePack>? =
        readAsList(Property.IncompatibleResourcePacks)?.map {
            ResourcePack.getByValue(it)
        }

    fun setIncompatibleResourcePacks(resourcePacks: List<ResourcePack>): Result<Unit> =
        setPropertyAsList(
            Property.IncompatibleResourcePacks,
            resourcePacks.map {
                it.toValue()
            },
        )
}
