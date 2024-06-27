package utils

import constants.SyncScriptDotMinecraftFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okio.buffer
import okio.sink
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.isWritable
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.pathString

/**
 * A utility class for downloading files from [downloadUrl] to [targetFilePath] with [progressListener].
 * TODO: Add the option to cancel a download, might need to refactor this class too
 *
 * Currently will handle errors internally by showing a error message and close.
 * This might change in case
 * we want to share this with other modules.
 * */
class FileDownloader(
    private val downloadUrl: String,
    private val targetFilePath: Path,
    val progressListener: (
        downloadedBytes: Long,
        // in percentage, from 0 to 100
        downloadedProgress: Float,
        bytesToDownload: Long,
    ) -> Unit,
) {
    suspend fun downloadFile() {
        if (targetFilePath.exists()) {
            showErrorMessageAndTerminate(
                title = "📁 File Conflict",
                message =
                    "Unable to download the file. The destination file already exists. " +
                        "This might be a bug, delete the file: (${targetFilePath.pathString}) as a workaround.",
            )
        }
        val request =
            Request
                .Builder()
                .url(downloadUrl)
                .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = HttpService.client.newCall(request).executeAsync()
                if (!response.isSuccessful) {
                    showErrorMessageAndTerminate(
                        title = "Failed to download",
                        message = "Response status: ${response.code}, response body : ${response.body?.string()}",
                    )
                }

                val responseBody = response.getBodyOrThrow()
                val bytesToDownload = responseBody.contentLength()

                // Download the file to somewhere temporary and move it when success
                // We could use File.createTempFile from JVM, to avoid creating
                // files on the user system we will handle it manually
                val tempFile =
                    SyncScriptDotMinecraftFiles.SyncScriptData.Temp.path.resolve(
                        "${targetFilePath.nameWithoutExtension}-${System.currentTimeMillis()}.${targetFilePath.extension}",
                    )
                tempFile.createFileWithParentDirectoriesOrTerminate()
                if (!tempFile.isWritable()) {
                    showErrorMessageAndTerminate(
                        title = "🔒 Permission Error",
                        message =
                            "It seems that we don't have the necessary write permission to download" +
                                " the file: ${tempFile.pathString}. Double check your permissions and try again.",
                    )
                }
                tempFile.sink().buffer().use { sink ->
                    responseBody.source().use { source ->
                        var downloadedBytes = 0L
                        while (true) {
                            val readBytes = source.read(sink.buffer, 2048)
                            if (readBytes == -1L) break
                            downloadedBytes += readBytes
                            val progress = downloadedBytes.toFloat() / bytesToDownload.coerceAtLeast(1L) * 100
                            progressListener(downloadedBytes, progress, bytesToDownload)
                            sink.flush()
                        }
                    }
                }

                // The use block already calls 'response.closeQuietly()'

                // Move the downloaded file from the temporary place to where it should
                tempFile.moveToOrTerminate(
                    target = targetFilePath,
                    StandardCopyOption.ATOMIC_MOVE,
                    fileEntityType = "JAR",
                )
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorMessageAndTerminate(
                    title = "🚫 Couldn't download the file",
                    message = "An unknown error occurred while downloading the file ($downloadUrl): ${e.message}:",
                )
            }
        }
    }
}
