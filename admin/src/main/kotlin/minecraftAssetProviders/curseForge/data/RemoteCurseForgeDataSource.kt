package minecraftAssetProviders.curseForge.data

import constants.AdminConstants
import kotlinx.serialization.json.Json
import minecraftAssetProviders.curseForge.models.CurseForgeModFileDownloadUrlResponse
import minecraftAssetProviders.curseForge.models.CurseForgeModFileResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import utils.executeAsync
import utils.getBodyOrThrow

class RemoteCurseForgeDataSource(
    private val client: OkHttpClient,
) : CurseForgeDataSource {
    companion object {
        private const val BASE_URL = "https://api.curseforge.com/v1"
    }

    override suspend fun getModFile(
        modId: String,
        fileId: String,
        overrideApiKey: String?,
    ): Result<CurseForgeModFileResponse> {
        try {
            val url = "$BASE_URL/mods/$modId/files/$fileId"
            val request =
                Request.Builder().url(url)
                    .addHeader("x-api-key", overrideApiKey ?: AdminConstants.DEFAULT_CURSE_FORGE_API_KEY)
                    .get().build()
            val response =
                client.newCall(request).executeAsync()

            if (!response.isSuccessful) {
                return Result.failure(
                    RuntimeException(
                        "Unknown error while getting the Curse Forge mod file. status code: ${response.code}, body: " +
                            "${response.body?.string()}",
                    ),
                )
            }
            val responseBody: String = response.getBodyOrThrow().string()
            println("ℹ\uFE0F Curse Forge Mod ($modId) File ($fileId) Json Response: $responseBody")
            val syncInfo = Json.decodeFromString<CurseForgeModFileResponse>(responseBody)
            return Result.success(syncInfo)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun getModFileDownloadUrl(
        modId: String,
        fileId: String,
        overrideApiKey: String?,
    ): Result<CurseForgeModFileDownloadUrlResponse> {
        try {
            val url = "$BASE_URL/mods/$modId/files/$fileId/download-url"
            val request =
                Request.Builder().url(url)
                    .addHeader("x-api-key", overrideApiKey ?: AdminConstants.DEFAULT_CURSE_FORGE_API_KEY)
                    .get().build()
            val response =
                client.newCall(request).executeAsync()

            if (!response.isSuccessful) {
                return Result.failure(
                    RuntimeException(
                        "Unknown error while getting the Curse Forge mod file download url. status code: ${response.code}, body: " +
                            "${response.body?.string()}",
                    ),
                )
            }
            val responseBody: String = response.getBodyOrThrow().string()
            println("ℹ\uFE0F Curse Forge Mod ($modId) File ($fileId) Download URL Json Response: $responseBody")
            val syncInfo = Json.decodeFromString<CurseForgeModFileDownloadUrlResponse>(responseBody)
            return Result.success(syncInfo)
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
