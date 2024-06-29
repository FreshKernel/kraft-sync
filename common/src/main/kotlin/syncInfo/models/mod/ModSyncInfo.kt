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
     * See [Mod.overrideShouldVerifyFileIntegrity] to override this value for a specific mod
     *
     * @see FileIntegrityInfo
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
     * if there is already existing players that has the installed mods with a specific [fileSyncMarker] and you change
     * it from (`.synced` to `.sync` for example), then the script would identify the mods with (`.sync`) as mods
     * installed by the player resulting in duplication, However, this occurs only if [allowUsingOtherMods] is true, otherwise
     * will be deleted and downloaded again in case of this changed
     * */
    val fileSyncMarker: String? = ".synced",
)
