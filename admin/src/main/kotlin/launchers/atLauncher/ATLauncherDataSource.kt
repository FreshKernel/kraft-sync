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

    override suspend fun validateInstanceDirectory(launcherInstanceDirectory: File): Result<Unit> =
        try {
            getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    private fun isCurseForgeApiRequestNeededForMod(atLauncherMod: ATLauncherInstance.Launcher.Mod): Boolean {
        val modrinthFile = atLauncherMod.modrinthVersion?.files?.firstOrNull()
        return modrinthFile == null && (atLauncherMod.curseForgeProjectId != null && atLauncherMod.curseForgeFileId != null)
    }

    override suspend fun isCurseForgeApiRequestNeeded(launcherInstanceDirectory: File): Result<Boolean> =
        try {
            val instance = getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            val isCurseForgeApiRequestNeeded =
                getATLauncherMods(instance).any { atLauncherMod ->
                    isCurseForgeApiRequestNeededForMod(atLauncherMod = atLauncherMod)
                }
            Result.success(isCurseForgeApiRequestNeeded)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    override suspend fun getMods(
        launcherInstanceDirectory: File,
        curseForgeApiKeyOverride: String?,
    ): Result<List<Mod>> =
        try {
            val instance = getInstance(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            val mods =
                instance.launcher.mods
                    .filter { it.type == ATLauncherInstance.Launcher.Mod.Type.Mods }
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
                        if (isCurseForgeApiRequestNeededForMod(atLauncherMod = atLauncherMod)) {
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
}
