package syncInfo.data

import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import syncInfo.models.SyncInfo
import utils.executeAsync
import utils.getBodyOrThrow

// TODO: I might add an option for caching the data/response or disable it. Similar to Next Js
class RemoteSyncInfoDataSource(
    private val client: OkHttpClient,
) : SyncInfoDataSource {
    override suspend fun fetchSyncInfo(url: String): Result<SyncInfo> {
        try {
            println("\uD83D\uDCE5 Sending GET request to: $url")
            val request = Request.Builder().url(url).get().build()
            val response = client.newCall(request).executeAsync()
            if (!response.isSuccessful) {
                return Result.failure(
                    RuntimeException(
                        "Unknown error while getting the sync info data. status code: ${response.code}, body: ${response.body?.string()}",
                    ),
                )
            }
            val responseBody: String = response.getBodyOrThrow().string()
            val syncInfo = Json.decodeFromString<SyncInfo>(responseBody)
            println("ℹ\uFE0F Sync Info Json Response: ${Json.encodeToString(SyncInfo.serializer(), syncInfo)}")
            println("ℹ\uFE0F Sync Info: $syncInfo")
            return Result.success(syncInfo)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
