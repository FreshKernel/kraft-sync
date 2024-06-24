package launchers.atLauncher

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import minecraftAssetProviders.modrinth.models.ModrinthProject.ProjectSide
import syncInfo.models.FileIntegrityInfo

/**
 * From https://github.com/ATLauncher/ATLauncher/blob/master/src/main/java/com/atlauncher/data/Instance.java#L178
 * */
@Serializable
data class ATLauncherInstance(
    val launcher: Launcher,
) {
    @Serializable
    data class Launcher(
        val mods: List<Mod>,
        val enableCommands: Boolean = false,
        val preLaunchCommand: String? = null,
        val postExitCommand: String? = null,
        val wrapperCommand: String? = null,
    ) {
        /**
         * ATLauncher identify the resource-packs, shaders, JAR mods and others as a mods
         * which is why it has a [type]
         * */
        @Serializable
        data class Mod(
            val name: String,
            val version: String,
            val optional: Boolean,
            @SerialName("file")
            val fileName: String,
            val type: Type,
            val description: String,
            val disabled: Boolean,
            val userAdded: Boolean,
            val wasSelected: Boolean,
            val skipped: Boolean,
            val curseForgeProjectId: Int? = null,
            val curseForgeFileId: Int? = null,
            val curseForgeProject: CurseForgeProject? = null,
            val curseForgeFile: CurseForgeFile? = null,
            // ATLauncher might not store Modrinth data if the mod is available on Curse Forge and not on Modrinth
            val modrinthProject: ModrinthProject? = null,
            val modrinthVersion: ModrinthVersion? = null,
        ) {
            @Serializable
            enum class Type {
                @SerialName("jar")
                Jar,

                @SerialName("dependency")
                Dependency,

                @SerialName("forge")
                Forge,

                @SerialName("mcpc")
                McPc,

                @SerialName("worlds")
                Worlds,

                @SerialName("mods")
                Mods,

                @SerialName("plugins")
                Plugins,

                @SerialName("ic2lib")
                Ic2lib,

                @SerialName("denlib")
                DenLib,

                @SerialName("flan")
                Flan,

                @SerialName("coremods")
                CoreMods,

                @SerialName("extract")
                Extract,

                @SerialName("decomp")
                DeComp,

                @SerialName("millenaire")
                Millenaire,

                @SerialName("texturepack")
                TexturePack,

                @SerialName("resourcepack")
                ResourcePack,

                @SerialName("texturepackextract")
                TexturePacketExtract,

                @SerialName("resourcepackextract")
                ResourcePackExtract,

                @SerialName("shaderpack")
                ShaderPack,
            }

            @Serializable
            data class CurseForgeProject(
                val id: Int,
                val name: String,
                val gameId: Int,
                val summary: String,
                val mainFileId: Int,
            )

            @Serializable
            data class CurseForgeFile(
                val id: Int,
                val gameId: Int,
                val isAvailable: Boolean,
                val displayName: String,
                val fileName: String,
                val fileLength: Int,
                val isServerPack: Boolean,
                val serverPackFileId: Int? = null,
                val gameVersions: List<String>,
                val modId: Int,
            )

            @Serializable
            data class ModrinthProject(
                val id: String,
                val title: String,
                val description: String,
                val body: String,
                val published: String,
                @SerialName("client_side")
                val clientSide: ProjectSide,
                @SerialName("server_side")
                val serverSide: ProjectSide,
            )

            @Serializable
            data class ModrinthVersion(
                val id: String,
                @SerialName("project_id")
                val projectId: String,
                @SerialName("version_number")
                val versionNumber: String,
                val changelog: String,
                @SerialName("version_type")
                val versionType: ModrinthChannel,
                val files: List<ModrinthFile>,
                val dependencies: List<ModrinthDependency>,
                @SerialName("game_versions")
                val gameVersions: List<String>,
                val loaders: List<String>,
            ) {
                @Serializable
                enum class ModrinthChannel {
                    @SerialName("release")
                    Release,

                    @SerialName("beta")
                    Beta,

                    @SerialName("alpha")
                    Alpha,
                }

                @Serializable
                data class ModrinthFile(
                    val hashes: Map<String, String>,
                    val url: String,
                    @SerialName("filename")
                    val fileName: String,
                    val primary: Boolean,
                    val size: Long,
                ) {
                    fun getFileIntegrityInfo() =
                        FileIntegrityInfo(
                            sha1 = hashes.getOrDefault("sha1", null),
                            sha256 = hashes.getOrDefault("sha256", null),
                            sha512 = hashes.getOrDefault("sha512", null),
                            sizeInBytes = size,
                        )
                }

                @Serializable
                data class ModrinthDependency(
                    @SerialName("project_id")
                    val projectId: String,
                    @SerialName("dependency_type")
                    val dependencyType: ModrinthDependencyType,
                ) {
                    @Serializable
                    enum class ModrinthDependencyType {
                        @SerialName("required")
                        Required,

                        @SerialName("optional")
                        Optional,
                    }
                }
            }
        }
    }
}
