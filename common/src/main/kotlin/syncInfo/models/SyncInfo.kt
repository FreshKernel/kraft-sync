package syncInfo.models

import kotlinx.serialization.Serializable
import syncInfo.models.mod.ModSyncInfo
import syncInfo.models.server.ServerSyncInfo

// When adding new data fields to this data class, ensure they are added in the correct order
// to maintain consistency with the JSON structure.

/**
 * This data class represents the JSON structure containing sync information from a remote server.
 * */
@Serializable
data class SyncInfo(
    /**
     * Indicates whether the integrity of asset files should be verified.
     *
     * If set to `true`, the integrity check for asset files will be performed.
     * It Will validate the file integrity to make sure it has not been corrupted or modified and the file name.
     * This will validate both the newly downloaded files and the current files
     *
     * If set to `false`, the integrity check will be skipped.
     * Will only validate the file name
     *
     */
    val shouldVerifyAssetFilesIntegrity: Boolean = true,
    /**
     * The method/option preferred to validate the assets, will fall back to other methods if not available
     *
     * Set this to null if you want to validate all
     * @see PreferredFileVerificationOption
     * */
    val preferredAssetFileVerification: PreferredFileVerificationOption? = PreferredFileVerificationOption.Medium,
    /**
     * All required and optional properties for the mod syncing process
     * */
    val modSyncInfo: ModSyncInfo = ModSyncInfo(),
    /**
     * All required and optional properties for the server syncing process
     * */
    val serverSyncInfo: ServerSyncInfo = ServerSyncInfo(),
) {
    companion object
}
