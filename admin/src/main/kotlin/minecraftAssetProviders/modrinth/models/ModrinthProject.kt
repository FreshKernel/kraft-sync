package minecraftAssetProviders.modrinth.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import syncInfo.models.mod.Mod.ModSupport

/**
 * From https://docs.modrinth.com/#tag/projects/operation/getProject
 * */
@Serializable
data class ModrinthProject(
    val slug: String,
    val title: String,
    val description: String,
    val categories: List<String>,
    @SerialName("client_side")
    val clientSide: ProjectSide,
    @SerialName("server_side")
    val serverSide: ProjectSide,
    val body: String,
    val status: ProjectStatus,
    @SerialName("requested_status")
    val requestedStatus: ProjectRequestedStatus? = null,
    @SerialName("additional_categories")
    val additionalCategories: List<String>? = null,
    @SerialName("issues_url")
    val issueUrl: String? = null,
    @SerialName("source_url")
    val sourceUrl: String? = null,
    @SerialName("wiki_url")
    val wikiUrl: String? = null,
    @SerialName("discord_url")
    val discordUrl: String? = null,
    @SerialName("donation_urls")
    val donationUrls: List<ProjectDonationURL>? = null,
    @SerialName("project_type")
    val projectType: ProjectType,
    val downloads: Int,
    @SerialName("icon_url")
    val iconUrl: String? = null,
    val color: Int? = null,
    @SerialName("thread_id")
    val threadId: String,
    @SerialName("monetization_status")
    val monetizationStatus: ProjectMonetizationStatus,
    val id: String,
    val team: String,
    val published: String,
    val updated: String,
    val approved: String? = null,
    val queued: String? = null,
    val followers: Int,
    val license: ProjectLicense,
    val versions: List<String>,
    @SerialName("game_versions")
    val gameVersions: List<String>,
    val loaders: List<String>,
    val gallery: List<GalleryImage>,
    val organization: String? = null,
    @Deprecated("Always null, Modrinth kept it for legacy compatibility.")
    @SerialName("body_url")
    val bodyUrl: String? = null,
    @Deprecated("Modrinth mark this as deprecated")
    @SerialName("moderator_message")
    val moderatorMessage: String? = null,
) {
    @Serializable
    data class ProjectLicense(
        val id: String,
        val name: String,
        val url: String? = null,
    )

    @Serializable
    enum class ProjectSide {
        @SerialName("required")
        Required,

        @SerialName("optional")
        Optional,

        @SerialName("unsupported")
        Unsupported,

        ;

        /**
         * @return Convert [ProjectSide] to [ModSupport] which is data-specific to the project
         * */
        fun toModSupport(): ModSupport =
            when (this) {
                Required -> ModSupport.Required
                Optional -> ModSupport.Optional
                Unsupported -> ModSupport.Unsupported
            }
    }

    @Serializable
    enum class ProjectStatus {
        @SerialName("approved")
        Approved,

        @SerialName("archived")
        Archived,

        @SerialName("rejected")
        Rejected,

        @SerialName("draft")
        Draft,

        @SerialName("unlisted")
        Unlisted,

        @SerialName("processing")
        Processing,

        @SerialName("withheld")
        Withheld,

        @SerialName("scheduled")
        Scheduled,

        @SerialName("private")
        Private,

        @SerialName("unknown")
        Unknown,
    }

    @Serializable
    enum class ProjectRequestedStatus {
        @SerialName("approved")
        Approved,

        @SerialName("archived")
        Archived,

        @SerialName("unlisted")
        Unlisted,

        @SerialName("private")
        Private,

        @SerialName("draft")
        Draft,
    }

    @Serializable
    data class ProjectDonationURL(
        val id: String,
        val platform: String,
        val url: String,
    )

    @Serializable
    enum class ProjectType {
        @SerialName("mod")
        Mod,

        @SerialName("modpack")
        ModPack,

        @SerialName("resourcepack")
        ResourcePack,

        @SerialName("shader")
        Shader,
    }

    @Serializable
    enum class ProjectMonetizationStatus {
        @SerialName("monetized")
        Monetized,

        @SerialName("demonetized")
        Demonetized,

        @SerialName("force-demonetized")
        ForceDemonetized,
    }

    @Serializable
    data class GalleryImage(
        val url: String,
        val featured: Boolean,
        val title: String? = null,
        val description: String? = null,
        val created: String,
        val ordering: Int,
    )
}
