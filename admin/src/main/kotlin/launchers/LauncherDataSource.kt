package launchers

import minecraftAssetProviders.MinecraftAssetProvider
import syncInfo.models.Mod
import java.io.File

/**
 * An interface that abstract dealing with the launcher data, like converting mods from the launcher data format
 * into the script data format
 * */
interface LauncherDataSource {
    /**
     * Check if the provided path is valid and contain valid expected data
     * it doesn't return a [Boolean], instead it will return [Result.failure]
     * it will attempt to parse the data and find it, if anything goes wrong, it will return [Result.failure]
     * with an exception.
     * @return [Result.success] if valid, otherwise [Result.failure]
     * */
    suspend fun validateInstanceDirectory(launcherInstanceDirectory: File): Result<Unit>

    /**
     * If an HTTP get request is needed to convert all the mods, this will be true if
     * 1. You're downloaded some or all the mods from [MinecraftAssetProvider.CurseForge]
     * 2. If the launcher doesn't store the download URL for the mod from either [MinecraftAssetProvider.CurseForge] or other providers
     * like [MinecraftAssetProvider.Modrinth]
     * 3. Other reasons that are specific to the launcher implementation, if the download url or related info wasn't available
     * for some reason and a request to Curse Forge API is needed to get the info, this should return true
     * */
    suspend fun isCurseForgeApiRequestNeeded(launcherInstanceDirectory: File): Result<Boolean>

    /**
     * @return A list of [Mod] which contains the information about the mod, converting it from the specified launcher
     * into the script data format
     * */
    suspend fun getMods(
        launcherInstanceDirectory: File,
        curseForgeApiKeyOverride: String?,
    ): Result<List<Mod>>
}
