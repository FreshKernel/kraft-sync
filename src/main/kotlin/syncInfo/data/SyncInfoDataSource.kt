package syncInfo.data

import syncInfo.models.SyncInfo

interface SyncInfoDataSource {
    suspend fun fetchSyncInfo(url: String): Result<SyncInfo>
}
