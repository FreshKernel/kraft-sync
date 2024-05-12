package syncService

import Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import syncInfo.models.SyncInfo
import useGui
import util.FileDownloader
import util.convertBytesToMb
import util.getFileNameFromUrl
import util.gui.LoadingIndicatorWindow
import util.prettyPrintList
import util.showErrorMessage
import java.io.File

class SyncModsService : SyncService(Constants.MinecraftInstanceFiles.Mods.file) {
    private val modsFolder = folder

    override suspend fun syncContents(syncInfo: SyncInfo) =
        withContext(Dispatchers.IO) {
            println("\uD83D\uDD04 Syncing mods...")

            // 1. Check if the mods exist, create the mods folder if not, then make sure it's a directory

            if (!modsFolder.exists()) {
                println("\uD83D\uDCC1 The mods folder doesn't exist, creating it..")
                modsFolder.mkdirs()
            }

            if (!modsFolder.isDirectory) {
                modsFolder.isFile
                showErrorMessage(
                    title = "Invalid Mods Folder",
                    message = "Mods must be stored in a directory, but a file was found instead.",
                )
            }

            // 2. List the current files inside the mods folder, print it to the log
            val modFiles =
                modsFolder.listFiles() ?: kotlin.run {
                    showErrorMessage(
                        title = "File Listing Error",
                        message = "Failed to list the files in the mods folder.",
                    )
                    return@withContext
                }
            prettyPrintList(modFiles.map { it.name }, "Current Mods:")

            // 3. Print the download url of each mod from the server
            prettyPrintList(syncInfo.mods.map { it.url }, "Server Mod Download URLs:")

            // 4. Get the name of each mod from the server and print them

            // TODO: Extract function that build the jar file name
            val modSyncMarker: String? = syncInfo.modSyncMarker

            val modsFileNames =
                syncInfo.mods.map {
                    val modFileNameWithoutExtension =
                        File(
                            getFileNameFromUrl(it.url) ?: kotlin.run {
                                showErrorMessage(
                                    title = "File Name Retrieval Error",
                                    message = "Failed to retrieve the file name from the download URL: ${it.url}",
                                )
                                return@withContext
                            },
                        ).nameWithoutExtension
                    "${modFileNameWithoutExtension}${modSyncMarker.orEmpty()}.jar"
                }

            prettyPrintList(modsFileNames, "Server Mod Names:")

            // 5. Get only the mods that is created by the script if the admin allow the player to install other mods

            /**
             * The mods to deal based on [SyncInfo.allowUsingOtherMods]
             * */
            val processMods =
                if (syncInfo.allowUsingOtherMods) {
                    modFiles.filter {
                        it.name.endsWith(
                            "${modSyncMarker.orEmpty()}.jar",
                        )
                    }
                } else {
                    modFiles.toList()
                }

            // 6. Delete the old un-synced mods
            for (modFile in processMods) {
                if (!modsFileNames.contains(modFile.name)) {
                    println("❌ Deleting the mod '${modFile.name}' as it's no longer on the server.")
                    modFile.delete()
                }
            }

            // 7. Download the new mods && remove the modified or the mods that has invalid integrity

            val loadingIndicatorWindow = LoadingIndicatorWindow("Syncing Mods...")
            if (useGui) {
                loadingIndicatorWindow.showWindow()
            }

            // TODO: Complete this
//            val modsToDownload =
//                syncInfo.mods.filter {
//                    true
//                }

            for ((index, mod) in syncInfo.mods.withIndex()) {
                val modFileName =
                    getFileNameFromUrl(mod.url) ?: kotlin.run {
                        // TODO: Extract common code between above
                        showErrorMessage(
                            title = "File Name Retrieval Error",
                            message = "Failed to retrieve the file name from the download URL: ${mod.url}",
                        )
                        return@withContext
                    }
                val modFile =
                    File(modsFolder, "${File(modFileName).nameWithoutExtension}${modSyncMarker.orEmpty()}.jar")
                // TODO: Make use of syncInfo.verifyModsIntegrity
                if (modFile.exists()) {
                    val hasValidModIntegrity = mod.fileIntegrityInfo.hasValidIntegrity(modFile)
                    if (hasValidModIntegrity == null) {
                        println("❓ The mod: $modFileName has an unknown integrity. Skipping to the next mod.")
                        continue
                    }
                    if (hasValidModIntegrity) {
                        println("✅ The mod: $modFileName has valid integrity. Skipping to the next mod.")
                        continue
                    }
                    println(
                        "❌ The mod: $modFileName has invalid integrity. Deleting the mod " +
                            "and downloading it again.",
                    )
                    modFile.delete()
                }
                val isCreateSuccessful = modFile.createNewFile()
                if (!isCreateSuccessful) {
                    println("⚠\uFE0F The mod: $modFileName already exists.")
                }
                if (!modFile.canWrite()) {
                    loadingIndicatorWindow.hideWindow()
                    showErrorMessage(
                        title = "Permission Error",
                        message = "Missing write permission for the mod: ${modFile.path}",
                    )
                }

                println("\uD83D\uDD3D Downloading mod $modFileName from ${mod.url}")

                FileDownloader(
                    mod.url,
                    modFile,
                    progressListener = { downloadedBytes, downloadedProgress, bytesToDownload ->
                        loadingIndicatorWindow.updateProgress(
                            titleLabelText = "Downloading ${mod.getDisplayName()}",
                            progress = downloadedProgress,
                            infoLabelText =
                                "${downloadedBytes.convertBytesToMb()} MB /" +
                                    " ${bytesToDownload.convertBytesToMb()} MB",
                            // TODO: Bug, This will show the total mods and not the mods to sync
                            title = "${index + 1} of ${syncInfo.mods.size}",
                        )
                    },
                ).downloadFile()
            }

            loadingIndicatorWindow.hideWindow()
        }
}
