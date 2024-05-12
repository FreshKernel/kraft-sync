package syncService

import Constants
import syncInfo.models.SyncInfo

class SyncShaderPacksService: SyncService(Constants.MinecraftInstanceFiles.ShaderPacks.file) {
    override suspend fun syncContents(syncInfo: SyncInfo) {
        TODO("Not yet implemented")
    }
}