package services.syncScriptInstaller

sealed class SyncScriptInstallationResult {
    data object Success : SyncScriptInstallationResult()

    data class Failure(
        val error: SyncScriptInstallationError,
    ) : SyncScriptInstallationResult()

    /**
     * This state indicates that a Pre-Launch command already exists, but it was not created by this application.
     * User confirmation is required before replacing the existing Pre-Launch command.
     * */
    data class RequiresUserConfirmationToReplacePreLaunchCommand(
        val existingCommand: String,
        val newCommand: String,
    ) : SyncScriptInstallationResult()

    data object Cancelled : SyncScriptInstallationResult()
}
