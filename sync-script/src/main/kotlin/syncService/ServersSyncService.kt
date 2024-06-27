package syncService

import constants.SyncScriptDotMinecraftFiles

class ServersSyncService : SyncService {
    private val serversFilePath = SyncScriptDotMinecraftFiles.ServersDat.path

    override suspend fun syncData() {
        TODO("Not yet implemented")
    }
}
