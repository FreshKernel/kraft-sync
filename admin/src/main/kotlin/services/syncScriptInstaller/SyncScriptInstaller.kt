package services.syncScriptInstaller

import launchers.MinecraftLauncher

interface SyncScriptInstaller {
    /**
     * @param installationConfig Either install or uninstall
     * */
    suspend fun configureInstallation(
        installationConfig: SyncScriptInstallationConfig,
        launcher: MinecraftLauncher,
        launcherInstanceDirectoryPathString: String,
        confirmReplaceExistingPreLaunchCommand: Boolean,
        shouldEnableGui: Boolean,
    ): SyncScriptInstallationResult
}
