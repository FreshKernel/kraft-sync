package syncService

import constants.SyncScriptInstanceFiles

class ResourcePacksSyncService : SyncService {
    private val resourcePacksFolder = SyncScriptInstanceFiles.ResourcePacks.file

    override suspend fun syncData() {
        TODO("Not yet implemented")
    }
}
