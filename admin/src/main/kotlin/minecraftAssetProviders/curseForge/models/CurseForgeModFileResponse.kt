package minecraftAssetProviders.curseForge.models

import kotlinx.serialization.Serializable

/**
 * From https://docs.curseforge.com/#tocS_Get%20Mod%20File%20Response
 * */
@Serializable
data class CurseForgeModFileResponse(
    val data: CurseForgeFile,
)
