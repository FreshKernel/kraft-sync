package launchers

import launchers.atLauncher.ATLauncherDataSource
import launchers.modrinth.ModrinthLauncherDataSource
import launchers.prismLauncher.PrismLauncherDataSource

object LauncherDataSourceFactory {
    private val handlers = mutableMapOf<MinecraftLauncher, LauncherDataSource>()

    fun getHandler(minecraftLauncher: MinecraftLauncher): LauncherDataSource {
        // TODO: Implement missing and add support for more later
        return handlers.computeIfAbsent(minecraftLauncher) {
            when (minecraftLauncher) {
                MinecraftLauncher.Official -> TODO()
                MinecraftLauncher.MultiMc -> TODO()
                MinecraftLauncher.GDLauncher -> TODO()
                MinecraftLauncher.PrismLauncher -> PrismLauncherDataSource()
                MinecraftLauncher.ModrinthApp -> ModrinthLauncherDataSource()
                MinecraftLauncher.ATLauncher -> ATLauncherDataSource()
            }
        }
    }
}
