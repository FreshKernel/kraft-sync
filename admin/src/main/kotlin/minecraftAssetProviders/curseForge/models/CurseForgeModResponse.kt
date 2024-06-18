package minecraftAssetProviders.curseForge.models

import kotlinx.serialization.Serializable

/**
 * From https://docs.curseforge.com/#tocS_Get%20Mod%20Response
 * */
@Serializable
data class CurseForgeModResponse(
    val data: CurseForgeMod,
)
