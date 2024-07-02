package syncService.common

import syncService.SyncService
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

/**
 * Share common code for syncing files inside a [assetDirectory], like the mods, resource-packs, shaders.
 * */
abstract class AssetSyncService(
    val assetDirectory: Path,
    val assetFileExtension: String,
) : SyncService {
    protected fun validateAssetDirectory() {
        if (!assetDirectory.exists()) {
            println("\uD83D\uDCC1 The '${assetDirectory.name}' folder doesn't exist, creating it..")
            assetDirectory.createDirectories()
        }

        if (!assetDirectory.isDirectory()) {
            showErrorMessageAndTerminate(
                title = "❌ Invalid '${assetDirectory.name}' Folder",
                message =
                    "\uD83D\uDEE0 '${assetDirectory.name}' must be a directory/folder \uD83D\uDCC2, a file was found instead.",
            )
        }
    }

    /**
     * @return The message that will be used for the dialog that will show the progress
     * of syncing, downloading, and verifying items.
     *
     * @param currentIndex The index of the current item being processed (0-based index).
     * @param pendingCount The number of items remaining to be processed or downloaded.
     * @param totalCount The total number of items that should be processed or present in [assetDirectory].
     *
     * @return The progress message indicating the current state of the process.
     * */
    protected fun buildProgressMessage(
        currentIndex: Int,
        pendingCount: Int,
        totalCount: Int,
    ): String =
        buildString {
            append("${currentIndex + 1} of $pendingCount")
            if (pendingCount != totalCount) {
                append(" ($totalCount total)")
            }
        }

    /**
     * Generates the file path that will be used in the [assetDirectory].
     *
     * @param downloadUrl The download URL that will be used to get the file name from
     * @param fileSyncMarker Will be appended to the file name if not null, will be used to know if the
     * asset file is created/synced by the script.
     *
     *
     * @return The [Path] for the asset file
     *
     * @see isScriptAssetFile
     * */
    protected fun getAssetFilePath(
        downloadUrl: String,
        fileSyncMarker: String?,
    ): Path {
        val assetFileNameWithoutExtension =
            Paths.get(getFileNameFromUrlOrError(downloadUrl)).nameWithoutExtension
        val assetFileName =
            buildString {
                append(assetFileNameWithoutExtension)
                fileSyncMarker?.let { append(it) }
                append(".$assetFileExtension")
            }
        return assetDirectory.resolve(assetFileName)
    }

    /**
     * Check if the [assetFileFilePath] was created/synced by the script.
     *
     * It will be identified by [fileSyncMarker], will return `true` if [fileSyncMarker] is null
     * and the [assetFileFilePath] end with [assetFileExtension]
     *
     * @return if this asset file is created/synced by the script.
     *
     * @see getAssetFilePath
     * */
    protected fun isScriptAssetFile(
        assetFileFilePath: Path,
        fileSyncMarker: String?,
    ): Boolean =
        assetFileFilePath.name.endsWith(
            "${fileSyncMarker.orEmpty()}.$assetFileExtension",
        )

    /**
     * Either the script asset files or all files depending on [allowUsingOtherAssets]
     *
     * @return The script local asset files that will be dealt with
     * */
    protected suspend fun getScriptLocalAssetFilePathsOrAll(
        allowUsingOtherAssets: Boolean,
        isScriptAssetFile: (assetFilePath: Path) -> Boolean,
    ): List<Path> {
        return assetDirectory
            .listFilteredPaths {
                val isAssetFile =
                    !it.isDirectory() && !it.isHidden() && it.extension == assetFileExtension
                if (allowUsingOtherAssets) {
                    return@listFilteredPaths isAssetFile && isScriptAssetFile(it)
                } else {
                    return@listFilteredPaths isAssetFile
                }
            }.getOrElse {
                showErrorMessageAndTerminate(
                    title = "📁 File Listing Error",
                    message = "⚠ Failed to list the files in the '${assetDirectory.name}' folder: $it",
                )
                // This will never reach due to the previous statement stopping the application
                exitProcess(1)
            }
    }
}
