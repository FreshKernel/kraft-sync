package services.modsConverter

import launchers.MinecraftLauncher
import services.modsConverter.models.ModsConvertMode
import syncInfo.models.Mod

interface ModsConverter {
    /**
     * Convert the mods from [selectedLauncher] into the project format [Mod]
     * */
    suspend fun convertMods(
        selectedLauncher: MinecraftLauncher,
        launcherInstanceDirectoryPath: String,
        convertMode: ModsConvertMode,
        prettyFormat: Boolean,
        curseForgeApiKeyOverride: String?,
        isCurseForgeForStudiosTermsOfServiceAccepted: Boolean,
    ): ModsConvertResult
}
