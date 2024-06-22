package launchers.atLauncher

import curseForgeDataSource
import launchers.LauncherDataSource
import syncInfo.models.Mod
import utils.JsonIgnoreUnknownKeys
import java.io.File
import java.nio.file.Paths

class ATLauncherDataSource : LauncherDataSource {
    companion object {
        /**
         * ATLauncher will create a file in the instance directory containing all the info for the instance
         * */
        const val INSTANCE_FILE_NAME = "instance.json"
    }

    private fun getInstance(launcherInstanceDirectory: File): Result<ATLauncherInstance> {
        return try {
            val instanceFile = Paths.get(launcherInstanceDirectory.path, INSTANCE_FILE_NAME).toFile()
            val atLauncherInstance = JsonIgnoreUnknownKeys.decodeFromString<ATLauncherInstance>(instanceFile.readText())
            return Result.success(atLauncherInstance)
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
    private fun getATLauncherMods(instance: ATLauncherInstance): List<ATLauncherInstance.Launcher.Mod> =
        instance.launcher.mods
            .filter { it.type == ATLauncherInstance.Launcher.Mod.Type.Mods }

    override suspend fun isValidInstanceDirectory(launcherInstanceDirectory: File): Result<Unit> =
        try {
            getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    override suspend fun isCurseForgeApiRequestNeeded(launcherInstanceDirectory: File): Result<Boolean> =
        try {
            val instance = getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            val isCurseForgeApiRequestNeeded =
                getATLauncherMods(instance).any { atLauncherMod ->
                    atLauncherMod.modrinthVersion.files.firstOrNull() == null
                }
            Result.success(isCurseForgeApiRequestNeeded)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    // TODO: Needs to be tested with Curse Forge and Modrinth with resource-packs at the same time, also convert/import more data like description field
    override suspend fun getMods(
        launcherInstanceDirectory: File,
        overrideCurseForgeApiKey: String?,
    ): Result<List<Mod>> =
        try {
            val instance = getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            val mods =
                instance.launcher.mods
                    .filter { it.type == ATLauncherInstance.Launcher.Mod.Type.Mods }
                    .map { atLauncherMod ->
                        val modrinthFile = atLauncherMod.modrinthVersion.files.firstOrNull()
                        var downloadUrl = modrinthFile?.url
                        var fileIntegrityInfo = modrinthFile?.getFileIntegrityInfo()

                        if (modrinthFile == null) {
                            // ATLauncher always store the info for both CurseForge and Modrinth even if it's downloaded
                            // from CurseForge, we will use Curse Forge instead if the file doesn't exist for some reason
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

                        // ATLauncher seems to always store the Modrinth project,
                        // regardless of whether it's downloaded from CurseForge or Modrinth.
                        val modrinthProject = atLauncherMod.modrinthProject

                        Mod(
                            downloadUrl = downloadUrl,
                            fileIntegrityInfo = fileIntegrityInfo,
                            clientSupport = modrinthProject.clientSide.toModSupport(),
                            serverSupport = modrinthProject.serverSide.toModSupport(),
                            name = modrinthProject.title,
                            description = modrinthProject.description,
                        )
                    }
            Result.success(mods)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
