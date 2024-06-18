package syncService

/**
 * This interface defines a contract for services that will sync the data
 * */
interface SyncService {
    suspend fun syncData()
}
