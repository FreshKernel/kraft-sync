package minecraftAssetProviders.modrinth.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import syncInfo.models.FileIntegrityInfo

// Useful public Modrinth API route: https://docs.modrinth.com/#tag/versions/operation/getVersions

/**
 * From https://docs.modrinth.com/#tag/versions/operation/getVersion
 * */
@Serializable
data class ModrinthVersion(
    val name: String,
    @SerialName("version_number")
    val versionNumber: String,
    val changelog: String? = null,
    val dependencies: List<VersionDependency>,
    @SerialName("game_versions")
    val gameVersions: List<String>,
    @SerialName("version_type")
    val versionType: VersionType,
    val loaders: List<String>,
    val featured: Boolean,
    val status: VersionStatus,
    @SerialName("requested_status")
    val requestedStatus: RequestedStatus? = null,
    val id: String,
    @SerialName("project_id")
    val projectId: String,
    @SerialName("author_id")
    val authorId: String,
    @SerialName("date_published")
    val datePublished: String,
    val downloads: Int,
    @Deprecated("Always null, Modrinth kept it for legacy compatibility.")
    @SerialName("changelog_url")
    val changelogUrl: String? = null,
    val files: List<VersionFile>,
) {
    @Serializable
    data class VersionDependency(
        @SerialName("version_id")
        val versionId: String? = null,
        @SerialName("project_id")
        val projectId: String? = null,
        val fileName: String? = null,
        @SerialName("dependency_type")
        val dependencyType: Dependency,
    ) {
        @Serializable
        enum class Dependency {
            @SerialName("required")
            Required,

            @SerialName("optional")
            Optional,

            @SerialName("incompatible")
            Incompatible,

            @SerialName("embedded")
            Embedded,
        }
    }

    @Serializable
    enum class VersionType {
        @SerialName("release")
        Release,

        @SerialName("beta")
        Beta,

        @SerialName("alpha")
        Alpha,
    }

    @Serializable
    enum class VersionStatus {
        @SerialName("listed")
        Listed,

        @SerialName("archived")
        Archived,

        @SerialName("draft")
        Draft,

        @SerialName("unlisted")
        Unlisted,

        @SerialName("scheduled")
        Scheduled,

        @SerialName("unknown")
        Unknown,
    }

    @Serializable
    enum class RequestedStatus {
        @SerialName("listed")
        Listed,

        @SerialName("archived")
        Archived,

        @SerialName("draft")
        Draft,

        @SerialName("unlisted")
        Unlisted,
    }

    @Serializable
    data class VersionFile(
        val hashes: VersionFileHashes,
        val url: String,
        val filename: String,
        val primary: Boolean,
        val size: Long,
        @SerialName("file_type")
        val fileType: FileType? = null,
    ) {
        @Serializable
        data class VersionFileHashes(
            val sha512: String,
            val sha1: String,
        )

        @Serializable
        enum class FileType {
            @SerialName("required-resource-pack")
            RequiredResourcePack,

            @SerialName("optional-resource-pack")
            OptionalResourcePack,
        }

        fun getFileIntegrityInfo() =
            FileIntegrityInfo(
                sha1 = hashes.sha1,
                sha512 = hashes.sha512,
                sizeInBytes = size,
            )
    }
}
