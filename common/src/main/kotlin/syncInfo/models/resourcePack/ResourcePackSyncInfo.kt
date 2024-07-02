package syncInfo.models.resourcePack

import kotlinx.serialization.Serializable
import syncInfo.models.FileIntegrityInfo
import syncInfo.models.SyncInfo

@Serializable
data class ResourcePackSyncInfo(
    /**
     * The list of resource-packs to sync.
     *
     * See [ResourcePack] for details.
     * */
    val resourcePacks: List<ResourcePack> = emptyList(),
    /**
     * Indicates whether the synced resource-packs should be activated in the game.
     *
     * When `true`, the script will activate the resource-packs in the order specified by [resourcePacks],
     * ignoring any built-in activated resource-packs.
     *
     * If [allowUsingOtherResourcePacks] is `true`, the script will also ignore the resource packs activated by the user.
     *
     * Currently the resource-packs will be applied with the following order:
     * 1. The [resourcePacks] with the same order
     * 2. The player resource-packs if [allowUsingOtherResourcePacks] is `true` and player have some resource-packs applied
     * 3. The built-in resource-packs, from mods, mod-loader or in the game if the player applied them
     *
     * TODO: Add an option to override the apply order
     */
    val shouldApplyResourcePacks: Boolean = false,
    /**
     * Will override [SyncInfo.shouldVerifyAssetFilesIntegrity] for the resource-packs
     *
     * See [ResourcePack.overrideShouldVerifyFileIntegrity] to override this value for a specific resource-pack
     *
     * @see FileIntegrityInfo
     *
     * */
    val shouldVerifyFilesIntegrity: Boolean? = null,
    /**
     * Determines whether the script allows the player to use resource-packs other than those synchronized by the script.
     *
     * If `true`, the script will not modify or delete any resource-packs not installed by the script.
     * If `false`, the script will delete any resource-packs not installed by the script and prevent the use of such resource-packs.
     *
     */
    val allowUsingOtherResourcePacks: Boolean = false,
    /**
     * Specifies a suffix to append to the file names of resource-packs downloaded by the script,
     * allowing the script to distinguish
     * between resource-packs it has installed and those installed by the player.
     * For example, a resource-pack file might be saved as `resource-pack.synced.zip`
     * instead of `resource-pack.zip`.
     *
     * This will allow [allowUsingOtherResourcePacks] to check if a resource-pack is installed by the script or the player.
     *
     * Pass `null` if you want to disable this and install the resource-pack with the file name just like how it's on the source.
     * In that case make sure to not pass `true` to [allowUsingOtherResourcePacks] because it won't work.
     *
     * If you want to change it to something like `resource-pack.sync.zip` pass `.sync` instead
     *
     * if there is already existing players that has the installed resource-packs with a specific [fileSyncMarker] and you change
     * it from (`.synced` to `.sync` for example), then the script would identify the resource-packs with (`.sync`) as resource-packs
     * installed by the player resulting in duplication, However, this occurs only if [allowUsingOtherResourcePacks] is true, otherwise
     * will be deleted and downloaded again in case of this changed
     * */
    val fileSyncMarker: String? = ".synced",
)
