package syncService

import Constants
import syncInfo.models.SyncInfo

class SyncServersService : SyncService(Constants.MinecraftInstanceFiles.ServersDat.file) {
    override suspend fun syncContents(syncInfo: SyncInfo) {
        TODO("Not yet implemented")
    }
}
