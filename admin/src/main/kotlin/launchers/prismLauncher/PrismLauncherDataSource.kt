package launchers.prismLauncher

import com.akuleshov7.ktoml.Toml
import constants.DotMinecraftFileNames
import curseForgeDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import launchers.Instance
import launchers.LauncherDataSource
import okio.IOException
import syncInfo.models.Mod
import utils.SystemFileProvider
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isHidden
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readLines
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.streams.toList

class PrismLauncherDataSource : LauncherDataSource {
    companion object {
        /**
         * A directory inside [DotMinecraftFileNames.MODS_DIRECTORY] that contains meta-data for the mods
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

    private fun getInstanceConfigFilePath(launcherInstanceDirectoryPath: Path): Path =
        launcherInstanceDirectoryPath.parent.resolve("instance.cfg")

    // TODO: We have a issue in the naming of launcherInstanceDirectory and similar names everywhere in the admin module
    //  by this, we mean the root instance folder as some launchers might store minecraft specific folders
    //  in sub folder that could be called (.minecraft) like in Prism Launcher and MultiMC
    //  yet we ask for the launcherInstanceDirectory everywhere which doesn't work for launchers like MultiMC
    //  a solution would be to review this name and call it something else like (dotMinecraftDirectory)
    //  and still request the launcherInstanceDirectory and will provide a function that return the (dotMinecraftDirectory)
    //  by the root instance folder (launcherInstanceDirectory), currently for this implementation, we're asking
    //  for the `.minecraft` folder, also update the GUI instructions (InstanceDirectoryInputField.kt), and the docs if there are any references

    override suspend fun validateInstanceDirectory(launcherInstanceDirectoryPath: Path): Result<Unit> {
        val dotMinecraftDirectoryPath = launcherInstanceDirectoryPath.parent.resolve(DOT_MINECRAFT_DIRECTORY_NAME)

        if (!dotMinecraftDirectoryPath.exists()) {
            return Result.failure(
                IllegalArgumentException(
                    "The file (${dotMinecraftDirectoryPath.absolutePathString()}) does not exist. If this " +
                        "is the root instance folder for Prism Launcher, the path should be to '.minecraft' folder.",
                ),
            )
        }
        if (!dotMinecraftDirectoryPath.isDirectory()) {
            return Result.failure(
                IllegalArgumentException("The file (${dotMinecraftDirectoryPath.absolutePathString()}) should be a folder."),
            )
        }

        val instanceConfigFilePath = getInstanceConfigFilePath(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath)

        if (!instanceConfigFilePath.exists()) {
            return Result.failure(IllegalArgumentException("The file (${instanceConfigFilePath.absolutePathString()}) does not exist."))
        }
        if (!instanceConfigFilePath.isRegularFile()) {
            return Result.failure(IllegalArgumentException("The file (${instanceConfigFilePath.absolutePathString()}) should be a file."))
        }

        return Result.success(Unit)
    }

    private fun isCurseForgeApiRequestNeededForMod(modMetadata: PrismLauncherModMetadata): Boolean =
        modMetadata.download.url.isBlank() && modMetadata.update.curseForge != null

    private fun getModsMetaDataDirectoryPath(launcherInstanceDirectoryPath: Path): Path =
        launcherInstanceDirectoryPath
            .resolve(launcherInstanceDirectoryPath)
            .resolve(DotMinecraftFileNames.MODS_DIRECTORY)
            .resolve(MODS_METADATA_DIRECTORY_NAME)

    private fun getModMetadataFilePaths(launcherInstanceDirectoryPath: Path): Result<List<Path>> {
        return try {
            val modsMetaDataDirectoryPath =
                getModsMetaDataDirectoryPath(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath)

            val modMetadataFilePaths =
                try {
                    Files
                        .list(modsMetaDataDirectoryPath)
                        .use { stream ->
                            stream.filter { it.isRegularFile() && !it.isHidden() && it.extension == MOD_METADATA_FILE_EXTENSION }
                        }.toList()
                } catch (e: Exception) {
                    return Result.failure(
                        IOException("Failed to list the files in (${modsMetaDataDirectoryPath.absolutePathString()}): ${e.message}"),
                    )
                }
            Result.success(modMetadataFilePaths)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getModsMetadata(launcherInstanceDirectoryPath: Path): Result<List<PrismLauncherModMetadata>> =
        try {
            val modMetadataFilePaths =
                getModMetadataFilePaths(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath).getOrThrow()
            val modsMetadata =
                modMetadataFilePaths.map {
                    val fileText = it.readText()
                    Toml.decodeFromString(PrismLauncherModMetadata.serializer(), fileText)
                }
            Result.success(modsMetadata)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    override suspend fun isCurseForgeApiRequestNeededForConvertingMods(launcherInstanceDirectoryPath: Path): Result<Boolean> =
        try {
            val modsMetadata =
                getModsMetadata(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath).getOrThrow()
            val isCurseForgeApiRequestNeeded =
                modsMetadata.any { modMetadata ->
                    isCurseForgeApiRequestNeededForMod(modMetadata = modMetadata)
                }
            Result.success(isCurseForgeApiRequestNeeded)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun hasMods(launcherInstanceDirectoryPath: Path): Result<Boolean> {
        return try {
            val modsMetaDataDirectoryPath =
                getModsMetaDataDirectoryPath(
                    launcherInstanceDirectoryPath = launcherInstanceDirectoryPath,
                )
            if (!modsMetaDataDirectoryPath.exists()) {
                return Result.success(false)
            }
            if (!modsMetaDataDirectoryPath.isDirectory()) {
                return Result.failure(
                    IllegalArgumentException("The file (${modsMetaDataDirectoryPath.absolutePathString()} should be a folder/directory."),
                )
            }
            val modMetadataFilePaths =
                getModMetadataFilePaths(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath).getOrThrow()
            Result.success(modMetadataFilePaths.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLauncherInstanceMods(
        launcherInstanceDirectoryPath: Path,
        overrideCurseForgeApiKey: String?,
    ): Result<List<Mod>> =
        try {
            val modsMetadata =
                getModsMetadata(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath).getOrThrow()
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
                                    overrideApiKey = overrideCurseForgeApiKey,
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

    override suspend fun getPreLaunchCommand(launcherInstanceDirectoryPath: Path): Result<String?> {
        return try {
            val preLaunchCommand =
                readInstanceProperty(
                    propertyKey = PropertyKey.PRE_LAUNCH_COMMAND,
                    instanceFileLines =
                        getInstanceConfigFilePath(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath).readLines(),
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
        launcherInstanceDirectoryPath: Path,
    ): Result<Unit> =
        try {
            // Manually update the file instead of Properties
            val instanceConfigFilePath =
                getInstanceConfigFilePath(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath)
            val instanceConfigFileLines = instanceConfigFilePath.readLines().toMutableList()

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

            instanceConfigFilePath.writeText(text = instanceConfigFileLines.joinToString("\n"))

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getInstances(): Result<List<Instance>?> =
        try {
            val (directoryPath, isFlatpak) =
                SystemFileProvider
                    .getUserApplicationDataDirectoryWithFlatpakSupport(
                        applicationDirectoryName = "PrismLauncher",
                        flatpakApplicationId = "org.prismlauncher.PrismLauncher",
                    ).getOrThrow()
            val instancesDirectoryPath =
                (if (isFlatpak) directoryPath?.resolve("PrismLauncher") else directoryPath)
                    ?.resolve("instances")
            val instances =
                instancesDirectoryPath
                    ?.let {
                        withContext(Dispatchers.IO) {
                            Files
                                .list(it)
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
                            launcherInstanceDirectoryPath = it.resolve(DOT_MINECRAFT_DIRECTORY_NAME),
                            instanceName = it.name,
                        )
                    }

            Result.success(instances)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
