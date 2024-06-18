package minecraftAssetProviders.curseForge.data

import minecraftAssetProviders.curseForge.models.CurseForgeModFileDownloadUrlResponse
import minecraftAssetProviders.curseForge.models.CurseForgeModFileResponse

// TODO: Improve it so it will send only one network request to get all the data
// TODO: Might use https://docs.curseforge.com/#get-mod-file-download-url
// TODO: Notify the user to agree to https://docs.curseforge.com/#curseforge-for-studios-terms-of-use

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
