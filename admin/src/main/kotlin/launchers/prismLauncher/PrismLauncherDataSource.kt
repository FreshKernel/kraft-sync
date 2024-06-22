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
    }

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

    override suspend fun isValidInstanceDirectory(launcherInstanceDirectory: File): Result<Unit> =
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
        overrideCurseForgeApiKey: String?,
    ): Result<List<Mod>> =
        try {
            // TODO: use File.exist(), check of File.exist() might need to be shared to avoid duplications
            val prismLauncherModsMetadata =
                getPrismLauncherModsMetadata(launcherInstanceDirectory = launcherInstanceDirectory).getOrThrow()
            val mods =
                prismLauncherModsMetadata.map { prismLauncherModMetadata ->
                    var modDownloadUrl = prismLauncherModMetadata.download.url
                    if (isCurseForgeApiRequestNeededForMod(prismLauncherModMetadata = prismLauncherModMetadata)) {
                        // The mod download URL is null though not null

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
                                    overrideApiKey = overrideCurseForgeApiKey,
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
}
