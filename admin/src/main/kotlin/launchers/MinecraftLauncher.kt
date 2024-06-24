package launchers

enum class MinecraftLauncher(
    private val launcherName: String,
    val link: String,
    val supportsBuiltInModDownload: Boolean,
    /**
     * If the launcher allows running a command before starting the game once the launch process start.
     *
     * Also known as hooks.
     * */
    val supportsCustomCommands: Boolean,
    /**
     * If each instance data is separated from the other, each one has it own worlds, server list, mods, resource packs.
     * */
    val supportsInstanceDataSeparation: Boolean,
) {
    Official(
        launcherName = "Minecraft Launcher",
        link = "https://www.minecraft.net/download",
        supportsBuiltInModDownload = false,
        supportsCustomCommands = false,
        supportsInstanceDataSeparation = false,
    ),
    MultiMc(
        launcherName = "MultiMC",
        link = "https://multimc.org/",
        supportsBuiltInModDownload = false,
        supportsCustomCommands = true,
        supportsInstanceDataSeparation = true,
    ),
    ModrinthApp(
        launcherName = "Modrinth App",
        link = "https://modrinth.com/app",
        supportsBuiltInModDownload = true,
        supportsCustomCommands = true,
        supportsInstanceDataSeparation = true,
    ),
    PrismLauncher(
        launcherName = "Prism Launcher",
        link = "https://prismlauncher.org/",
        supportsBuiltInModDownload = true,
        supportsCustomCommands = true,
        supportsInstanceDataSeparation = true,
    ),
    ATLauncher(
        launcherName = "ATLauncher",
        link = "https://atlauncher.com/",
        supportsBuiltInModDownload = true,
        supportsCustomCommands = true,
        supportsInstanceDataSeparation = true,
    ),
    GDLauncher(
        launcherName = "GDLauncher",
        link = "https://gdlauncher.com/",
        supportsBuiltInModDownload = true,
        supportsCustomCommands = true,
        supportsInstanceDataSeparation = true,
    ),
    ;

    companion object {
        fun entriesWithBuiltInModDownloadSupport() = entries.filter { it.supportsBuiltInModDownload }

        /**
         * If the launcher has the features that are required to provide optimal experience.
         * */
        fun entriesWithOptimalDataSyncSupport() = entries.filter { it.supportsCustomCommands && it.supportsInstanceDataSeparation }
    }

    override fun toString(): String = launcherName
}
