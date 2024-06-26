package syncService

import constants.SyncScriptDotMinecraftFiles

class ResourcePacksSyncService : SyncService {
    private val resourcePacksDirectory = SyncScriptDotMinecraftFiles.ResourcePacks.file

    override suspend fun syncData() {
        TODO("Not yet implemented")
    }
}
