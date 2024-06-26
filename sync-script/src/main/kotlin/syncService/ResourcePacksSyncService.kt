package syncService

import constants.SyncScriptInstanceFiles

class ResourcePacksSyncService : SyncService {
    private val resourcePacksDirectory = SyncScriptInstanceFiles.ResourcePacks.file

    override suspend fun syncData() {
        TODO("Not yet implemented")
    }
}
