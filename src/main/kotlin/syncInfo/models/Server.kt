package syncInfo.models

import kotlinx.serialization.Serializable

@Serializable
data class Server(
    /**
     * The server name, for example (My Minecraft Server)
     * */
    val name: String,
    /**
     * The server address, for example (localhost)
     * */
    val address: String,
)
