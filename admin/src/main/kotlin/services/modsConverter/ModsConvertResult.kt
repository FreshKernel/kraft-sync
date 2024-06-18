package services.modsConverter

import syncInfo.models.Mod

sealed class ModsConvertResult {
    data class Success(
        val mods: List<Mod>,
        val modsOutputText: String,
    ) : ModsConvertResult()

    data class Failure(val error: ModsConvertError) : ModsConvertResult()

    data object NeedToAcceptCurseForgeForStudiosTermsOfUse : ModsConvertResult()
}
