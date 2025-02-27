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
     * Will override [SyncInfo.verifyAssetFilesIntegrity] for the mods
     *
     * See [Mod.verifyFileIntegrity] to override this value for a specific mod
     *
     * @see FileIntegrityInfo
     *
     * */
    val verifyFilesIntegrity: Boolean? = null,
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
     * it's not needed on the **client side** to join a **multiplayer server**.
     *
     * Another example is that if you use a mod required on both sides, and it uses a library
     * mod marked as **server side only** although it's required to run the mod.
     * You will get an error from the mod loader.
     *
     * */
    val syncOnlyModsForCurrentEnv: Boolean = false,
    /**
     * Indicates whether optional mods for the current [Environment] should be synced.
     *
     * Some mods might be optional to install on either server or client side or both.
     *
     * This setting only takes effect if [syncOnlyModsForCurrentEnv] is `true`.
     * If [syncOnlyModsForCurrentEnv] is `false`, this property has no impact.
     *
     * if [syncOptionalModsForCurrentEnv] is `false`, then will only sync the
     * required mods for the current system [Environment].
     *
     * This option is overridable in the script config for a specific client.
     *
     * @see Mod.syncOptionalForCurrentEnv
     * */
    val syncOptionalModsForCurrentEnv: Boolean = true,
    /**
     * Determines whether the script allows the player to use mods other than those synchronized by the script.
     *
     * If `true`, the script will not modify or delete any mods not installed by the script.
     * If `false`, the script will delete any mods not installed by the script and prevent the use of such mods
     *
     * This restriction can be bypassed.
     * It is recommended to install a server-side mod
     * that checks the installed mods for better enforcement (though this too can be bypassed).
     *
     * TODO: Currently (BUG) if you let's say have sodium installed (sodium.jar), the synced mod by the script would be (sodium.synced.jar)
     *  however the user can still install (sodium.jar) resulting in mod duplication which will cause the game to not run
     *  (for most mod loaders), we can solve this by reading the mod id from the jar and make sure there are no duplication
     * */
    val allowUsingOthers: Boolean = false,
    /**
     * Specifies a suffix to append to the file names of mods downloaded by the script,
     * allowing the script to distinguish
     * between mods it has installed and those installed by the player.
     * For example, a mod file might be saved as `my-mod.synced.jar`
     * instead of `my-mod.jar`.
     *
     * This will allow [allowUsingOthers] to check if a mod is installed by the script or the player.
     *
     * Pass `null` if you want to disable this and install the mod with the file name just like how it's on the source.
     * In that case make sure to not pass `true` to [allowUsingOthers] because it won't work.
     *
     * If you want to change it to something like `my-mod.sync.jar` pass `.sync` instead
     *
     * if there is already existing players that has the installed mods with a specific [fileSyncMarker] and you change
     * it from (`.synced` to `.sync` for example), then the script would identify the mods with (`.sync`) as mods
     * installed by the player resulting in duplication, However, this occurs only if [allowUsingOthers] is true, otherwise
     * will be deleted and downloaded again in case of this changed
     * */
    val fileSyncMarker: String? = ".synced",
)
