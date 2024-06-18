package launchers.prismLauncher

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import syncInfo.models.FileIntegrityInfo
import syncInfo.models.Mod

/**
 * Prism launcher stores the mod info inside a folder [PrismLauncherDataSource.MODS_METADATA_FOLDER_NAME] which is
 * in the `mods` folder
 *
 * This data class represents the data as TOML
 *
 * From https://github.com/PrismLauncher/PrismLauncher/tree/develop/launcher/minecraft/mod
 * */
@Serializable
data class PrismLauncherModMetadata(
    @SerialName("filename")
    val fileName: String,
    val name: String = "",
    val side: ModSide = ModSide.Both,
    val download: Download,
    val update: Update,
) {
    @Serializable
    enum class ModSide {
        @SerialName("client")
        Client,

        @SerialName("server")
        Server,

        @SerialName("both")
        Both,

        ;

        /**
         * @return a [Pair] where the [Pair.first] is the [Mod.clientSupport] and [Pair.second] is [Mod.serverSupport]
         *
         * Note: This mapping assumes the mod is required when present on the respective side
         * due to the lack of explicit optional support information.
         * */
        fun toClientServerModSupport(): Pair<Mod.ModSupport, Mod.ModSupport> {
            return when (this) {
                Client -> Pair(Mod.ModSupport.Required, Mod.ModSupport.Unsupported)
                Server -> Pair(Mod.ModSupport.Unsupported, Mod.ModSupport.Required)
                Both -> Pair(Mod.ModSupport.Required, Mod.ModSupport.Required)
            }
        }
    }

    @Serializable
    data class Download(
        val hash: String,
        @SerialName("hash-format")
        val hashFormat: String,
        val mode: String,
        /**
         * The url might be empty because it might not be stored when using some asset providers
         * */
        val url: String = "",
    ) {
        fun getFileIntegrityInfo() =
            FileIntegrityInfo(
                sha1 = if (hashFormat == "sha1") hash else null,
                sha256 = if (hashFormat == "sha256") hash else null,
                sha512 = if (hashFormat == "sha512") hash else null,
                sizeInBytes = null,
            )
    }

    @Serializable
    data class Update(
        val modrinth: Modrinth? = null,
        @SerialName("curseforge")
        val curseForge: CurseForge? = null,
    ) {
        @Serializable
        data class Modrinth(
            @SerialName("mod-id") val modId: String,
            val version: String,
        )

        @Serializable
        data class CurseForge(
            @SerialName("file-id") val fileId: Int,
            @SerialName("project-id") val projectId: Int,
        )
    }
}
