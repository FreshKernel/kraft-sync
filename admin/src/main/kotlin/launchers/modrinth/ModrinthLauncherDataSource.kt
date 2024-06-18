package launchers.modrinth

import launchers.LauncherDataSource
import syncInfo.models.Mod
import utils.JsonIgnoreUnknownKeys
import java.io.File
import java.nio.file.Paths

class ModrinthLauncherDataSource : LauncherDataSource {
    companion object {
        /**
         * ATLauncher will create a file in the instance directory containing all the info for the instance
         * */
        const val INSTANCE_FILE_NAME = "profile.json"
    }

    private fun getInstance(launcherInstanceDirectory: File): Result<ModrinthLauncherInstance> {
        return try {
            val instanceFile = Paths.get(launcherInstanceDirectory.path, INSTANCE_FILE_NAME).toFile()
            val modrinthLauncherInstance = JsonIgnoreUnknownKeys.decodeFromString<ModrinthLauncherInstance>(instanceFile.readText())
            Result.success(modrinthLauncherInstance)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isValidInstanceDirectory(launcherInstanceDirectory: File): Result<Unit> {
        return try {
            getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isCurseForgeApiRequestNeeded(launcherInstanceDirectory: File): Result<Boolean> {
        return Result.success(false)
    }

    override suspend fun getMods(
        launcherInstanceDirectory: File,
        overrideCurseForgeApiKey: String?,
    ): Result<List<Mod>> {
        return try {
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
    }
}
