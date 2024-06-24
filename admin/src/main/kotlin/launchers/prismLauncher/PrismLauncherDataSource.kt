package launchers.prismLauncher

import com.akuleshov7.ktoml.Toml
import constants.MinecraftInstanceNames
import curseForgeDataSource
import launchers.LauncherDataSource
import syncInfo.models.Mod
import java.io.File
import java.nio.file.Paths

class PrismLauncherDataSource : LauncherDataSource {
    companion object {
        /**
         * A folder inside [MinecraftInstanceNames.MODS_FOLDER] that contains meta-data for the mods
         * it's specific to this implementation
         * */
        const val MODS_METADATA_FOLDER_NAME = ".index"

        /**
         * Inside [MODS_METADATA_FOLDER_NAME], there will be files and each file has info about the mod
         * */
        const val MOD_METADATA_FILE_EXTENSION = "toml"

        object PropertyKey {
            const val OVERRIDE_COMMANDS = "OverrideCommands"

            const val PRE_LAUNCH_COMMAND = "PreLaunchCommand"

            const val WRAPPER_COMMAND = "WrapperCommand"

            const val POT_EXIT_COMMAND = "PostExitCommand"
        }
    }

    private fun getInstanceFile(launcherInstanceDirectory: File): File = launcherInstanceDirectory.parentFile.resolve("instance.cfg")

    private fun getModsMetaDataFolder(launcherInstanceDirectory: File): File =
        File(
            Paths.get(launcherInstanceDirectory.path, MinecraftInstanceNames.MODS_FOLDER).toFile(),
            MODS_METADATA_FOLDER_NAME,
        )

    private fun getPrismLauncherModsMetadata(launcherInstanceDirectory: File): Result<List<PrismLauncherModMetadata>> {
        return try {
            val modsMetaDataFolder = getModsMetaDataFolder(launcherInstanceDirectory = launcherInstanceDirectory)
            val modMetadataFiles =
                modsMetaDataFolder.listFiles()?.filter {
                    it.isFile && it.extension == MOD_METADATA_FILE_EXTENSION
                }
            if (modMetadataFiles == null) {
                return Result.failure(
                    IllegalArgumentException("(${modsMetaDataFolder.path}) might not be a directory or an I/O error occurred."),
                )
            }
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
    }

    // TODO: Broken, decode which directory should we choose, either .minecraft or prism launcher root folder,
    //  also this only work for mods, doesn't work if we want the directory to install sync script
    //  might refactor this function to return true or false to validate the folders in it instead
    //  for all implementations or maybe for this only, also might avoid to read twice by using the error that will be thrown
    override suspend fun validateInstanceDirectory(launcherInstanceDirectory: File): Result<Unit> =
        try {
            getPrismLauncherModsMetadata(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    private fun isCurseForgeApiRequestNeededForMod(prismLauncherModMetadata: PrismLauncherModMetadata): Boolean =
        prismLauncherModMetadata.download.url.isBlank() && prismLauncherModMetadata.update.curseForge != null

    override suspend fun isCurseForgeApiRequestNeeded(launcherInstanceDirectory: File): Result<Boolean> =
        try {
            val prismLauncherModsMetadata =
                getPrismLauncherModsMetadata(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            val isCurseForgeApiRequestNeeded =
                prismLauncherModsMetadata.any { prismLauncherModMetadata ->
                    isCurseForgeApiRequestNeededForMod(prismLauncherModMetadata = prismLauncherModMetadata)
                }
            Result.success(isCurseForgeApiRequestNeeded)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getMods(
        launcherInstanceDirectory: File,
        curseForgeApiKeyOverride: String?,
    ): Result<List<Mod>> =
        try {
            val prismLauncherModsMetadata =
                getPrismLauncherModsMetadata(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            val mods =
                prismLauncherModsMetadata.map { prismLauncherModMetadata ->
                    var modDownloadUrl = prismLauncherModMetadata.download.url
                    if (isCurseForgeApiRequestNeededForMod(prismLauncherModMetadata = prismLauncherModMetadata)) {
                        // The mod download URL is empty though not null

                        // Prism launcher and most launchers are no longer store the curse forge CDN download link
                        // see https://github.com/orgs/PrismLauncher/discussions/2394 for more details.

                        requireNotNull(prismLauncherModMetadata.update.curseForge) {
                            "The return value of ${::isCurseForgeApiRequestNeededForMod.name} " +
                                "is true yet the Curse Forge data is null."
                        }

                        modDownloadUrl =
                            curseForgeDataSource
                                .getModFileDownloadUrl(
                                    fileId =
                                        prismLauncherModMetadata.update.curseForge.fileId
                                            .toString(),
                                    modId =
                                        prismLauncherModMetadata.update.curseForge.projectId
                                            .toString(),
                                    overrideApiKey = curseForgeApiKeyOverride,
                                ).getOrThrow()
                                .data
                    }
                    require(modDownloadUrl.isNotBlank()) {
                        "The mod download URL should not be empty."
                    }
                    val (clientSupport, serverSupport) = prismLauncherModMetadata.side.toClientServerModSupport()
                    Mod(
                        downloadUrl = modDownloadUrl,
                        clientSupport = clientSupport,
                        serverSupport = serverSupport,
                        fileIntegrityInfo = prismLauncherModMetadata.download.getFileIntegrityInfo(),
                        name = prismLauncherModMetadata.name,
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
                    instanceFileLines = getInstanceFile(launcherInstanceDirectory = launcherInstanceDirectory).readLines(),
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
            val instanceFile = getInstanceFile(launcherInstanceDirectory = launcherInstanceDirectory)
            val instanceFileLines = instanceFile.readLines().toMutableList()

            setInstancePropertyInGeneralSection(
                propertyKey = PropertyKey.PRE_LAUNCH_COMMAND,
                propertyValue = command,
                instanceFileLines = instanceFileLines,
            )
            if (command != null) {
                setInstancePropertyInGeneralSection(
                    propertyKey = PropertyKey.OVERRIDE_COMMANDS,
                    propertyValue = "true",
                    instanceFileLines = instanceFileLines,
                )
            } else {
                val postExistCommand =
                    readInstanceProperty(
                        propertyKey = PropertyKey.POT_EXIT_COMMAND,
                        instanceFileLines = instanceFileLines,
                    )
                val wrapperCommand =
                    readInstanceProperty(
                        propertyKey = PropertyKey.WRAPPER_COMMAND,
                        instanceFileLines = instanceFileLines,
                    )
                // The user might use other commands like post-exit command,
                // make sure we don't touch this key if they are used
                if (postExistCommand.isNullOrBlank() && wrapperCommand.isNullOrBlank()) {
                    // The instance settings do not have any other commands,
                    // remove overriding enable commands for this instance
                    setInstancePropertyInGeneralSection(
                        propertyKey = PropertyKey.OVERRIDE_COMMANDS,
                        propertyValue = null,
                        instanceFileLines = instanceFileLines,
                    )
                }
            }

            instanceFile.writeText(text = instanceFileLines.joinToString("\n"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
