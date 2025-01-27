package syncInfo.models.customFile

import kotlinx.serialization.Serializable
import syncInfo.models.FileIntegrityInfo
import syncInfo.models.SyncInfo

@Serializable
data class CustomFileSyncInfo(
    /**
     * List of custom files to sync.
     * */
    val files: List<CustomFile> = emptyList(),
    /**
     * Indicates whether the script should delete files that are no longer listed.
     * If set to `true`, files removed from [files] will be deleted during the sync process.
     *
     * This will only delete the files, but not the parent directories that have been created by the script,
     * and they might remain empty.
     */
    val deleteRemovedFiles: Boolean = false,
    /**
     * Overrides [SyncInfo.verifyAssetFilesIntegrity] for the custom files.
     *
     * See [CustomFile.verifyFileIntegrity] to override this value for a file.
     *
     * @see FileIntegrityInfo
     * */
    val verifyFilesIntegrity: Boolean? = null,
)
