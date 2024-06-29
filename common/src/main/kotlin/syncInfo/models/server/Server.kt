package syncInfo.models.server

import kotlinx.serialization.Serializable

@Serializable
data class Server(
    /**
     * The server name, for example, New Server
     * */
    val name: String,
    /**
     * The server address, for example, localhost
     * */
    val address: String,
)
