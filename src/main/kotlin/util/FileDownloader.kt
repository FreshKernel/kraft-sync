package util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import ru.gildor.coroutines.okhttp.await
import java.io.File

// TODO: Improve the file downloader
class FileDownloader(
    private val url: String,
    private val targetFile: File,
    val progressListener: (
        downloadedBytes: Long,
        downloadedProgress: Float, // in percentage, from 0 to 100
        bytesToDownload: Long,
    ) -> Unit,
) {
    suspend fun downloadFile() {
        if (!targetFile.exists()) {
            showErrorMessage(
                title = "File doesn't exit",
                message = "Can't write file data into a file that doesn't exist",
            )
        }
        val request =
            Request.Builder()
                .url(url)
                .build()

        return withContext(Dispatchers.IO) {
            try {
                val response = okHttpClient.newCall(request).await()
                if (!response.isSuccessful) {
                    showErrorMessage(
                        title = "Failed to download",
                        message = "Response status: ${response.code}, response body : ${response.body?.string()}",
                    )
                    return@withContext
                }

                val responseBody =
                    response.body ?: kotlin.run {
                        showErrorMessage(
                            title = "Failed to download",
                            message = "Response successful but the body is null",
                        )
                        return@withContext
                    }
                val bytesToDownload = responseBody.contentLength()

                val sink = targetFile.sink().buffer()
                val source = responseBody.source()

                // Most of the code bellow is not written by me

                source.use {
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
                sink.close()
            } catch (e: Exception) {
                showErrorMessage(
                    title = "Unknown error",
                    message = "Unknown error while downloading a file: ${e.message}",
                )
            }
        }
    }
}
