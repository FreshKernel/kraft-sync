package syncService

import Constants
import syncInfo.models.SyncInfo

class SyncResourcePacksService : SyncService(Constants.MinecraftInstanceFiles.ResourcePacks.file) {
    override suspend fun syncContents(syncInfo: SyncInfo) {
        TODO("Not yet implemented")
    }
}
