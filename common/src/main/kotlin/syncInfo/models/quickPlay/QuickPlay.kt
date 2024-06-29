package syncInfo.models.quickPlay

import constants.DotMinecraftFileNames
import kotlinx.serialization.Serializable

/**
 * A data class that contains information the Minecraft Java Quick Play Feature which was added in
 * Minecraft Version [snapshot 23w14a](https://www.minecraft.net/en-us/article/minecraft-snapshot-23w14a)
 * and above.
 * Only of the properties should be either not null or all of them null to disable the feature
 *
 * @param serverAddress The address used to connect to minecraft server have backward compatibility
 * for earlier versions (lower than 1.20), example `localhost`, `localhost:25565`, `play.server.net` or `play.server.net:25565`
 * @param worldSaveName The world save name (folder name) in the [DotMinecraftFileNames.SAVES_DIRECTORY] folder
 * that is used to load a minecraft world, work only on recent minecraft versions (1.20 and above), example `New World`
 * @param realmId The realm id that is used to connect to a Minecraft Realm, work only on recent minecraft versions (1.20 and above)
 *
 * Work only on the supported Minecraft launchers
 *
 * **Currently this feature is not implemented**.
 *
 * */
@Serializable
data class QuickPlay(
    val serverAddress: String? = null,
    val worldSaveName: String? = null,
    val realmId: String? = null,
)
