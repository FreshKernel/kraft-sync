package launchers.prismLauncher

import com.akuleshov7.ktoml.Toml
import constants.MinecraftInstanceNames
import curseForgeDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import launchers.Instance
import launchers.LauncherDataSource
import syncInfo.models.Mod
import utils.SystemFileProvider
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import kotlin.io.path.isHidden
import kotlin.io.path.name
import kotlin.streams.toList

class PrismLauncherDataSource : LauncherDataSource {
    companion object {
        /**
         * A directory inside [MinecraftInstanceNames.MODS_DIRECTORY] that contains meta-data for the mods
         * it's specific to this implementation
         * */
        const val MODS_METADATA_DIRECTORY_NAME = ".index"

        /**
         * Inside [MODS_METADATA_DIRECTORY_NAME], there will be files and each file has info about the mod
         * */
        const val MOD_METADATA_FILE_EXTENSION = "toml"

        const val DOT_MINECRAFT_DIRECTORY_NAME = ".minecraft"

        object PropertyKey {
            const val OVERRIDE_COMMANDS = "OverrideCommands"

            const val PRE_LAUNCH_COMMAND = "PreLaunchCommand"

            const val WRAPPER_COMMAND = "WrapperCommand"

            const val POT_EXIT_COMMAND = "PostExitCommand"
        }
    }

    private fun getInstanceConfigFile(launcherInstanceDirectory: File): File = launcherInstanceDirectory.parentFile.resolve("instance.cfg")

    // TODO: We have a issue in the naming of launcherInstanceDirectory and similar names everywhere in the admin module
    //  by this, we mean the root instance folder as some launchers might store minecraft specific folders
    //  in sub folder that could be called (.minecraft) like in Prism Launcher and MultiMC
    //  yet we ask for the launcherInstanceDirectory everywhere which doesn't work for launchers like MultiMC
    //  a solution would be to review this name and call it something else like (dotMinecraftDirectory)
    //  and still request the launcherInstanceDirectory and will provide a function that return the (dotMinecraftDirectory)
    //  by the root instance folder (launcherInstanceDirectory), currently for this implementation, we're asking
    //  for the `.minecraft` folder, also update the GUI instructions (InstanceDirectoryInputField.kt), and the docs if there are any references

    override suspend fun validateInstanceDirectory(launcherInstanceDirectory: File): Result<Unit> {
        val dotMinecraftDirectory = File(launcherInstanceDirectory.parentFile, DOT_MINECRAFT_DIRECTORY_NAME)

        if (!dotMinecraftDirectory.exists()) {
            return Result.failure(
                IllegalArgumentException(
                    "The file (${dotMinecraftDirectory.absolutePath}) does not exist. If this " +
                        "is the root instance folder for Prism Launcher, the path should be to '.minecraft' folder.",
                ),
            )
        }
        if (!dotMinecraftDirectory.isDirectory) {
            return Result.failure(IllegalArgumentException("The file (${dotMinecraftDirectory.absolutePath}) should be a folder."))
        }

        val instanceConfigFile = getInstanceConfigFile(launcherInstanceDirectory = launcherInstanceDirectory)

        if (!instanceConfigFile.exists()) {
            return Result.failure(IllegalArgumentException("The file (${instanceConfigFile.absolutePath}) does not exist."))
        }
        if (!instanceConfigFile.isFile) {
            return Result.failure(IllegalArgumentException("The file (${instanceConfigFile.absolutePath}) should be a file."))
        }

        return Result.success(Unit)
    }

    private fun isCurseForgeApiRequestNeededForMod(modMetadata: PrismLauncherModMetadata): Boolean =
        modMetadata.download.url.isBlank() && modMetadata.update.curseForge != null

    private fun getModsMetaDataDirectory(launcherInstanceDirectory: File): File =
        File(
            Paths.get(launcherInstanceDirectory.path, MinecraftInstanceNames.MODS_DIRECTORY).toFile(),
            MODS_METADATA_DIRECTORY_NAME,
        )

    private fun getModMetadataFiles(launcherInstanceDirectory: File): Result<List<File>> {
        return try {
            val modsMetaDataDirectory = getModsMetaDataDirectory(launcherInstanceDirectory = launcherInstanceDirectory)
            val modMetadataFiles =
                modsMetaDataDirectory.listFiles()?.filter {
                    it.isFile && it.extension == MOD_METADATA_FILE_EXTENSION
                }
            if (modMetadataFiles == null) {
                return Result.failure(
                    IllegalArgumentException("(${modsMetaDataDirectory.absolutePath}) might not be a directory or an I/O error occurred."),
                )
            }
            Result.success(modMetadataFiles)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getModsMetadata(launcherInstanceDirectory: File): Result<List<PrismLauncherModMetadata>> =
        try {
            val modMetadataFiles =
                getModMetadataFiles(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            val modsMetadata =
                modMetadataFiles.map {
                    val fileText = it.readText()
                    Toml.decodeFromString(PrismLauncherModMetadata.serializer(), fileText)
                }
            Result.success(modsMetadata)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    override suspend fun isCurseForgeApiRequestNeededForConvertingMods(launcherInstanceDirectory: File): Result<Boolean> =
        try {
            val modsMetadata =
                getModsMetadata(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            val isCurseForgeApiRequestNeeded =
                modsMetadata.any { modMetadata ->
                    isCurseForgeApiRequestNeededForMod(modMetadata = modMetadata)
                }
            Result.success(isCurseForgeApiRequestNeeded)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun hasMods(launcherInstanceDirectory: File): Result<Boolean> {
        return try {
            val modsMetaDataDirectory =
                getModsMetaDataDirectory(
                    launcherInstanceDirectory = launcherInstanceDirectory,
                )
            if (!modsMetaDataDirectory.exists()) {
                return Result.success(false)
            }
            if (!modsMetaDataDirectory.isDirectory) {
                return Result.failure(
                    IllegalArgumentException("The file (${modsMetaDataDirectory.absolutePath} should be a folder/directory."),
                )
            }
            val modMetadataFiles =
                getModMetadataFiles(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            Result.success(modMetadataFiles.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLauncherInstanceMods(
        launcherInstanceDirectory: File,
        curseForgeApiKeyOverride: String?,
    ): Result<List<Mod>> =
        try {
            val modsMetadata =
                getModsMetadata(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            val mods =
                modsMetadata.map { modMetadata ->
                    var modDownloadUrl = modMetadata.download.url
                    if (isCurseForgeApiRequestNeededForMod(modMetadata = modMetadata)) {
                        // The mod download URL is empty though not null

                        // Prism launcher and most launchers are no longer store the curse forge CDN download link
                        // see https://github.com/orgs/PrismLauncher/discussions/2394 for more details.

                        requireNotNull(modMetadata.update.curseForge) {
                            "The return value of ${::isCurseForgeApiRequestNeededForMod.name} " +
                                "is true yet the Curse Forge data is null."
                        }

                        modDownloadUrl =
                            curseForgeDataSource
                                .getModFileDownloadUrl(
                                    fileId =
                                        modMetadata.update.curseForge.fileId
                                            .toString(),
                                    modId =
                                        modMetadata.update.curseForge.projectId
                                            .toString(),
                                    overrideApiKey = curseForgeApiKeyOverride,
                                ).getOrThrow()
                                .data
                    }
                    require(modDownloadUrl.isNotBlank()) {
                        "The mod download URL should not be empty."
                    }
                    val (clientSupport, serverSupport) = modMetadata.side.toClientServerModSupport()
                    Mod(
                        downloadUrl = modDownloadUrl,
                        clientSupport = clientSupport,
                        serverSupport = serverSupport,
                        fileIntegrityInfo = modMetadata.download.getFileIntegrityInfo(),
                        name = modMetadata.name,
                    )
                }
            Result.success(mods)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    override suspend fun getPreLaunchCommand(launcherInstanceDirectory: File): Result<String?> {
        return try {
            val preLaunchCommand =
                readInstanceProperty(
                    propertyKey = PropertyKey.PRE_LAUNCH_COMMAND,
                    instanceFileLines = getInstanceConfigFile(launcherInstanceDirectory = launcherInstanceDirectory).readLines(),
                )
            return Result.success(preLaunchCommand)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun setInstancePropertyInGeneralSection(
        propertyKey: String,
        propertyValue: String?,
        instanceFileLines: MutableList<String>,
    ) {
        val generalSection = "[General]"
        val generalSectionIndex = instanceFileLines.indexOf(generalSection)
        val isGeneralSectionExist = generalSectionIndex != -1

        if (!isGeneralSectionExist) {
            throw IllegalArgumentException("The '$generalSection' doesn't exist in Prism Launcher instance configuration file.")
        }

        if (!instanceFileLines.any { it.contains("$propertyKey=") }) {
            // The property does not exist, adding it
            if (propertyValue != null) {
                instanceFileLines.add(generalSectionIndex + 1, "$propertyKey=$propertyValue")
            }
            return
        }

        // Update the existing property
        for ((index, line) in instanceFileLines.withIndex()) {
            if (!line.startsWith("$propertyKey=")) {
                continue
            }
            if (propertyValue == null) {
                instanceFileLines.removeAt(index)
                break
            }
            instanceFileLines[index] = line.replaceAfter("=", propertyValue)
        }
    }

    private fun readInstanceProperty(
        propertyKey: String,
        instanceFileLines: List<String>,
    ): String? {
        for (line in instanceFileLines) {
            if (!line.startsWith("$propertyKey=")) {
                continue
            }
            val (_, value) = line.split("=", limit = 2)
            val trimmedValue = value.trim()
            return trimmedValue
        }
        return null
    }

    override suspend fun setPreLaunchCommand(
        command: String?,
        launcherInstanceDirectory: File,
    ): Result<Unit> =
        try {
            // Manually update the file instead of Properties
            val instanceConfigFile = getInstanceConfigFile(launcherInstanceDirectory = launcherInstanceDirectory)
            val instanceConfigFileLines = instanceConfigFile.readLines().toMutableList()

            setInstancePropertyInGeneralSection(
                propertyKey = PropertyKey.PRE_LAUNCH_COMMAND,
                propertyValue = command,
                instanceFileLines = instanceConfigFileLines,
            )
            if (command != null) {
                setInstancePropertyInGeneralSection(
                    propertyKey = PropertyKey.OVERRIDE_COMMANDS,
                    propertyValue = "true",
                    instanceFileLines = instanceConfigFileLines,
                )
            } else {
                val postExistCommand =
                    readInstanceProperty(
                        propertyKey = PropertyKey.POT_EXIT_COMMAND,
                        instanceFileLines = instanceConfigFileLines,
                    )
                val wrapperCommand =
                    readInstanceProperty(
                        propertyKey = PropertyKey.WRAPPER_COMMAND,
                        instanceFileLines = instanceConfigFileLines,
                    )
                // The user might use other commands like post-exit command,
                // make sure we don't touch this key if they are used
                if (postExistCommand.isNullOrBlank() && wrapperCommand.isNullOrBlank()) {
                    // The instance settings do not have any other commands,
                    // remove overriding enable commands for this instance
                    setInstancePropertyInGeneralSection(
                        propertyKey = PropertyKey.OVERRIDE_COMMANDS,
                        propertyValue = null,
                        instanceFileLines = instanceConfigFileLines,
                    )
                }
            }

            instanceConfigFile.writeText(text = instanceConfigFileLines.joinToString("\n"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getInstances(): Result<List<Instance>?> =
        try {
            val (directory, isFlatpak) =
                SystemFileProvider
                    .getUserApplicationDataDirectoryWithFlatpakSupport(
                        applicationDirectoryName = "PrismLauncher",
                        flatpakApplicationId = "org.prismlauncher.PrismLauncher",
                    ).getOrThrow()
            val instancesDirectory =
                (if (isFlatpak) directory?.resolve("PrismLauncher") else directory)
                    ?.resolve("instances")
            val instances =
                instancesDirectory
                    ?.let {
                        withContext(Dispatchers.IO) {
                            Files
                                .list(it.toPath())
                                .filter {
                                    it.isDirectory() &&
                                        !it.isHidden() &&
                                        it.name !in
                                        listOf(
                                            ".LAUNCHER_TEMP",
                                            ".tmp",
                                        )
                                }.toList()
                        }
                    }?.map {
                        Instance(
                            launcherInstanceDirectory = it.resolve(DOT_MINECRAFT_DIRECTORY_NAME).toFile(),
                            instanceName = it.name,
                        )
                    }

            Result.success(instances)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
