package launchers.modrinth

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import launchers.LauncherDataSource
import launchers.modrinth.ModrinthLauncherInstance.ModrinthLauncherProject
import syncInfo.models.Mod
import utils.JsonIgnoreUnknownKeys
import utils.simpleMergeJsonObjects
import java.io.File
import java.nio.file.Paths

class ModrinthLauncherDataSource : LauncherDataSource {
    companion object {
        /**
         * Modrinth Launcher will create a file in the instance directory containing all the info for the instance
         * */
        const val INSTANCE_FILE_NAME = "profile.json"
    }

    private fun getInstanceConfigFile(launcherInstanceDirectory: File) =
        Paths.get(launcherInstanceDirectory.path, INSTANCE_FILE_NAME).toFile()

    private fun getInstance(launcherInstanceDirectory: File): Result<ModrinthLauncherInstance> =
        try {
            val instanceConfigFile = getInstanceConfigFile(launcherInstanceDirectory = launcherInstanceDirectory)
            val modrinthLauncherInstance =
                JsonIgnoreUnknownKeys.decodeFromString<ModrinthLauncherInstance>(instanceConfigFile.readText())
            Result.success(modrinthLauncherInstance)
        } catch (e: Exception) {
            Result.failure(e)
        }

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
            Result.failure(e)
        }
    }

    override suspend fun isCurseForgeApiRequestNeededForConvertingMods(launcherInstanceDirectory: File): Result<Boolean> =
        Result.success(false)

    private fun getModrinthLauncherProjects(instance: ModrinthLauncherInstance): Map<String, ModrinthLauncherProject> = instance.projects

    override suspend fun hasMods(launcherInstanceDirectory: File): Result<Boolean> {
        return try {
            val instanceConfigFile = getInstanceConfigFile(launcherInstanceDirectory = launcherInstanceDirectory)
            if (!instanceConfigFile.exists()) {
                return Result.failure(IllegalArgumentException("The file (${instanceConfigFile.absolutePath}) does not exist."))
            }
            if (!instanceConfigFile.isFile) {
                return Result.failure(IllegalArgumentException("The file (${instanceConfigFile.absolutePath}) should be a file."))
            }
            val atLauncherMods =
                getModrinthLauncherProjects(
                    instance =
                        getInstance(
                            launcherInstanceDirectory = launcherInstanceDirectory,
                        ).getOrThrow(),
                )
            Result.success(atLauncherMods.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMods(
        launcherInstanceDirectory: File,
        curseForgeApiKeyOverride: String?,
    ): Result<List<Mod>> =
        try {
            val instance = getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            val mods =
                getModrinthLauncherProjects(instance = instance).map { (_, project) ->
                    val metadata = project.metadata
                    val modrinthProject = metadata.project
                    val modrinthFile = metadata.version.files.first()
                    Mod(
                        downloadUrl = modrinthFile.url,
                        fileIntegrityInfo = modrinthFile.getFileIntegrityInfo(),
                        name = modrinthProject.title,
                        description = modrinthProject.description,
                        clientSupport = modrinthProject.clientSide.toModSupport(),
                        serverSupport = modrinthProject.serverSide.toModSupport(),
                    )
                }
            Result.success(mods)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun getPreLaunchCommand(launcherInstanceDirectory: File): Result<String?> =
        try {
            val instance = getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            val command = instance.hooks?.preLaunch
            Result.success(command)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun setPreLaunchCommand(
        command: String?,
        launcherInstanceDirectory: File,
    ): Result<Unit> =
        try {
            val instanceConfigFile = getInstanceConfigFile(launcherInstanceDirectory = launcherInstanceDirectory)

            // Since the data class does not have the full properties, and to avoid removing any other properties,
            // we will load it using a json element and modify it using the data class then merge it
            val instanceJsonElement: JsonElement = Json.parseToJsonElement(instanceConfigFile.readText())
            val instance: ModrinthLauncherInstance = JsonIgnoreUnknownKeys.decodeFromJsonElement(instanceJsonElement)

            // TODO The current implementation does not disable
            //  the hooks if the new pre-launch command is null
            //  and the other commands like post-exist command is null
            val newInstance: ModrinthLauncherInstance =
                instance.copy(
                    hooks =
                        if (instance.hooks != null) {
                            instance.hooks.copy(preLaunch = command)
                        } else {
                            ModrinthLauncherInstance.Hooks(
                                preLaunch = command,
                            )
                        },
                )
            val mergedInstance =
                simpleMergeJsonObjects(
                    original = instanceJsonElement.jsonObject,
                    updates = Json.encodeToJsonElement(newInstance).jsonObject,
                )

            instanceConfigFile.writeText(
                text =
                    Json.encodeToString(
                        JsonObject.serializer(),
                        mergedInstance,
                    ),
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
