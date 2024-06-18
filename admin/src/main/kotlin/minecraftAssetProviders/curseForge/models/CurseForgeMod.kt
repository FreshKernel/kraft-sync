package minecraftAssetProviders.curseForge.models

import kotlinx.serialization.Serializable

/**
 * From https://docs.curseforge.com/#tocS_Mod
 * */
@Serializable
data class CurseForgeMod(
    val id: Int,
    val gameId: Int,
    val name: String,
    val slug: String,
    val links: ModLinks,
    val summary: String,
    /**
     * Represent [ModStatus] as integer
     * */
    val status: Int,
    val downloadCount: Long,
    val isFeatured: Boolean,
    val primaryCategoryId: Int,
    val categories: List<Category>,
    val classId: Int? = null,
    val authors: List<ModAuthor>,
    val logo: ModAsset,
    val screenshots: List<ModAsset>,
    val mainFileId: Int,
    val latestFiles: List<CurseForgeFile>,
    val latestFilesIndexes: List<FileIndex>,
    val latestEarlyAccessFilesIndexes: List<FileIndex>,
    val dateCreated: String,
    val dateModified: String,
    val dateReleased: String,
    val allowModDistribution: Boolean? = null,
    val gamePopularityRank: Int,
    val isAvailable: Boolean,
    val thumbsUpCount: Int,
    val rating: Double? = null,
) {
    @Serializable
    data class ModLinks(
        val websiteUrl: String,
        val wikiUrl: String,
        val issuesUrl: String,
        val sourceUrl: String,
    )

    @Serializable
    enum class ModStatus(val value: Int) {
        New(1),
        ChangesRequired(2),
        UnderSoftReview(3),
        Approved(4),
        Rejected(5),
        ChangesMade(6),
        Inactive(7),
        Abandoned(8),
        Deleted(9),
        UnderReview(10),
    }

    @Serializable
    data class Category(
        val id: Int,
        val gameId: Int,
        val name: String,
        val slug: String,
        val url: String,
        val iconUrl: String,
        val dateModified: String,
        val isClass: Boolean? = null,
        val classId: Int? = null,
        val parentCategoryId: Int? = null,
        val displayIndex: Int? = null,
    )

    @Serializable
    data class ModAuthor(
        val id: Int,
        val name: String,
        val url: String,
    )

    @Serializable
    data class ModAsset(
        val id: Int,
        val modId: Int,
        val title: String,
        val description: String,
        val thumbnailUrl: String,
        val url: String,
    )

    @Serializable
    data class FileIndex(
        val gameVersion: String,
        val fileId: Int,
        val filename: String,
        /**
         * Represent [CurseForgeFile.FileReleaseType] as integer
         * */
        val releaseType: Int,
        val gameVersionTypeId: Int? = null,
        /**
         * Represent [ModLoaderType] as integer
         * */
        val modLoader: Int,
    )

    @Serializable
    enum class ModLoaderType(val value: Int) {
        Any(0),
        Forge(1),
        Cauldron(2),
        LiteLoader(3),
        Fabric(4),
        Quilt(5),
        NeoForge(6),
    }
}
