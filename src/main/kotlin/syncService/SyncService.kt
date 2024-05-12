package syncService

import syncInfo.models.SyncInfo
import java.io.File

/**
 * An abstract service that share common things will be used between different services that sync the resources
 * */
abstract class SyncService(
    val folder: File,
) {
    abstract suspend fun syncContents(syncInfo: SyncInfo)
}
