package syncService

import constants.SyncScriptDotMinecraftFiles
import gui.dialogs.LoadingIndicatorDialog
import services.minecraft.MinecraftOptionsManager
import syncInfo.models.SyncInfo
import syncInfo.models.getDisplayName
import syncInfo.models.hasValidFileIntegrityOrError
import syncInfo.models.instance
import syncInfo.models.resourcePack.ResourcePack
import syncInfo.models.resourcePack.ResourcePackSyncInfo
import syncInfo.models.shouldVerifyFileIntegrity
import utils.ExecutionTimer
import utils.FileDownloader
import utils.calculateProgressByIndex
import utils.convertBytesToReadableMegabytesAsString
import utils.deleteExistingOrTerminate
import utils.getFileNameFromUrlOrError
import utils.listFilteredPaths
import utils.showErrorMessageAndTerminate
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.isHidden
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.system.exitProcess

// TODO: Extract the common code between ResourcePacksSyncService and ModsSyncService

class ResourcePacksSyncService : SyncService {
    private val resourcePacksDirectoryPath = SyncScriptDotMinecraftFiles.ResourcePacks.path
    private val resourcePackSyncInfo = SyncInfo.instance.resourcePackSyncInfo

    companion object {
        private const val RESOURCE_PACK_FILE_EXTENSION = "zip"
    }

    override suspend fun syncData() {
        val executionTimer = ExecutionTimer()
        executionTimer.setStartTime()

        println("\n\uD83D\uDD04 Syncing resource-packs...")

        // Resource packs from the remote
        val resourcePacks = resourcePackSyncInfo.resourcePacks
        println("📥 Total received resource-packs from server: ${resourcePacks.size}")

        validateResourcePacksDirectory()
        deleteUnSyncedLocalResourcePackFiles(resourcePacks = resourcePacks)

        val loadingIndicatorDialog: LoadingIndicatorDialog? =
            LoadingIndicatorDialog.createIfGuiEnabled("Syncing resource-packs...")
        loadingIndicatorDialog?.isVisible = true

        val resourcePacksToDownload =
            getResourcePacksForDownloadAndValidateIfRequired(
                resourcePacks = resourcePacks,
                loadingIndicatorDialog = loadingIndicatorDialog,
            )
        println("\n🔍 Resource Packs to download: ${resourcePacksToDownload.size}")

        downloadResourcePacks(
            resourcePacksToDownload = resourcePacksToDownload,
            totalResourcePacks = resourcePacks,
            loadingIndicatorDialog = loadingIndicatorDialog,
        )

        if (resourcePackSyncInfo.shouldApplyResourcePacks) {
            applyResourcePacks()
        }

        loadingIndicatorDialog?.isVisible = false

        println("\uD83D\uDD52 Finished syncing the resource-packs in ${executionTimer.getRunningUntilNowDuration().inWholeMilliseconds}ms.")
    }

    private fun validateResourcePacksDirectory() {
        if (!resourcePacksDirectoryPath.exists()) {
            println("\uD83D\uDCC1 The resource-packs folder doesn't exist, creating it..")
            resourcePacksDirectoryPath.createDirectories()
        }

        if (!resourcePacksDirectoryPath.isDirectory()) {
            showErrorMessageAndTerminate(
                title = "❌ Invalid Resource-Packs Folder",
                message =
                    "\uD83D\uDEE0 Resource Packs must be stored in a directory/folder \uD83D\uDCC2 called " +
                        "`${SyncScriptDotMinecraftFiles.ResourcePacks.path.name}`" +
                        ", a file was found instead.",
            )
        }
    }

    private suspend fun deleteUnSyncedLocalResourcePackFiles(resourcePacks: List<ResourcePack>) {
        // Get only the resource-packs that are created by the script
        // if the admin allows the player to install other resource-packs

        // will or will not remove the resource-packs that are created by the script
        val localResourcePackFilePathsToProcess =
            getScriptLocalResourcePackFilePathsOrAll()

        val remoteResourcePackFileNames: List<String> = resourcePacks.map { getResourcePackFilePath(it).name }
        for (localResourcePackFilePath in localResourcePackFilePathsToProcess) {
            if (localResourcePackFilePath.name !in remoteResourcePackFileNames) {
                println("\uD83D\uDEAB Deleting the resource-pack '${localResourcePackFilePath.name}' as it's no longer on the server.")
                localResourcePackFilePath.deleteExistingOrTerminate(
                    fileEntityType = "resource-pack",
                    reasonOfDelete = "it's no longer on the server",
                )
            }
        }
    }

    private suspend fun getResourcePacksForDownloadAndValidateIfRequired(
        resourcePacks: List<ResourcePack>,
        loadingIndicatorDialog: LoadingIndicatorDialog?,
    ): List<ResourcePack> =
        resourcePacks.filter { resourcePack ->
            val resourcePackFileName = getFileNameFromUrlOrError(resourcePack.downloadUrl)
            val resourcePackFilePath = getResourcePackFilePath(resourcePack)
            if (resourcePackFilePath.exists()) {
                if (!resourcePack.shouldVerifyFileIntegrity()) {
                    println("ℹ️ The resource-pack: '$resourcePackFileName' is set to not be verified. Skipping to the next resource-pack.")
                    return@filter false
                }

                loadingIndicatorDialog?.updateComponentProperties(
                    title =
                        "Verifying resource-packs",
                    infoText = "Verifying ${resourcePack.getDisplayName()}",
                    progress =
                        resourcePacks.calculateProgressByIndex(currentIndex = resourcePacks.indexOf(resourcePack)),
                    detailsText =
                        "Verifying the resource-pack files integrity...",
                )
                val hasValidResourcePackFileIntegrity = resourcePack.hasValidFileIntegrityOrError(resourcePackFilePath)
                if (hasValidResourcePackFileIntegrity == null) {
                    println("❓ The resource-pack: '$resourcePackFileName' has an unknown integrity. Skipping to the next resource-pack.")
                    return@filter false
                }
                if (hasValidResourcePackFileIntegrity) {
                    println("✅ The resource-pack: '$resourcePackFileName' has valid file integrity. Skipping to the next resource-pack.")
                    return@filter false
                }
                println(
                    "❌ The resource-pack: '$resourcePackFileName' has invalid integrity. Deleting the resource-pack " +
                        "and downloading it again.",
                )
                resourcePackFilePath.deleteExistingOrTerminate(
                    fileEntityType = "resource-pack",
                    reasonOfDelete = "it has invalid file integrity",
                )
            }

            // Add this resource-pack to the download list process
            true
        }

    private suspend fun downloadResourcePacks(
        resourcePacksToDownload: List<ResourcePack>,
        totalResourcePacks: List<ResourcePack>,
        loadingIndicatorDialog: LoadingIndicatorDialog?,
    ) {
        for ((index, resourcePack) in resourcePacksToDownload.withIndex()) {
            val resourcePackFileName = getFileNameFromUrlOrError(resourcePack.downloadUrl)
            val resourcePackFilePath = getResourcePackFilePath(resourcePack)
            if (resourcePackFilePath.exists()) {
                println("⚠\uFE0F The resource-pack: '$resourcePackFileName' already exists.")
            }

            println("\uD83D\uDD3D Downloading resource-pack '$resourcePackFileName' from ${resourcePack.downloadUrl}")

            FileDownloader(
                downloadUrl = resourcePack.downloadUrl,
                targetFilePath = resourcePackFilePath,
                progressListener = { downloadedBytes, downloadedProgress, bytesToDownload ->
                    loadingIndicatorDialog?.updateComponentProperties(
                        title =
                            buildTitleMessage(
                                currentResourcePackIndex = index,
                                pendingResourcePacksCount = resourcePacksToDownload.size,
                                totalResourcePacksCount = totalResourcePacks.size,
                            ),
                        infoText = "Downloading ${resourcePack.getDisplayName()}",
                        progress = downloadedProgress.toInt(),
                        detailsText =
                            "${downloadedBytes.convertBytesToReadableMegabytesAsString()} MB /" +
                                " ${bytesToDownload.convertBytesToReadableMegabytesAsString()} MB",
                    )
                },
            ).downloadFile()

            // This will always validate newly downloaded resource-packs regardless of the configurations
            val isNewlyDownloadedFileHasValidFileIntegrity =
                resourcePack.hasValidFileIntegrityOrError(resourcePackFilePath)
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

    private suspend fun applyResourcePacks() {
        val resourcePackFilePaths = getScriptLocalResourcePackFilePathsOrAll()

        MinecraftOptionsManager.loadPropertiesFromFile(createIfMissing = true)

        val optionsResourcePacks: List<MinecraftOptionsManager.ResourcePack>? =
            MinecraftOptionsManager.readResourcePacks()
        val builtInOptionsResourcePacks =
            optionsResourcePacks
                ?.filterIsInstance<MinecraftOptionsManager.ResourcePack.BuiltIn>()

        MinecraftOptionsManager.setResourcePacks(
            resourcePacks =
                buildList {
                    builtInOptionsResourcePacks?.let { addAll(it) }
                    if (resourcePackSyncInfo.allowUsingOtherResourcePacks) {
                        val userOptionsResourcePacks =
                            optionsResourcePacks?.filter {
                                it is MinecraftOptionsManager.ResourcePack.File &&
                                    !isScriptResourcePackFile(Paths.get(it.resourcePackZipFileName))
                            }
                        userOptionsResourcePacks?.let { addAll(userOptionsResourcePacks) }
                    }
                    addAll(
                        resourcePackFilePaths
                            .map { MinecraftOptionsManager.ResourcePack.File(it.name) },
                    )
                },
        )
    }

    /**
     * @return The file that will be used, we use [ResourcePackSyncInfo.fileSyncMarker] to support [isScriptResourcePackFile]
     * will be the same file name from the [ResourcePack.downloadUrl] if [ResourcePackSyncInfo.fileSyncMarker] is null
     *
     * @see getResourcePackFilePath
     * */
    private fun getResourcePackFilePath(resourcePack: ResourcePack): Path {
        val resourcePackFileNameWithoutExtension =
            Paths.get(getFileNameFromUrlOrError(resourcePack.downloadUrl)).nameWithoutExtension
        val resourcePackFileName =
            buildString {
                append(resourcePackFileNameWithoutExtension)
                resourcePackSyncInfo.fileSyncMarker?.let { append(it) }
                append(".$RESOURCE_PACK_FILE_EXTENSION")
            }
        return resourcePacksDirectoryPath.resolve(resourcePackFileName)
    }

    /**
     * @return if this resource-pack is created/synced by the script
     * it will be identified by [ResourcePackSyncInfo.fileSyncMarker] and will always return true
     * if [ResourcePackSyncInfo.fileSyncMarker] is null
     *
     * @see getResourcePackFilePath
     * */
    private fun isScriptResourcePackFile(resourcePackFilePath: Path): Boolean =
        resourcePackFilePath.name.endsWith(
            "${resourcePackSyncInfo.fileSyncMarker.orEmpty()}.$RESOURCE_PACK_FILE_EXTENSION",
        )

    /**
     * @return The message that will be used for the dialog that will show the progress
     * of syncing, downloading and verifying resource-packs
     * */
    private fun buildTitleMessage(
        currentResourcePackIndex: Int,
        pendingResourcePacksCount: Int,
        totalResourcePacksCount: Int,
    ): String =
        buildString {
            append("${currentResourcePackIndex + 1} of $pendingResourcePacksCount")
            if (pendingResourcePacksCount != totalResourcePacksCount) {
                append(" ($totalResourcePacksCount total)")
            }
        }

    /**
     * The resource-packs to deal with based on [ResourcePackSyncInfo.allowUsingOtherResourcePacks]
     * @return Either all the resource pack files or only the ones created by the script
     * */
    private suspend fun getScriptLocalResourcePackFilePathsOrAll(): List<Path> {
        return resourcePacksDirectoryPath
            .listFilteredPaths {
                val isResourcePackFile =
                    !it.isDirectory() && !it.isHidden() && it.extension == RESOURCE_PACK_FILE_EXTENSION
                if (resourcePackSyncInfo.allowUsingOtherResourcePacks) {
                    return@listFilteredPaths isResourcePackFile && isScriptResourcePackFile(it)
                } else {
                    return@listFilteredPaths isResourcePackFile
                }
            }.getOrElse {
                showErrorMessageAndTerminate(
                    title = "📁 File Listing Error",
                    message = "⚠ Failed to list the files in the resource-packs folder: ${it.message}",
                )
                // This will never reach due to the previous statement stopping the application
                exitProcess(1)
            }
    }
}
