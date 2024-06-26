package launchers

import launchers.atLauncher.ATLauncherDataSource
import launchers.modrinth.ModrinthLauncherDataSource
import launchers.prismLauncher.PrismLauncherDataSource

object LauncherDataSourceFactory {
    private val handlers = mutableMapOf<MinecraftLauncher, LauncherDataSource>()

    fun getHandler(launcher: MinecraftLauncher): LauncherDataSource {
        // TODO: Implement missing and add support for more later
        return handlers.computeIfAbsent(launcher) {
            when (launcher) {
                MinecraftLauncher.Official -> TODO()
                MinecraftLauncher.MultiMc -> TODO()
                MinecraftLauncher.GDLauncher -> TODO()
                MinecraftLauncher.PrismLauncher -> PrismLauncherDataSource()
                MinecraftLauncher.ModrinthApp -> ModrinthLauncherDataSource()
                MinecraftLauncher.ATLauncher -> ATLauncherDataSource()
            }
        }
    }

    // TODO: Might remove this once implement the missing above
    fun getHandlerOrNull(launcher: MinecraftLauncher): LauncherDataSource? =
        kotlin.runCatching { getHandler(launcher = launcher) }.getOrNull()
}
