package syncInfo.models

import kotlinx.serialization.Serializable
import util.gui.LoadingIndicatorWindow

/**
 * This data class represents the JSON structure containing sync information from a remote server.
 * To simplify editing the JSON data, most variables like mods and related options to the mods are included directly,
 *
 * However, when adding new data fields to this class, ensure they are added in the correct order
 * to maintain consistency with the JSON structure.
 * */
@Serializable
data class SyncInfo(
    /**
     * Should allow to close the window of [LoadingIndicatorWindow] while syncing processes by downloading resources from the network
     * // TODO: Set back to false later
     * */
    val canCloseWhileDownloading: Boolean = true,
    /**
     * The list of mods to sync, please take a look at [Mod] for more details
     * */
    val mods: List<Mod> = listOf(),
    /**
     * // TODO: Update this description after moving the ways to validate the mod in another file
     * By default, the script only validate the file name and usually the file name contains
     * the mod and minecraft version, so when you update some mods, the old versions will be deleted and be
     * downloaded once again, but if you also want the script to verify each file and if it matches the file from the source
     * if not then it will be deleted and re-downloaded again
     *
     * also if some of the mods got corrupted because of killing the script, then this would be helpful to make sure
     * you have healthy mod files
     *
     * **Notice**: This option will only take affect for the mods that have at least one non-null value in the [FileIntegrityInfo]
     * for example if [FileIntegrityInfo.sha256] or [FileIntegrityInfo.size] is not null, you can use one, some or all
     * of them, it's up to you, in short if you want to verify a mod to be matched on the one one the server, you have
     * to assign a value to at least one, use [FileIntegrityInfo.sha256] or [FileIntegrityInfo.sha512]
     * as it's validating the content to make sure it's valid and secure,
     * validating using [FileIntegrityInfo.size] is a little bit faster (the difference is very negligible)
     *
     * if you want to verify all the mods, then all the mods needs to have at least one value for one
     * of those discussed above
     *
     * if you want to completely disable the verifying process, pass false to [verifyModsIntegrity] and will ignored
     * even if the data in the [FileIntegrityInfo] are specified
     *
     * // TODO: Make use of this
     * */
    val verifyModsIntegrity: Boolean = true,
    /**
     * Should the script allow the player to install mods other than the synced mods by the script?
     * if true then the script won't touch any mods that is not installed by the script, otherwise will delete it and
     * won't allow using any other mods, still can be bypassed, it's better to install a server side mod
     * that check the installed mods (still can be bypassed but extra layer of security)
     *
     * TODO: Currently if you let's say have sodium installed (sodium.jar), the synced mod by the script would be (sodium.synced.jar)
     * but the user can still install (sodium.jar) resulting in mod duplication which will cause the game to not run
     * (for most mod loaders), we will solve this by reading the mod id from the jar and make sure there are no duplication
     * */
    val allowUsingOtherMods: Boolean = false,
    /**
     * A way for the script to download mods and save them like (`my-mod.synced.jar`) instead of (`my-mod.jar`)
     * the script need this so it can know if the mod installed by it or not in order to support [allowUsingOtherMods]
     * when true, so it will ignore the mods installed by the player
     *
     * pass null if you want to disable this and install the mod file name just like how it's on the server
     * but in that case make sure to not pass true to [allowUsingOtherMods] because it won't work
     *
     * if you want to change it to something like (`my-mod.sync.jar`) please pass `.sync` instead
     *
     * if there is already existing players that has the installed mods with a specific [modSyncMarker] and you change
     * it from (`.synced` to `.sync` for example), then the script would identify the mods with (`.sync`) as mods
     * installed by the player resulting in duplication, but that's only if [allowUsingOtherMods] is true, otherwise
     * will be deleted and downloaded again in case of this changed
     * */
    val modSyncMarker: String? = ".synced",
    /**
     * The list of servers to sync, so when you change the server address or move to another host
     * the players is no longer required to update it, it will be all automated, you add multiple servers
     * in case you have different servers for different regions or some other use-case for example
     * */
    val servers: List<Server> = listOf(),
)
