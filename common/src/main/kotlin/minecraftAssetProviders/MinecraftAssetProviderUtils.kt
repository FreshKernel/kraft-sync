package minecraftAssetProviders

import syncInfo.models.SyncInfo
import utils.baseUrl
import java.net.URL

object MinecraftAssetProviderUtils {
    fun getAssetProvider(downloadUrl: String): Result<MinecraftAssetProvider> =
        try {
            val assetProvider =
                MinecraftAssetProvider.knownProviders.firstOrNull {
                    val url = URL(downloadUrl)
                    it.downloadSources.any { downloadSource ->
                        url.host == URL(downloadSource).host
                    }
                } ?: MinecraftAssetProvider.Unknown(
                    link = downloadUrl.baseUrl(),
                    downloadSources = listOf(downloadUrl.baseUrl()),
                    providerName = URL(downloadUrl).host,
                )
            Result.success(assetProvider)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    /**
     * @return The used providers for all the assets (e.g., Mods, Resource-packs etc...)
     * */
    fun getAssetsProviders(syncInfo: SyncInfo): Result<Set<MinecraftAssetProvider>> {
        val assetsProviders = mutableSetOf<MinecraftAssetProvider>()
        syncInfo.modSyncInfo.mods.mapTo(assetsProviders) { getAssetProvider(it.downloadUrl).getOrThrow() }
        // TODO: Add all kind of assets like Resource-packs and shaders
        return Result.success(assetsProviders)
    }
}
