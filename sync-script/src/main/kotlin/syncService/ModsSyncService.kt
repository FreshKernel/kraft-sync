package syncService

import constants.SyncScriptDotMinecraftFiles
import gui.dialogs.LoadingIndicatorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import syncInfo.models.SyncInfo
import syncInfo.models.getDisplayName
import syncInfo.models.hasValidFileIntegrityOrError
import syncInfo.models.instance
import syncInfo.models.mod.Mod
import syncInfo.models.shouldSyncOnCurrentEnvironment
import syncInfo.models.shouldVerifyFileIntegrity
import syncService.common.AssetSyncService
import utils.ExecutionTimer
import utils.FileDownloader
import utils.Logger
import utils.calculateProgressByIndex
import utils.convertBytesToReadableMegabytesAsString
import utils.deleteExistingOrTerminate
import utils.getFileNameFromUrlOrError
import utils.showErrorMessageAndTerminate
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.name

// TODO: Use JarFile(modFile).manifest.mainAttributes to read the mod name, id and some info to solve the duplicating
//  mods issue when allowing the user to install other mods

class ModsSyncService :
    AssetSyncService(
        assetDirectory = SyncScriptDotMinecraftFiles.Mods.path,
        assetFileExtension = MOD_FILE_EXTENSION,
    ) {
    private val modSyncInfo = SyncInfo.instance.modSyncInfo

    companion object {
        private const val MOD_FILE_EXTENSION = "jar"
    }

    override suspend fun syncData() =
        withContext(Dispatchers.IO) {
            val executionTimer = ExecutionTimer()
            executionTimer.setStartTime()
            Logger.info(extraLine = true) { "\uD83D\uDD04 Syncing mods..." }

            // Mods from the remote
            val mods = modSyncInfo.mods
            Logger.info { "📥 Total received mods from server: ${mods.size}" }

            validateAssetDirectory()
            deleteUnSyncedLocalModFiles(mods = mods)

            val currentEnvironmentModsOrAll = getCurrentEnvironmentModsOrAll(mods = mods)
            Logger.info { "📥 Current environment mods: ${currentEnvironmentModsOrAll.size}" }

            LoadingIndicatorDialog.instance?.updateComponentProperties(
                title = "Syncing Mods...",
                infoText = "In progress",
                progress = null,
                detailsText = null,
            )

            // Download the missing/new mods && remove the modified or the ones that has invalid file integrity

            val modsToDownload =
                getModsForDownloadAndValidateIfRequired(
                    mods = currentEnvironmentModsOrAll,
                )
            Logger.info(extraLine = true) { "🔍 Mods to download: ${modsToDownload.size}" }

            downloadMods(
                modsToDownload = modsToDownload,
                totalMods = currentEnvironmentModsOrAll,
            )

            Logger.info {
                "\uD83D\uDD52 Finished syncing the mods in ${executionTimer.getRunningUntilNowDuration().inWholeMilliseconds}ms."
            }
        }

    private suspend fun deleteUnSyncedLocalModFiles(mods: List<Mod>) {
        // Get only the mods that are created by the script if the admin allows the player to install other mods

        // will or will not remove the mods that are created by the script
        val localModFilePathsToProcess = getScriptLocalModFilePathsOrAll()

        // Delete the old un-synced mods

        val remoteModFileNames: List<String> = mods.map { getModFilePath(it).name }
        for (localModFilePath in localModFilePathsToProcess) {
            if (localModFilePath.name !in remoteModFileNames) {
                Logger.info { "\uD83D\uDEAB Deleting the mod '${localModFilePath.name}' as it's no longer on the server." }
                localModFilePath.deleteExistingOrTerminate(
                    fileEntityType = "mod",
                    reasonOfDelete = "it's no longer on the server",
                )
            }
        }
    }

    private fun getCurrentEnvironmentModsOrAll(mods: List<Mod>): List<Mod> {
        if (!modSyncInfo.syncOnlyModsForCurrentEnv) {
            return mods
        }
        val currentEnvironmentMods =
            mods.filter { mod ->
                if (mod.shouldSyncOnCurrentEnvironment()) {
                    // Include the mod if it should be synced on the current environment.
                    return@filter true
                }

                val modFilePath = getModFilePath(mod)
                if (modFilePath.exists()) {
                    Logger.info { "\uD83D\uDEAB Deleting the mod '${modFilePath.name}' as it's not needed on the current environment." }
                    modFilePath.deleteExistingOrTerminate(
                        fileEntityType = "mod",
                        reasonOfDelete = "it's not required on the current environment",
                    )
                }
                // Exclude the mod as it's not needed in the current environment
                return@filter false
            }
        return currentEnvironmentMods
    }

    private suspend fun getModsForDownloadAndValidateIfRequired(mods: List<Mod>): List<Mod> {
        return mods.filter { mod ->
            val modFileName = getFileNameFromUrlOrError(mod.downloadUrl)
            val modFilePath = getModFilePath(mod)
            if (modFilePath.exists()) {
                if (!mod.shouldVerifyFileIntegrity()) {
                    Logger.info { "ℹ️ The mod: '$modFileName' is set to not be verified. Skipping to the next mod." }
                    return@filter false
                }

                LoadingIndicatorDialog.instance?.updateComponentProperties(
                    title =
                        "Verifying mods",
                    infoText = buildVerifyAssetFileMessage(assetDisplayName = mod.getDisplayName()),
                    progress =
                        mods.calculateProgressByIndex(currentIndex = mods.indexOf(mod)),
                    detailsText =
                        "Verifying the mod files integrity...",
                )
                val hasValidModFileIntegrity = mod.hasValidFileIntegrityOrError(modFilePath)
                if (hasValidModFileIntegrity == null) {
                    Logger.info { "❓ The mod: '$modFileName' has an unknown integrity. Skipping to the next mod." }
                    return@filter false
                }
                if (hasValidModFileIntegrity) {
                    Logger.info { "✅ The mod: '$modFileName' has valid file integrity. Skipping to the next mod." }
                    return@filter false
                }
                Logger.info {
                    "\uD83D\uDEAB The mod: '$modFileName' has invalid integrity. Deleting the mod and downloading it again."
                }
                modFilePath.deleteExistingOrTerminate(
                    fileEntityType = "mod",
                    reasonOfDelete = "it has invalid file integrity",
                )
            }

            // Add this mod to the download list process
            true
        }
    }

    private suspend fun downloadMods(
        modsToDownload: List<Mod>,
        totalMods: List<Mod>,
    ) {
        for ((index, mod) in modsToDownload.withIndex()) {
            val modFileName = getFileNameFromUrlOrError(mod.downloadUrl)
            val modFilePath = getModFilePath(mod)
            if (modFilePath.exists()) {
                Logger.info { "⚠\uFE0F The mod: '$modFileName' already exists." }
            }

            Logger.info { "\uD83D\uDD3D Downloading mod '$modFileName' from ${mod.downloadUrl}" }

            FileDownloader(
                downloadUrl = mod.downloadUrl,
                targetFilePath = modFilePath,
                progressListener = { downloadedBytes, downloadedProgress, bytesToDownload ->
                    LoadingIndicatorDialog.instance?.updateComponentProperties(
                        title =
                            buildProgressMessage(
                                currentIndex = index,
                                pendingCount = modsToDownload.size,
                                totalCount = totalMods.size,
                            ),
                        infoText =
                            buildDownloadAssetFileMessage(assetDisplayName = mod.getDisplayName()),
                        progress = downloadedProgress.toInt(),
                        detailsText =
                            "${downloadedBytes.convertBytesToReadableMegabytesAsString()} MB /" +
                                " ${bytesToDownload.convertBytesToReadableMegabytesAsString()} MB",
                    )
                },
            ).downloadFile()

            // This will always validate newly downloaded mods regardless of the configurations
            val isNewlyDownloadedFileHasValidFileIntegrity = mod.hasValidFileIntegrityOrError(modFilePath)
            if (isNewlyDownloadedFileHasValidFileIntegrity == false) {
                showErrorMessageAndTerminate(
                    title = "❌ File Integrity Check Failed",
                    message =
                        "\uD83D\uDEA8 The newly downloaded file has failed the integrity check. This might be due to a bug " +
                            "in the script \uD83D\uDC1B or an incorrect integrity info value provided by the admin.",
                )
            }
        }
    }

    private fun getModFilePath(mod: Mod): Path =
        getAssetFilePath(
            downloadUrl = mod.downloadUrl,
            fileSyncMarker = modSyncInfo.fileSyncMarker,
        )

    private fun isScriptModFile(modFilePath: Path): Boolean =
        isScriptAssetFile(
            assetFileFilePath = modFilePath,
            fileSyncMarker = modSyncInfo.fileSyncMarker,
        )

    private suspend fun getScriptLocalModFilePathsOrAll(): List<Path> =
        getScriptLocalAssetFilePathsOrAll(
            allowUsingOtherAssets = modSyncInfo.allowUsingOthers,
            isScriptAssetFile = { isScriptModFile(it) },
        )
}
