package services.syncScriptInstaller

sealed class SyncScriptInstallationError {
    data object EmptyLauncherInstanceDirectoryPath : SyncScriptInstallationError()

    data object LauncherInstanceDirectoryNotFound : SyncScriptInstallationError()

    data class InvalidLauncherInstanceDirectory(
        val message: String,
        val exception: Throwable,
    ) : SyncScriptInstallationError()

    data object EmptySyncScriptJarFilePath : SyncScriptInstallationError()

    data object SyncScriptJarFileNotFound : SyncScriptInstallationError()

    data object CouldNotDeleteSyncScriptJarFileWhileUninstall : SyncScriptInstallationError()

    data object CouldNotDeleteSyncScriptDataWhileUninstall : SyncScriptInstallationError()

    data class CouldNotSetPreLaunchCommand(
        val message: String,
        val exception: Throwable,
    ) : SyncScriptInstallationError()

    data class UnknownError(
        val message: String,
        val exception: Exception,
    ) : SyncScriptInstallationError()
}
