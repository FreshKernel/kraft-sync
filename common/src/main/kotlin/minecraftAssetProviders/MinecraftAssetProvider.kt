package minecraftAssetProviders

/**
 * A class that contains information about the asset providers, that provide mods, resource-packs and more to download for Minecraft
 *
 *
 * @param link The link for this provider webpage
 * @param downloadSources The downloaded sources which is used to get the [MinecraftAssetProvider] by the url
 * @param providerName The name of the provider
 * @param isKnownProvider If the provider is known or commonly used
 * */
sealed class MinecraftAssetProvider(
    open val link: String,
    open val downloadSources: List<String>,
    open val providerName: String,
    open val isKnownProvider: Boolean = false,
) {
    data object Modrinth :
        MinecraftAssetProvider(
            link = "https://www.modrinth.com/",
            downloadSources = listOf("https://cdn.modrinth.com/"),
            providerName = "Modrinth",
            isKnownProvider = true,
        )

    data object CurseForge : MinecraftAssetProvider(
        link = "https://www.curseforge.com/",
        downloadSources = listOf("https://edge.forgecdn.net/", "https://mediafilez.forgecdn.net"),
        providerName = "Curse Forge",
        isKnownProvider = true,
    )

    data class Unknown(
        override val link: String,
        override val downloadSources: List<String>,
        override val providerName: String,
    ) : MinecraftAssetProvider(link = link, downloadSources = downloadSources, providerName = providerName)

    companion object {
        val knownProviders: List<MinecraftAssetProvider> by lazy { listOf(Modrinth, CurseForge) }
    }
}
