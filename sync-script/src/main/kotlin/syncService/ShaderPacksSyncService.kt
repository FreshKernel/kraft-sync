package syncService

import constants.SyncScriptDotMinecraftFiles

class ShaderPacksSyncService : SyncService {
    private val shaderPacksDirectoryPath = SyncScriptDotMinecraftFiles.ShaderPacks.path

    override suspend fun syncData() {
        TODO("Not yet implemented")
    }
}
