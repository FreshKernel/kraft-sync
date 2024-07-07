package minecraftAssetProviders.curseForge.data

import constants.AdminConstants
import minecraftAssetProviders.curseForge.models.CurseForgeModFileDownloadUrlResponse
import minecraftAssetProviders.curseForge.models.CurseForgeModFileResponse
import services.HttpClient
import services.HttpResponse

class RemoteCurseForgeDataSource : CurseForgeDataSource {
    companion object {
        private const val BASE_URL = "https://api.curseforge.com/v1"
        private const val API_KEY_HEADER_NAME = "x-api-key"
    }

    private fun getApiKeyValue(overrideApiKey: String?) = overrideApiKey ?: AdminConstants.DEFAULT_CURSE_FORGE_API_KEY

    override suspend fun getModFile(
        modId: String,
        fileId: String,
        overrideApiKey: String?,
    ): Result<CurseForgeModFileResponse> {
        val url = "$BASE_URL/mods/$modId/files/$fileId"
        return when (
            val response =
                HttpClient.get(
                    url = url,
                    headers = mapOf(API_KEY_HEADER_NAME to getApiKeyValue(overrideApiKey = overrideApiKey)),
                )
        ) {
            is HttpResponse.Success -> {
                println("ℹ\uFE0F Curse Forge Mod ($modId) File ($fileId) Json Response: ${response.body}")
                Result.success(response.decodeJson<CurseForgeModFileResponse>())
            }

            is HttpResponse.HttpFailure ->
                Result.failure(response.exception())

            is HttpResponse.UnknownError -> Result.failure(exception = response.exception)
        }
    }

    override suspend fun getModFileDownloadUrl(
        modId: String,
        fileId: String,
        overrideApiKey: String?,
    ): Result<CurseForgeModFileDownloadUrlResponse> {
        val url = "$BASE_URL/mods/$modId/files/$fileId/download-url"
        val response =
            HttpClient.get(
                url = url,
                headers = mapOf(API_KEY_HEADER_NAME to getApiKeyValue(overrideApiKey = overrideApiKey)),
            )
        return when (response) {
            is HttpResponse.Success -> {
                println("ℹ\uFE0F Curse Forge Mod ($modId) File ($fileId) Download URL Json Response: ${response.body}")
                Result.success(response.decodeJson<CurseForgeModFileDownloadUrlResponse>())
            }

            is HttpResponse.HttpFailure ->
                Result.failure(response.exception())

            is HttpResponse.UnknownError -> Result.failure(exception = response.exception)
        }
    }
}
