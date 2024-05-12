package syncInfo.data

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import ru.gildor.coroutines.okhttp.await
import syncInfo.models.SyncInfo
import java.io.IOException

// TODO: I might add an option for caching the data/response or disable it. Similar to Next Js
class RemoteSyncInfoDataSource(
    private val client: OkHttpClient,
) : SyncInfoDataSource {
    override suspend fun fetchSyncInfo(url: String): Result<SyncInfo> {
        try {
            println("Sending GET request to: $url")
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).await()
            if (!response.isSuccessful) {
                return Result.failure(
                    RuntimeException(
                        "Unknown error while getting the sync info data. status code: ${response.code}, body: ${response.body?.string()}",
                    ),
                )
            }
            val responseBody =
                response.body?.string()
                    ?: return Result.failure(
                        IOException(
                            "Response body for the sync info when sending GET request to: " +
                                url,
                        ),
                    )
            val syncInfo = Json.decodeFromString<SyncInfo>(responseBody)
            return Result.success(syncInfo)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
