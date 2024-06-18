package syncInfo.models

private lateinit var syncInfoInstance: SyncInfo

var SyncInfo.Companion.instance: SyncInfo
    get() = syncInfoInstance
    set(value) {
        syncInfoInstance = value
    }
