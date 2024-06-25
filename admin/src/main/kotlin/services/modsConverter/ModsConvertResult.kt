package services.modsConverter

import constants.AdminConstants
import syncInfo.models.Mod

sealed class ModsConvertResult {
    data class Success(
        val mods: List<Mod>,
        val modsOutputText: String,
    ) : ModsConvertResult()

    data class Failure(
        val error: ModsConvertError,
    ) : ModsConvertResult()

    /**
     * Need to accept Curse Forge Terms of Service [AdminConstants.CURSE_FORGE_FOR_STUDIOS_TERMS_OF_SERVICE_URL]
     * */
    data object RequiresAcceptanceOfCurseForgeForStudiosTermsOfUse : ModsConvertResult()
}
