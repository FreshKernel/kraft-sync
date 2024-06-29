package syncInfo.models.server

import kotlinx.serialization.Serializable

@Serializable
data class ServerSyncInfo(
    /**
     * The list of servers to sync, so when you change the server address or move to another host,
     * the players are no longer required to update it, it will be all automated, you can add multiple servers
     * in case you have different servers for different regions or some other use-case, for example
     * */
    val servers: List<Server> = emptyList(),
)
