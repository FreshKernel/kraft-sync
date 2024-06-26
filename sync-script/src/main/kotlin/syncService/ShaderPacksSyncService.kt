package syncService

import constants.SyncScriptInstanceFiles

class ShaderPacksSyncService : SyncService {
    private val shaderPacksDirectory = SyncScriptInstanceFiles.ShaderPacks.file

    override suspend fun syncData() {
        TODO("Not yet implemented")
    }
}
