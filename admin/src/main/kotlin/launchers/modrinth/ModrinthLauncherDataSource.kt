package launchers.modrinth

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import launchers.Instance
import launchers.LauncherDataSource
import launchers.modrinth.ModrinthLauncherInstance.ModrinthLauncherProject
import syncInfo.models.Mod
import utils.JsonIgnoreUnknownKeys
import utils.SystemFileProvider
import utils.listFilteredPaths
import utils.simpleMergeJsonObjects
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.isHidden
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

class ModrinthLauncherDataSource : LauncherDataSource {
    companion object {
        /**
         * Modrinth Launcher will create a file in the instance directory containing all the info for the instance
         * */
        const val INSTANCE_FILE_NAME = "profile.json"
    }

    private fun getInstanceConfigFilePath(launcherInstanceDirectoryPath: Path): Path =
        launcherInstanceDirectoryPath.resolve(INSTANCE_FILE_NAME)

    private fun getInstance(launcherInstanceDirectoryPath: Path): Result<ModrinthLauncherInstance> =
        try {
            val instanceConfigFilePath =
                getInstanceConfigFilePath(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath)
            val modrinthLauncherInstance =
                JsonIgnoreUnknownKeys.decodeFromString<ModrinthLauncherInstance>(instanceConfigFilePath.readText())
            Result.success(modrinthLauncherInstance)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun validateInstanceDirectory(launcherInstanceDirectoryPath: Path): Result<Unit> {
        return try {
            val instanceConfigFilePath =
                getInstanceConfigFilePath(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath)
            if (!instanceConfigFilePath.exists()) {
                return Result.failure(
                    IllegalArgumentException("The file (${instanceConfigFilePath.absolutePathString()}) does not exist."),
                )
            }
            if (!instanceConfigFilePath.isRegularFile()) {
                return Result.failure(
                    IllegalArgumentException("The file (${instanceConfigFilePath.absolutePathString()}) should be a file."),
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isCurseForgeApiRequestNeededForConvertingMods(launcherInstanceDirectoryPath: Path): Result<Boolean> =
        Result.success(false)

    private fun getModrinthLauncherProjects(instance: ModrinthLauncherInstance): Map<String, ModrinthLauncherProject> = instance.projects

    override suspend fun hasMods(launcherInstanceDirectoryPath: Path): Result<Boolean> {
        return try {
            val instanceConfigFilePath =
                getInstanceConfigFilePath(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath)
            if (!instanceConfigFilePath.exists()) {
                return Result.failure(
                    IllegalArgumentException("The file (${instanceConfigFilePath.absolutePathString()}) does not exist."),
                )
            }
            if (!instanceConfigFilePath.isRegularFile()) {
                return Result.failure(
                    IllegalArgumentException("The file (${instanceConfigFilePath.absolutePathString()}) should be a file."),
                )
            }
            val mods =
                getModrinthLauncherProjects(
                    instance =
                        getInstance(
                            launcherInstanceDirectoryPath = launcherInstanceDirectoryPath,
                        ).getOrThrow(),
                )
            Result.success(mods.isNotEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLauncherInstanceMods(
        launcherInstanceDirectoryPath: Path,
        overrideCurseForgeApiKey: String?,
    ): Result<List<Mod>> =
        try {
            val instance = getInstance(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath).getOrThrow()
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

    override suspend fun getPreLaunchCommand(launcherInstanceDirectoryPath: Path): Result<String?> =
        try {
            val instance = getInstance(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath).getOrThrow()
            val command = instance.hooks?.preLaunch
            Result.success(command)
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

            // Since the data class does not have the full properties, and to avoid removing any other properties,
            // we will load it using a json element and modify it using the data class then merge it
            val instanceJsonElement: JsonElement = Json.parseToJsonElement(instanceConfigFilePath.readText())
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

            instanceConfigFilePath.writeText(
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

    override suspend fun getInstances(): Result<List<Instance>?> =
        try {
            val directoryPath =
                SystemFileProvider
                    .getUserApplicationDataDirectory(
                        applicationDirectoryName = "com.modrinth.theseus",
                    ).getOrThrow()
            val instances =
                directoryPath
                    ?.resolve("profiles")
                    ?.listFilteredPaths { path ->
                        path.isDirectory() && !path.isHidden()
                    }?.getOrThrow()
                    ?.map {
                        Instance(
                            launcherInstanceDirectoryPath = it,
                            instanceName = it.name,
                        )
                    }
            Result.success(instances)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
