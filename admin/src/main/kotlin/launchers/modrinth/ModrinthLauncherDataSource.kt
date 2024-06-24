package launchers.modrinth

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import launchers.LauncherDataSource
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

    private fun getInstanceFile(launcherInstanceDirectory: File) = Paths.get(launcherInstanceDirectory.path, INSTANCE_FILE_NAME).toFile()

    private fun getInstance(launcherInstanceDirectory: File): Result<ModrinthLauncherInstance> =
        try {
            val instanceFile = getInstanceFile(launcherInstanceDirectory = launcherInstanceDirectory)
            val modrinthLauncherInstance =
                JsonIgnoreUnknownKeys.decodeFromString<ModrinthLauncherInstance>(instanceFile.readText())
            Result.success(modrinthLauncherInstance)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun validateInstanceDirectory(launcherInstanceDirectory: File): Result<Unit> =
        try {
            getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun isCurseForgeApiRequestNeeded(launcherInstanceDirectory: File): Result<Boolean> = Result.success(false)

    override suspend fun getMods(
        launcherInstanceDirectory: File,
        curseForgeApiKeyOverride: String?,
    ): Result<List<Mod>> =
        try {
            val instance = getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            val mods =
                instance.projects.map { (_, project) ->
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
            val instanceFile = getInstanceFile(launcherInstanceDirectory = launcherInstanceDirectory)

            // Since the data class does not have the full properties, and to avoid removing any other properties,
            // we will load it using a json element and modify it using the data class then merge it
            val instanceJsonElement: JsonElement = Json.parseToJsonElement(instanceFile.readText())
            val instance: ModrinthLauncherInstance = JsonIgnoreUnknownKeys.decodeFromJsonElement(instanceJsonElement)
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

            instanceFile.writeText(
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
