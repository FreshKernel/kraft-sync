package syncInfo.models.mod

import kotlinx.serialization.Serializable
import syncInfo.models.Environment
import syncInfo.models.FileIntegrityInfo
import syncInfo.models.SyncInfo

@Serializable
data class ModSyncInfo(
    /**
     * The list of mods to sync, take a look at [Mod] for more details
     * */
    val mods: List<Mod> = emptyList(),
    /**
     * Will override [SyncInfo.shouldVerifyAssetFilesIntegrity] for the mods
     *
     * By default, the script only validates the file name and usually the file name contains
     * the mod and minecraft version, so when you update some mods, the old versions will be deleted and be
     * downloaded once again, if you also want the script to verify each file and if it matches the file from the source
     * if not, then it will be deleted and re-downloaded again
     *
     * Also, if some mods got corrupted because of killing the process, then this would be helpful to make sure
     * you have healthy mod files
     *
     * **Notice**: This option will only take effect for the mods that have at least one non-null value in the [FileIntegrityInfo]
     * for example if [FileIntegrityInfo.sha256] or [FileIntegrityInfo.sizeInBytes] is not null, you can use one, some or all
     * of them, it's up to you, in short if you want to verify a mod to be matched on the one the server, you have
     * to assign a value to at least one, use [FileIntegrityInfo.sha256] or [FileIntegrityInfo.sha512]
     * as it's validating the content to make sure it's valid and secure,
     * validating using [FileIntegrityInfo.sizeInBytes] is a little bit faster (the difference is very negligible)
     *
     * If you want to verify all the mods, then all the mods need to have at least one value for one
     * of those discussed above
     *
     * If you want to completely disable the verifying process,
     * pass false to [shouldVerifyModFilesIntegrity] and will ignore
     * even if the data in the [FileIntegrityInfo] are specified
     *
     * @see Mod.overrideShouldVerifyFileIntegrity to override this value for a specific mod
     *
     * */
    val shouldVerifyModFilesIntegrity: Boolean? = null,
    /**
     * if [Environment] value is [Environment.Client] then will download/sync only the mods that'd need to be
     * on te client side like client side mods and the mods that's needed to be in both Client and Server
     *
     * The exact opposite apply for [Environment.Server]
     *
     * This will depend on [Mod.clientSupport] and [Mod.serverSupport] values.
     *
     * **Notice**: If you automate the process of generating the mods info and get it from the Mod provider/ launcher,
     * it might depend on the info that's on the provider (e.g, Modrinth) and it could be that the developers of the mod
     * marked the mod as server side only and unsupported on the client side, while it's true for mods like [Geyser](https://modrinth.com/mod/geyser)
     *
     * It is usually optional on the client side.
     * An example mod is [FallingTree](https://modrinth.com/mod/fallingtree)
     * is marked as **server side only** and unsupported on the **client side** while it's **optional** for single player,
     * it could work in single player in the **integrated server**;
     * it's not needed on the **client side** to join a **multiplayer server**
     *
     * */
    val shouldSyncOnlyModsForCurrentEnvironment: Boolean = true,
    /**
     * Should the script allow the player to install mods other than the synced mods by the script?
     * if true then the script won't touch any mods that is not installed by the script, otherwise will delete it and
     * won't allow using any other mods, still can be bypassed, it's better to install a server side mod
     * that check the installed mods (can be bypassed)
     *
     * TODO: Currently (BUG) if you let's say have sodium installed (sodium.jar), the synced mod by the script would be (sodium.synced.jar)
     *  however the user can still install (sodium.jar) resulting in mod duplication which will cause the game to not run
     *  (for most mod loaders), we can solve this by reading the mod id from the jar and make sure there are no duplication
     * */
    val allowUsingOtherMods: Boolean = false,
    /**
     * A way for the script to download mods and save them like (`my-mod.synced.jar`) instead of (`my-mod.jar`)
     * the script need this so it can know if the mod installed by it or not to support [allowUsingOtherMods]
     * when true, so it will ignore the mods installed by the player
     *
     * Pass null if you want to disable this and install the mod file name just like how it's on the source.
     * In that case make sure to not pass true to [allowUsingOtherMods] because it won't work
     *
     * If you want to change it to something like (`my-mod.sync.jar`) pass `.sync` instead
     *
     * if there is already existing players that has the installed mods with a specific [modSyncMarker] and you change
     * it from (`.synced` to `.sync` for example), then the script would identify the mods with (`.sync`) as mods
     * installed by the player resulting in duplication, However, this occurs only if [allowUsingOtherMods] is true, otherwise
     * will be deleted and downloaded again in case of this changed
     * */
    val modSyncMarker: String? = ".synced",
)
