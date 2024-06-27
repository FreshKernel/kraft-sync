package services.modsConverter

import launchers.MinecraftLauncher
import services.modsConverter.models.ModsConvertMode
import syncInfo.models.Mod

interface ModsConverter {
    /**
     * Convert the mods from [launcher] into the project format [Mod]
     * */
    suspend fun convertMods(
        launcher: MinecraftLauncher,
        launcherInstanceDirectoryPathString: String,
        convertMode: ModsConvertMode,
        prettyFormat: Boolean,
        overrideCurseForgeApiKey: String?,
        isCurseForgeForStudiosTermsOfServiceAccepted: Boolean,
    ): ModsConvertResult
}
