package minecraftAssetProviders.curseForge.models

import kotlinx.serialization.Serializable
import syncInfo.models.FileIntegrityInfo

// TODO: Update the integer types (64) to Long maybe, check all the types again

/**
 * From https://docs.curseforge.com/#tocS_File
 * */
@Serializable
data class CurseForgeFile(
    val id: Int,
    val gameId: Int,
    val modId: Int,
    val isAvailable: Boolean,
    val displayName: String,
    val fileName: String,
    /**
     * Represent [FileReleaseType] as integer
     * */
    val releaseType: Int,
    /**
     * Represent [FileStatus] as integer
     * */
    val fileStatus: Int,
    val hashes: List<FileHash>,
    val fileDate: String,
    val fileLength: Long,
    val downloadCount: Int,
    val fileSizeOnDisk: Long? = null,
    val downloadUrl: String,
    val gameVersions: List<String>,
    val sortableGameVersions: List<SortableGameVersion>,
    val dependencies: List<FileDependency>,
    val exposeAsAlternative: Boolean? = null,
    val parentProjectFileId: Int? = null,
    val alternateFileId: Int? = null,
    val isServerPack: Boolean? = null,
    val serverPackFileId: Int? = null,
    val isEarlyAccessContent: Boolean? = null,
    val earlyAccessEndDate: String? = null,
    val fileFingerprint: Long,
    val modules: List<FileModule>,
) {
    @Serializable
    enum class FileReleaseType(val value: Int) {
        Release(1),
        Beta(2),
        Alpha(3),
    }

    @Serializable
    enum class FileStatus(val value: Int) {
        Processing(1),
        ChangesRequired(2),
        UnderReview(3),
        Approved(4),
        Rejected(5),
        MalwareDetected(6),
        Deleted(7),
        Archived(8),
        Testing(9),
        Released(10),
        ReadyForReview(11),
        Deprecated(12),
        Baking(13),
        AwaitingPublishing(14),
        FailedPublishing(15),
    }

    @Serializable
    data class FileHash(
        val value: String,
        val algo: Int,
    )

    fun getFileIntegrityInfo() =
        FileIntegrityInfo(
            sha1 = hashes.firstOrNull { it.algo == 1 }?.value,
            md5 = hashes.firstOrNull { it.algo == 2 }?.value,
            sizeInBytes = fileLength,
        )

    @Serializable
    data class SortableGameVersion(
        val gameVersionName: String,
        val gameVersionPadded: String,
        val gameVersion: String,
        val gameVersionReleaseDate: String,
        val gameVersionTypeId: Int? = null,
    )

    @Serializable
    data class FileDependency(
        val modId: Int,
        /**
         * Represent [FileRelationType] as integer
         * */
        val relationType: Int,
    ) {
        @Serializable
        enum class FileRelationType(val value: Int) {
            EmbeddedLibrary(1),
            OptionalDependency(2),
            RequiredDependency(3),
            Tool(4),
            Incompatible(5),
            Include(6),
        }
    }

    @Serializable
    data class FileModule(
        val name: String,
        val fingerprint: Long,
    )
}
