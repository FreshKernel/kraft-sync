package launchers.modrinth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import minecraftAssetProviders.modrinth.models.ModrinthProject.ProjectSide
import minecraftAssetProviders.modrinth.models.ModrinthProject.ProjectType
import minecraftAssetProviders.modrinth.models.ModrinthVersion.VersionFile

// Also known as profile
@Serializable
data class ModrinthLauncherInstance(
    val uuid: String,
    val path: String,
    val metadata: Metadata,
    val projects: Map<String, ModrinthLauncherProject>,
    val hooks: Hooks? = null,
) {
    @Serializable
    data class Metadata(
        val name: String,
        val groups: List<String>,
        @SerialName("game_version")
        val gameVersion: String,
        val loader: String,
        @SerialName("loader_version")
        val loaderVersion: LoaderVersion? = null,
        @SerialName("date_created")
        val dateCreated: String,
        @SerialName("date_modified")
        val dateModified: String,
        @SerialName("last_played")
        val lastPlayed: String,
        @SerialName("submitted_time_played")
        val submittedTimePlayed: Int,
        @SerialName("recent_time_played")
        val recentTimePlayed: Int,
    ) {
        @Serializable
        data class LoaderVersion(
            val id: String,
            val url: String,
            val stable: Boolean,
        )
    }

    @Serializable
    data class ModrinthLauncherProject(
        val sha512: String,
        val disabled: Boolean,
        val metadata: Metadata,
        @SerialName("file_name")
        val fileName: String,
    ) {
        @Serializable
        data class Metadata(
            val type: String,
            val project: ModrinthProject,
            val version: ModrinthVersion,
        )

        @Serializable
        data class ModrinthProject(
            val title: String,
            val description: String,
            @SerialName("client_side")
            val clientSide: ProjectSide,
            @SerialName("server_side")
            val serverSide: ProjectSide,
            val body: String,
            @SerialName("project_type")
            val projectType: ProjectType,
            val downloads: Int,
        )

        @Serializable
        data class ModrinthVersion(
            val name: String,
            val files: List<VersionFile>,
        )
    }

    @Serializable
    data class Hooks(
        @SerialName("pre_launch")
        val preLaunch: String? = null,
        val wrapper: String? = null,
        @SerialName("post_exit")
        val postExit: String? = null,
    )
}
