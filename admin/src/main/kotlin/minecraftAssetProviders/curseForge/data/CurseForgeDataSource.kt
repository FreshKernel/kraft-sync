package minecraftAssetProviders.curseForge.data

import minecraftAssetProviders.curseForge.models.CurseForgeModFileDownloadUrlResponse
import minecraftAssetProviders.curseForge.models.CurseForgeModFileResponse

// TODO: Improve it so it will send only one network request to get all the data

interface CurseForgeDataSource {
    suspend fun getModFile(
        modId: String,
        fileId: String,
        overrideApiKey: String?,
    ): Result<CurseForgeModFileResponse>

    suspend fun getModFileDownloadUrl(
        modId: String,
        fileId: String,
        overrideApiKey: String?,
    ): Result<CurseForgeModFileDownloadUrlResponse>
}
