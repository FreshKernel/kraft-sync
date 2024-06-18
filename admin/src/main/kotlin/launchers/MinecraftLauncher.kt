package launchers

enum class MinecraftLauncher(
    private val launcherName: String,
    // TODO: Might move this somewhere else
    val hasModDownloadSupport: Boolean,
    val link: String,
) {
    Official("Minecraft Launcher", false, "https://www.minecraft.net/download"),
    MultiMc("MultiMC", false, "https://multimc.org/"),
    ModrinthApp("Modrinth App", true, "https://modrinth.com/app"),
    PrismLauncher("Prism Launcher", true, "https://prismlauncher.org/"),
    ATLauncher("ATLauncher", true, "https://atlauncher.com/"),
    GDLauncher("GDLauncher", true, "https://gdlauncher.com/"),
    ;

    companion object {
        fun entriesWithModDownloadSupport() = entries.filter { it.hasModDownloadSupport }
    }

    override fun toString(): String = launcherName
}
