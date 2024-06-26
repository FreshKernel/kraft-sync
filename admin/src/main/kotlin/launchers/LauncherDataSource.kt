package launchers

import minecraftAssetProviders.MinecraftAssetProvider
import syncInfo.models.Mod
import java.io.File

/**
 * An interface that abstract dealing with the launcher data, like converting mods from the launcher data format
 * into the script data format
 *
 * This is specific for the instance data, instance is also known as profile in some launchers like
 * [MinecraftLauncher.ModrinthApp].
 *
 * They are usually the same with a different name.
 * */
interface LauncherDataSource {
    /**
     * Check if the provided path is valid and contain expected common folders and files
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
    suspend fun isCurseForgeApiRequestNeededForConvertingMods(launcherInstanceDirectory: File): Result<Boolean>

    /**
     * Check if the instance has mods installed (not empty).
     * */
    suspend fun hasMods(launcherInstanceDirectory: File): Result<Boolean>

    /**
     * @return A list of [Mod] which contains the information about the mod, converting it from the specified launcher
     * into the script data format
     * */
    suspend fun getLauncherInstanceMods(
        launcherInstanceDirectory: File,
        curseForgeApiKeyOverride: String?,
    ): Result<List<Mod>>

    /**
     * Get the Pre Launch command that is set in the instance settings to configure a command to run
     * before launching the game.
     *
     * @return Null if it's not set
     * */
    suspend fun getPreLaunchCommand(launcherInstanceDirectory: File): Result<String?>

    /**
     * Update the Pre Launch command.
     *
     * Will also enable the commands/hooks if [command] is not null, otherwise will disable it only if
     * the user doesn't use other commands/hooks
     *
     * @param command The command to set, pass null to disable or delete it.
     * */
    suspend fun setPreLaunchCommand(
        command: String?,
        launcherInstanceDirectory: File,
    ): Result<Unit>

    /**
     * @return A list of the instances for the launcher
     * */
    suspend fun getInstances(): Result<List<Instance>?>
}
