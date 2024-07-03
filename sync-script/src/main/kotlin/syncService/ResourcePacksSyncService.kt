package syncService

import constants.SyncScriptDotMinecraftFiles
import gui.dialogs.LoadingIndicatorDialog
import services.minecraft.MinecraftOptionsManager
import syncInfo.models.SyncInfo
import syncInfo.models.getDisplayName
import syncInfo.models.hasValidFileIntegrityOrError
import syncInfo.models.instance
import syncInfo.models.resourcePack.ResourcePack
import syncInfo.models.shouldVerifyFileIntegrity
import syncService.common.AssetSyncService
import utils.ExecutionTimer
import utils.FileDownloader
import utils.calculateProgressByIndex
import utils.convertBytesToReadableMegabytesAsString
import utils.deleteExistingOrTerminate
import utils.getFileNameFromUrlOrError
import utils.showErrorMessageAndTerminate
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.name

class ResourcePacksSyncService :
    AssetSyncService(
        assetDirectory = SyncScriptDotMinecraftFiles.ResourcePacks.path,
        assetFileExtension = RESOURCE_PACK_FILE_EXTENSION,
    ) {
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

        validateAssetDirectory()
        deleteUnSyncedLocalResourcePackFiles(resourcePacks = resourcePacks)

        LoadingIndicatorDialog.instance?.updateComponentProperties(
            title = "Syncing Resource Packs...",
            infoText = "In progress",
            progress = null,
            detailsText = null,
        )

        val resourcePacksToDownload =
            getResourcePacksForDownloadAndValidateIfRequired(
                resourcePacks = resourcePacks,
            )
        println("\n🔍 Resource Packs to download: ${resourcePacksToDownload.size}")

        downloadResourcePacks(
            resourcePacksToDownload = resourcePacksToDownload,
            totalResourcePacks = resourcePacks,
        )

        if (resourcePackSyncInfo.shouldApplyResourcePacks) {
            applyResourcePacks()
        }

        println("\uD83D\uDD52 Finished syncing the resource-packs in ${executionTimer.getRunningUntilNowDuration().inWholeMilliseconds}ms.")
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

    private suspend fun getResourcePacksForDownloadAndValidateIfRequired(resourcePacks: List<ResourcePack>): List<ResourcePack> =
        resourcePacks.filter { resourcePack ->
            val resourcePackFileName = getFileNameFromUrlOrError(resourcePack.downloadUrl)
            val resourcePackFilePath = getResourcePackFilePath(resourcePack)
            if (resourcePackFilePath.exists()) {
                if (!resourcePack.shouldVerifyFileIntegrity()) {
                    println("ℹ️ The resource-pack: '$resourcePackFileName' is set to not be verified. Skipping to the next resource-pack.")
                    return@filter false
                }

                LoadingIndicatorDialog.instance?.updateComponentProperties(
                    title =
                        "Verifying Resource Packs",
                    infoText = buildVerifyAssetFileMessage(assetDisplayName = resourcePack.getDisplayName()),
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
                    LoadingIndicatorDialog.instance?.updateComponentProperties(
                        title =
                            buildProgressMessage(
                                currentIndex = index,
                                pendingCount = resourcePacksToDownload.size,
                                totalCount = totalResourcePacks.size,
                            ),
                        infoText = buildDownloadAssetFileMessage(assetDisplayName = resourcePack.getDisplayName()),
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

    private fun getResourcePackFilePath(resourcePack: ResourcePack): Path =
        getAssetFilePath(
            downloadUrl = resourcePack.downloadUrl,
            fileSyncMarker = resourcePackSyncInfo.fileSyncMarker,
        )

    private fun isScriptResourcePackFile(resourcePackFilePath: Path): Boolean =
        isScriptAssetFile(
            assetFileFilePath = resourcePackFilePath,
            fileSyncMarker = resourcePackSyncInfo.fileSyncMarker,
        )

    private suspend fun getScriptLocalResourcePackFilePathsOrAll(): List<Path> =
        getScriptLocalAssetFilePathsOrAll(
            allowUsingOtherAssets = resourcePackSyncInfo.allowUsingOtherResourcePacks,
            isScriptAssetFile = { isScriptResourcePackFile(resourcePackFilePath = it) },
        )
}
