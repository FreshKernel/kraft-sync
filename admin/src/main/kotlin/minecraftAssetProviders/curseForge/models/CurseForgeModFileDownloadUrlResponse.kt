package minecraftAssetProviders.curseForge.models

import kotlinx.serialization.Serializable

/**
 * From https://docs.curseforge.com/#get-mod-file-download-url
 * */
@Serializable
data class CurseForgeModFileDownloadUrlResponse(
    val data: String,
)
