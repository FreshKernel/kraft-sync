package syncInfo.models.resourcePack

import kotlinx.serialization.Serializable
import syncInfo.models.FileIntegrityInfo
import syncInfo.models.SyncInfo

@Serializable
data class ResourcePack(
    /**
     * The public download url of the resource-pack file
     * */
    val downloadUrl: String,
    /**
     * The [fileIntegrityInfo] contains different ways to validate a resource-pack file integrity
     * */
    val fileIntegrityInfo: FileIntegrityInfo = FileIntegrityInfo(),
    /**
     * The resource-pack name (optional) that might be displayed in GUI, if not present
     * will fall back to the file name from [downloadUrl]
     * */
    val name: String? = null,
    /**
     * Will override [ResourcePackSyncInfo.verifyFilesIntegrity] and [SyncInfo.verifyAssetFilesIntegrity]
     * for a specific resource-pack
     * */
    val verifyFileIntegrity: Boolean? = null,
)
