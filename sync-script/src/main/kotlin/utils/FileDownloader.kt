package utils

import constants.SyncScriptInstanceFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * A utility class for downloading files from [downloadUrl] to [targetFile] with [progressListener].
 * TODO: Add the option to cancel a download, might need to refactor this class too
 *
 * Currently will handle errors internally by showing a error message and close.
 * This might change in case
 * we want to share this with other modules.
 * */
class FileDownloader(
    private val downloadUrl: String,
    private val targetFile: File,
    val progressListener: (
        downloadedBytes: Long,
        // in percentage, from 0 to 100
        downloadedProgress: Float,
        bytesToDownload: Long,
    ) -> Unit,
) {
    suspend fun downloadFile() {
        if (targetFile.exists()) {
            showErrorMessageAndTerminate(
                title = "📁 File Conflict",
                message =
                    "Unable to download the file. The destination file already exists. " +
                        "This might be a bug, delete the file: (${targetFile.path}) as a workaround.",
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
                    SyncScriptInstanceFiles.SyncScriptData.Temp.file.resolve(
                        "${targetFile.nameWithoutExtension}-${System.currentTimeMillis()}.${targetFile.extension}",
                    )
                if (!tempFile.exists()) {
                    tempFile.parentFile.mkdirs()
                }
                val wasFileDoesNotExist = tempFile.createNewFile()
                // If the file doesn't exist, it means it created successfully
                if (!wasFileDoesNotExist) {
                    showErrorMessageAndTerminate(
                        title = "📄 File Already Exists",
                        message =
                            "⚠️ The temporary file '${tempFile.name}' already exists. We are unable to create it. " +
                                "This might be a bug," +
                                " delete the file: ${targetFile.path} as a workaround.",
                    )
                }
                if (!tempFile.canWrite()) {
                    showErrorMessageAndTerminate(
                        title = "🔒 Permission Error",
                        message =
                            "It seems that we don't have the necessary write permission to download" +
                                " the file: ${tempFile.path}. Double check your permissions and try again.",
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
                Files.move(tempFile.toPath(), targetFile.toPath(), StandardCopyOption.ATOMIC_MOVE)
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorMessageAndTerminate(
                    title = "🚫 Couldn't download the file",
                    message = "An unknown error occurred while downloading the file: ${e.message} from the url: $downloadUrl",
                )
            }
        }
    }
}
