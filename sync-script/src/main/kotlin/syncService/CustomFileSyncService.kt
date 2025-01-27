package syncService

import constants.SyncScriptDotMinecraftFiles
import gui.dialogs.LoadingIndicatorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import syncInfo.models.SyncInfo
import syncInfo.models.customFile.CustomFile
import syncInfo.models.hasValidFileIntegrityOrError
import syncInfo.models.instance
import syncInfo.models.shouldVerifyFileIntegrity
import utils.ExecutionTimer
import utils.FileDownloader
import utils.Logger
import utils.calculateProgressByIndex
import utils.convertBytesToReadableMegabytesAsString
import utils.createParentDirectoriesIfDoesNotExist
import utils.deleteExistingOrTerminate
import utils.getFileNameFromUrlOrError
import kotlin.io.path.Path
import kotlin.io.path.exists
import kotlin.io.path.name
import kotlin.io.path.pathString
import kotlin.io.path.readText
import kotlin.io.path.writeText

class CustomFileSyncService : SyncService {
    private val customFileSyncService = SyncInfo.instance.customFileSyncInfo

    override suspend fun syncData() =
        withContext(Dispatchers.IO) {
            if (customFileSyncService == null) {
                return@withContext
            }

            val executionTimer = ExecutionTimer()
            executionTimer.setStartTime()
            Logger.info(extraLine = true) { "\uD83D\uDD04 Syncing custom files..." }

            val customFiles = customFileSyncService.files
            Logger.info { "📥 Total received custom files from server: ${customFiles.size}" }

            deleteRemovedFiles(customFiles)

            LoadingIndicatorDialog.instance?.updateComponentProperties(
                title = "Syncing Custom-files...",
                infoText = "In progress",
                progress = null,
                detailsText = null,
            )

            val customFilesToDownload = getCustomFilesForDownloadAndValidateIfRequired(customFiles)

            Logger.info(extraLine = true) { "🔍 Custom-files to download: ${customFilesToDownload.size}" }

            downloadCustomFiles(customFilesToDownload)

            updateCustomSyncedFiles(customFiles)

            Logger.info {
                "\uD83D\uDD52 Finished syncing the custom-files in ${executionTimer.getRunningUntilNowDuration().inWholeMilliseconds}ms."
            }
        }

    /**
     * Delete custom files that have been created by the script previously, but no longer exist in the list.
     * */
    private fun deleteRemovedFiles(remoteCustomFiles: List<CustomFile>) {
        if (customFileSyncService?.deleteRemovedFiles == false) {
            Logger.info {
                "ℹ️ The server configuration is set to retain removed files. If the server deletes a custom file" +
                        ", it will not be removed locally."
            }
            return
        }
        val customSyncedFilesFilePath = SyncScriptDotMinecraftFiles.SyncScriptData.CustomSyncedFiles.path
        if (!customSyncedFilesFilePath.exists()) {
            Logger.info {
                "ℹ️ The custom synced files does not exist: ${customSyncedFilesFilePath.pathString}." +
                        "Skip deleting the removed files to the next run."
            }
            return
        }

        val currentSyncedFiles =
            Json.decodeFromString<List<CustomFile>>(customSyncedFilesFilePath.readText())

        Logger.info { "ℹ️ The current synced files size: ${currentSyncedFiles.size}" }

        for (localCustomFile in currentSyncedFiles) {
            val customFilePath = localCustomFile.asJavaFilePath

            if (localCustomFile !in remoteCustomFiles) {
                Logger.info {
                    "\uD83D\uDEAB Deleting the custom file '${customFilePath.name}' as it no longer " +
                            "exists in the remote custom files list."
                }
                customFilePath.deleteExistingOrTerminate(
                    fileEntityType = "custom",
                    reasonOfDelete = "it's no longer on the server",
                )
            }
        }
    }

    private suspend fun getCustomFilesForDownloadAndValidateIfRequired(customFiles: List<CustomFile>) =
        customFiles.filter { customFile ->
            val customFileName = getFileNameFromUrlOrError(customFile.downloadUrl)
            val customFilePath = customFile.asJavaFilePath

            if (customFilePath.exists()) {
                if (!customFile.shouldVerifyFileIntegrity()) {
                    Logger.info {
                        "ℹ️ The custom-file: '$customFileName' is set to not be verified. Skipping to the next custom-file."
                    }
                    return@filter false
                }

                LoadingIndicatorDialog.instance?.updateComponentProperties(
                    title =
                        "Verifying custom-files",
                    infoText = buildVerifyFileMessage(fileDisplayName = customFileName),
                    progress =
                        customFiles.calculateProgressByIndex(currentIndex = customFiles.indexOf(customFile)),
                    detailsText =
                        "Verifying the custom files integrity...",
                )

                val hasValidCustomFileIntegrity = customFile.hasValidFileIntegrityOrError(customFilePath)
                if (hasValidCustomFileIntegrity == null) {
                    Logger.info { "❓ The custom-file: '$customFileName' has an unknown integrity. Skipping to the next custom-file." }
                    return@filter false
                }
                if (hasValidCustomFileIntegrity) {
                    Logger.info { "✅ The custom-file: '$customFileName' has valid file integrity. Skipping to the next custom-file." }
                    return@filter false
                }
                Logger.info {
                    "\uD83D\uDEAB The custom-file: '$customFileName' has invalid integrity. Deleting the file and downloading it again."
                }
                customFilePath.deleteExistingOrTerminate(
                    fileEntityType = "custom-file",
                    reasonOfDelete = "it has invalid file integrity",
                )
            }
            // Add this custom-file to the download list.
            true
        }

    private suspend fun downloadCustomFiles(customFiles: List<CustomFile>) {
        for ((index, customFile) in customFiles.withIndex()) {
            val customFileName = getFileNameFromUrlOrError(customFile.downloadUrl)
            val customFilePath = Path(customFile.filePath)

            if (customFilePath.exists()) {
                Logger.warning { "⚠\uFE0F The custom-file: '$customFileName' already exists." }
            }

            Logger.info { "\uD83D\uDD3D Downloading custom-file '$customFileName' from ${customFile.downloadUrl}" }

            // Important so the FileDownloader can create the file.
            customFilePath.createParentDirectoriesIfDoesNotExist()

            FileDownloader(
                downloadUrl = customFile.downloadUrl,
                targetFilePath = customFilePath,
                progressListener = { downloadedBytes, downloadedProgress, bytesToDownload ->
                    LoadingIndicatorDialog.instance?.updateComponentProperties(
                        title =
                            buildProgressMessage(
                                currentIndex = index,
                                pendingCount = customFiles.size,
                                totalCount = customFiles.size,
                            ),
                        infoText =
                            buildDownloadFileMessage(fileDisplayName = customFileName),
                        progress = downloadedProgress.toInt(),
                        detailsText =
                            "${downloadedBytes.convertBytesToReadableMegabytesAsString()} MB /" +
                                    " ${bytesToDownload.convertBytesToReadableMegabytesAsString()} MB",
                    )
                },
            ).downloadFile(fileEntityType = "Custom")
        }
    }

    private fun updateCustomSyncedFiles(customFiles: List<CustomFile>) {
        val filePath = SyncScriptDotMinecraftFiles.SyncScriptData.CustomSyncedFiles.path
        filePath.writeText(Json.encodeToString<List<CustomFile>>(customFiles))
    }

    private val CustomFile.asJavaFilePath
        get() = Path(this.filePath)
}
