package syncInfo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import util.getFileNameFromUrl

// TODO: This might be used later https://docs.modrinth.com/#tag/versions/operation/scheduleVersion

/**
 * The mod data, we initially wanted to use **sealed class** to use different providers:
 *
 * ```kotlin
 * sealed class Mod {
 *     data class Custom(val downloadUrl: String): ModProvider()
 *     data class CurseForge(val modId :String): ModProvider()
 *     data class Modrinth(val modId: String): ModProvider()
 * }
 * ```
 *
 * but then we have to send http requests for each mod based on the provider api since there is no route for getting
 * all the mod download urls by mod ids, and it's requiring more time to run the script before launching the game
 *
 * */
@Serializable
data class Mod(
    /**
     * The public download url of the mod
     * */
    val url: String,
    /**
     * The [fileIntegrityInfo] contains different ways to validate a mod file integrity
     * */
    val fileIntegrityInfo: FileIntegrityInfo = FileIntegrityInfo(),
    /**
     * The mod name (optional) for now will be only used in the gui, if you don't specify it, then will get
     * the file name from [mod]
     * */
    val name: String? = null,
    /**
     * Used to check if the mod required or not to allow the user to ignore some mods
     * // TODO: Currently not implemented or might removed
     * */
    val required: Boolean = false,
    /**
     * The script need this to know if it should download the mod or not,
     * for example if this a server side only mod like Geyser, then use [ModSide.Server]
     * so it will be only installed when the environment is [Environment.Server]
     * or client side only mods that is quite big to download and don't need to be installed on the server
     * if the mod is required by both server and client then use [ModSide.Both]
     *
     * The accepted values are `client`, `server` or `both`
     *
     * // TODO: Make use of this
     * */
    val modSide: ModSide = ModSide.Both,
) {
    fun getDisplayName(): String {
        return name ?: getFileNameFromUrl(url) ?: throw IllegalStateException(
            "Error while getting the file name by the url $url which might be invalid.",
        )
    }

    companion object {
        @Serializable
        enum class ModSide {
            @SerialName("client")
            Client,

            @SerialName("server")
            Server,

            @SerialName("both")
            Both,

            ;

            fun isClient() = this == Client

            fun isServer() = this == Server

            fun isOnBothSides() = this == Both
        }
    }
}
