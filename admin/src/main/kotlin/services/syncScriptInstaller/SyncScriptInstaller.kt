package services.syncScriptInstaller

import launchers.MinecraftLauncher

interface SyncScriptInstaller {
    /**
     * @param installationConfig Either install or uninstall
     * */
    suspend fun configureInstallation(
        installationConfig: SyncScriptInstallationConfig,
        launcher: MinecraftLauncher,
        launcherInstanceDirectoryPath: String,
        confirmReplaceExistingPreLaunchCommand: Boolean,
    ): SyncScriptInstallationResult
}
