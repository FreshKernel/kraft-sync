package syncService

import constants.SyncScriptDotMinecraftFiles

class ShaderPacksSyncService : SyncService {
    private val shaderPacksDirectory = SyncScriptDotMinecraftFiles.ShaderPacks.file

    override suspend fun syncData() {
        TODO("Not yet implemented")
    }
}
