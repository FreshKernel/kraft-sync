package syncService

import constants.SyncScriptInstanceFiles

class ShaderPacksSyncService : SyncService {
    private val shaderPacksFolder = SyncScriptInstanceFiles.ShaderPacks.file

    override suspend fun syncData() {
        TODO("Not yet implemented")
    }
}
