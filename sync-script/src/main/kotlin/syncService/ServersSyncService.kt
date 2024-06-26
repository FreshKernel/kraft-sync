package syncService

import constants.SyncScriptDotMinecraftFiles

class ServersSyncService : SyncService {
    private val serversFile = SyncScriptDotMinecraftFiles.ServersDat.file

    override suspend fun syncData() {
        TODO("Not yet implemented")
    }
}
