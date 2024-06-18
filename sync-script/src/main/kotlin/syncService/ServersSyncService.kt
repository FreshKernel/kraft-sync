package syncService

import constants.SyncScriptInstanceFiles

class ServersSyncService : SyncService {
    private val serversFile = SyncScriptInstanceFiles.ServersDat.file

    override suspend fun syncData() {
        TODO("Not yet implemented")
    }
}
