package syncInfo.data

import services.HttpClient
import services.HttpResponse
import syncInfo.models.SyncInfo
import utils.Logger

// TODO: I might add an option for caching the data/response or disable it. Similar to Next Js
class RemoteSyncInfoDataSource : SyncInfoDataSource {
    override suspend fun fetchSyncInfo(url: String): Result<SyncInfo> {
        Logger.info { "\uD83D\uDCE5 Sending GET request to: $url" }
        return when (val response = HttpClient.get(url = url)) {
            is HttpResponse.Success -> Result.success(response.decodeJson<SyncInfo>())
            is HttpResponse.HttpFailure ->
                Result.failure(response.exception())
            is HttpResponse.UnknownError -> Result.failure(exception = response.exception)
        }
    }
}
