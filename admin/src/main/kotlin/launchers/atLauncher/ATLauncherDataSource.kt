package launchers.atLauncher

import curseForgeDataSource
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import launchers.LauncherDataSource
import syncInfo.models.Mod
import utils.JsonIgnoreUnknownKeys
import utils.JsonPrettyPrint
import java.io.File
import java.nio.file.Paths

class ATLauncherDataSource : LauncherDataSource {
    companion object {
        /**
         * ATLauncher will create a file in the instance directory containing all the info for the instance
         * */
        const val INSTANCE_FILE_NAME = "instance.json"
    }

    private fun getInstanceConfigFile(launcherInstanceDirectory: File): File =
        Paths.get(launcherInstanceDirectory.path, INSTANCE_FILE_NAME).toFile()

    private fun getInstance(launcherInstanceDirectory: File): Result<ATLauncherInstance> {
        return try {
            val instanceConfigFile = getInstanceConfigFile(launcherInstanceDirectory = launcherInstanceDirectory)
            val instance = JsonIgnoreUnknownKeys.decodeFromString<ATLauncherInstance>(instanceConfigFile.readText())
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

    override suspend fun validateInstanceDirectory(launcherInstanceDirectory: File): Result<Unit> {
        return try {
            val instanceConfigFile = getInstanceConfigFile(launcherInstanceDirectory = launcherInstanceDirectory)
            if (!instanceConfigFile.exists()) {
                return Result.failure(IllegalArgumentException("The file (${instanceConfigFile.absolutePath}) does not exist."))
            }
            if (!instanceConfigFile.isFile) {
                return Result.failure(IllegalArgumentException("The file (${instanceConfigFile.absolutePath}) should be a file."))
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

    override suspend fun isCurseForgeApiRequestNeededForConvertingMods(launcherInstanceDirectory: File): Result<Boolean> =
        try {
            val instance = getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            val isCurseForgeApiRequestNeeded =
                getMods(instance).any { atLauncherMod ->
                    isCurseForgeApiRequestNeededForMod(mod = atLauncherMod)
                }
            Result.success(isCurseForgeApiRequestNeeded)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    override suspend fun hasMods(launcherInstanceDirectory: File): Result<Boolean> =
        try {
            val mods =
                getMods(
                    instance = getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow(),
                )
            Result.success(mods.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getLauncherInstanceMods(
        launcherInstanceDirectory: File,
        curseForgeApiKeyOverride: String?,
    ): Result<List<Mod>> =
        try {
            val instance = getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()

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
                                        overrideApiKey = curseForgeApiKeyOverride,
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

    override suspend fun getPreLaunchCommand(launcherInstanceDirectory: File): Result<String?> =
        try {
            val instance = getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            Result.success(instance.launcher.preLaunchCommand)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun setPreLaunchCommand(
        command: String?,
        launcherInstanceDirectory: File,
    ): Result<Unit> =
        try {
            val instanceConfigFile = getInstanceConfigFile(launcherInstanceDirectory = launcherInstanceDirectory)

            val instanceJsonObject: JsonObject = Json.parseToJsonElement(instanceConfigFile.readText()).jsonObject

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

            instanceConfigFile.writeText(
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
}
