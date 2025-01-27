package syncService

import utils.buildHtml

/**
 * This interface defines a contract for services that will sync the data
 * */
interface SyncService {
    suspend fun syncData()

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
    fun buildProgressMessage(
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

    fun buildDownloadFileMessage(fileDisplayName: String): String =
        buildHtml {
            text("Downloading ")
            boldText(fileDisplayName)
        }.buildBodyAsText()

    fun buildVerifyFileMessage(fileDisplayName: String): String =
        buildHtml {
            text("Verifying ")
            boldText(fileDisplayName)
        }.buildBodyAsText()
}
