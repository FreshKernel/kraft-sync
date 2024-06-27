package syncService

import constants.SyncScriptDotMinecraftFiles

class ResourcePacksSyncService : SyncService {
    private val resourcePacksDirectoryPath = SyncScriptDotMinecraftFiles.ResourcePacks.path

    override suspend fun syncData() {
        TODO("Not yet implemented")
    }
}
