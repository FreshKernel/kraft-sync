package launchers.atLauncher

import curseForgeDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import launchers.Instance
import launchers.LauncherDataSource
import syncInfo.models.Mod
import utils.JsonIgnoreUnknownKeys
import utils.JsonPrettyPrint
import utils.SystemFileProvider
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isHidden
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.streams.toList

class ATLauncherDataSource : LauncherDataSource {
    companion object {
        /**
         * ATLauncher will create a file in the instance directory containing all the info for the instance
         * */
        const val INSTANCE_FILE_NAME = "instance.json"
    }

    private fun getInstanceConfigFilePath(launcherInstanceDirectoryPath: Path): Path =
        launcherInstanceDirectoryPath.resolve(INSTANCE_FILE_NAME)

    private fun getInstance(launcherInstanceDirectoryPath: Path): Result<ATLauncherInstance> {
        return try {
            val instanceConfigFilePath =
                getInstanceConfigFilePath(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath)
            val instance = JsonIgnoreUnknownKeys.decodeFromString<ATLauncherInstance>(instanceConfigFilePath.readText())
            return Result.success(instance)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * ATLauncher identify the resource-packs, shaders, JAR mods and others as a mods,
     * which is why we're filtering the result
     *
     * @see ATLauncherInstance.Launcher.Mod.Type
     * */
    private fun getMods(instance: ATLauncherInstance): List<ATLauncherInstance.Launcher.Mod> =
        instance.launcher.mods
            .filter { it.type == ATLauncherInstance.Launcher.Mod.Type.Mods }

    override suspend fun validateInstanceDirectory(launcherInstanceDirectoryPath: Path): Result<Unit> {
        return try {
            val instanceConfigFilePath =
                getInstanceConfigFilePath(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath)
            if (!instanceConfigFilePath.exists()) {
                return Result.failure(IllegalArgumentException("The file (${instanceConfigFilePath.absolutePathString()}) does not exist."))
            }
            if (!instanceConfigFilePath.isRegularFile()) {
                return Result.failure(
                    IllegalArgumentException("The file (${instanceConfigFilePath.absolutePathString()}) should be a file."),
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun isCurseForgeApiRequestNeededForMod(mod: ATLauncherInstance.Launcher.Mod): Boolean {
        val modrinthFile = mod.modrinthVersion?.files?.firstOrNull()
        return modrinthFile == null && (mod.curseForgeProjectId != null && mod.curseForgeFileId != null)
    }

    override suspend fun isCurseForgeApiRequestNeededForConvertingMods(launcherInstanceDirectoryPath: Path): Result<Boolean> =
        try {
            val instance = getInstance(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath).getOrThrow()
            val isCurseForgeApiRequestNeeded =
                getMods(instance).any { atLauncherMod ->
                    isCurseForgeApiRequestNeededForMod(mod = atLauncherMod)
                }
            Result.success(isCurseForgeApiRequestNeeded)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    override suspend fun hasMods(launcherInstanceDirectoryPath: Path): Result<Boolean> =
        try {
            val mods =
                getMods(
                    instance = getInstance(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath).getOrThrow(),
                )
            Result.success(mods.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getLauncherInstanceMods(
        launcherInstanceDirectoryPath: Path,
        overrideCurseForgeApiKey: String?,
    ): Result<List<Mod>> =
        try {
            val instance = getInstance(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath).getOrThrow()

            val mods =
                getMods(instance = instance)
                    .map { atLauncherMod ->
                        val modrinthFile = atLauncherMod.modrinthVersion?.files?.firstOrNull()
                        val modrinthProject = atLauncherMod.modrinthProject

                        var downloadUrl = modrinthFile?.url
                        var fileIntegrityInfo = modrinthFile?.getFileIntegrityInfo()
                        val clientSupport = modrinthProject?.clientSide?.toModSupport() ?: Mod.ModSupport.defaultValue()
                        val serverSupport = modrinthProject?.serverSide?.toModSupport() ?: Mod.ModSupport.defaultValue()
                        val name = modrinthProject?.title
                        val description = modrinthProject?.description

                        // ATLauncher always store Modrinth data even if the mod downloaded
                        // from Curse Forge if available on both.
                        if (isCurseForgeApiRequestNeededForMod(mod = atLauncherMod)) {
                            // Handle the case when the mod is exclusively found on Curse Forge, not on Modrinth
                            val curseForgeModFile =
                                curseForgeDataSource
                                    .getModFile(
                                        modId = atLauncherMod.curseForgeProjectId.toString(),
                                        fileId = atLauncherMod.curseForgeFileId.toString(),
                                        overrideApiKey = overrideCurseForgeApiKey,
                                    ).getOrThrow()
                            downloadUrl = curseForgeModFile.data.downloadUrl
                            fileIntegrityInfo = curseForgeModFile.data.getFileIntegrityInfo()
                        }

                        requireNotNull(downloadUrl)
                        requireNotNull(fileIntegrityInfo)

                        Mod(
                            downloadUrl = downloadUrl,
                            fileIntegrityInfo = fileIntegrityInfo,
                            clientSupport = clientSupport,
                            serverSupport = serverSupport,
                            name = name,
                            description = description,
                        )
                    }
            Result.success(mods)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getPreLaunchCommand(launcherInstanceDirectoryPath: Path): Result<String?> =
        try {
            val instance = getInstance(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath).getOrThrow()
            Result.success(instance.launcher.preLaunchCommand)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun setPreLaunchCommand(
        command: String?,
        launcherInstanceDirectoryPath: Path,
    ): Result<Unit> =
        try {
            val instanceConfigFilePath =
                getInstanceConfigFilePath(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath)

            val instanceJsonObject: JsonObject = Json.parseToJsonElement(instanceConfigFilePath.readText()).jsonObject

            val launcherJsonKey = ATLauncherInstance::launcher.name
            val preLaunchCommandJsonKey = ATLauncherInstance.Launcher::preLaunchCommand.name
            val enableCommandsJsonKey = ATLauncherInstance.Launcher::enableCommands.name

            val updatedLauncher =
                (instanceJsonObject.jsonObject[launcherJsonKey]?.jsonObject?.toMutableMap() ?: mutableMapOf())
                    .apply {
                        if (command != null) {
                            this[preLaunchCommandJsonKey] = JsonPrimitive(command)
                            this[enableCommandsJsonKey] = JsonPrimitive(true)
                        } else {
                            this.remove(preLaunchCommandJsonKey)
                            // The user might use other commands like post-exit command,
                            // make sure we don't touch this key if they are used
                            val instance: ATLauncherInstance =
                                JsonIgnoreUnknownKeys.decodeFromJsonElement(instanceJsonObject)
                            if (instance.launcher.postExitCommand == null && instance.launcher.wrapperCommand == null) {
                                // The instance settings do not have any other commands,
                                // remove overriding enable commands for this instance
                                this.remove(enableCommandsJsonKey)
                            }
                        }
                    }.let { JsonObject(it) }

            val updatedInstance =
                instanceJsonObject.jsonObject
                    .toMutableMap()
                    .apply {
                        this[launcherJsonKey] = updatedLauncher
                    }.let { JsonObject(it) }

            instanceConfigFilePath.writeText(
                text =
                    JsonPrettyPrint.encodeToString(
                        JsonObject.serializer(),
                        updatedInstance,
                    ),
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getInstances(): Result<List<Instance>?> =
        try {
            val (directoryPath, _) =
                SystemFileProvider
                    .getUserApplicationDataDirectoryWithFlatpakSupport(
                        applicationDirectoryName = "ATLauncher",
                        flatpakApplicationId = "com.atlauncher.ATLauncher",
                    ).getOrThrow()
            val instances =
                directoryPath?.resolve("instances").let {
                    withContext(Dispatchers.IO) {
                        Files
                            .list(it)
                            .filter { it.isDirectory() && !it.isHidden() }
                            .toList()
                    }.map {
                        Instance(
                            launcherInstanceDirectoryPath = it,
                            instanceName = it.name,
                        )
                    }
                }
            Result.success(instances)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
