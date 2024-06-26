package syncInfo.models

import config.models.ScriptConfig
import utils.getFileNameFromUrlOrError
import utils.showErrorMessageAndTerminate
import java.io.File
import kotlin.system.exitProcess

/**
 * The name that will be used to display in the console or GUI
 * will use the [Mod.name] which is the mod display name / title
 * will use [getFileNameFromUrlOrError] as an alternative if it's not available
 * */
fun Mod.getDisplayName(): String = name ?: getFileNameFromUrlOrError(downloadUrl)

/**
 * If this mod should be downloaded on the current [Environment].
 *
 * For example, if this is a client side-only mod and the environment is [Environment.Server],
 * it will return false
 * */
fun Mod.shouldSyncOnCurrentEnvironment(): Boolean {
    // TODO: Currently, this function will always download the mod even if it's optional on both client and server.
    //  Consider providing an option for the admin to decide if the optional mods or a specific mods will be downloaded
    return when (ScriptConfig.getInstanceOrThrow().environment) {
        Environment.Client ->
            when (clientSupport) {
                Mod.ModSupport.Required, Mod.ModSupport.Optional -> true
                Mod.ModSupport.Unsupported -> false
            }

        Environment.Server ->
            when (serverSupport) {
                Mod.ModSupport.Required, Mod.ModSupport.Optional -> true
                Mod.ModSupport.Unsupported -> false
            }
    }
}

/**
 * Allow overriding the value for a specific mod, or all the mods, or use a global value for all the assets.
 * */
fun Mod.shouldVerifyFileIntegrity(): Boolean =
    overrideShouldVerifyFileIntegrity ?: SyncInfo.instance.shouldVerifyModFilesIntegrity
        ?: SyncInfo.instance.shouldVerifyAssetFilesIntegrity

suspend fun Mod.hasValidFileIntegrityOrError(modFile: File): Boolean? =
    this.fileIntegrityInfo.hasValidIntegrity(file = modFile).getOrElse {
        showErrorMessageAndTerminate(
            title = "File Integrity Validation Error ⚠️",
            message = "An error occurred while validating the integrity of the mod file (${modFile.name}) \uD83D\uDCC1.",
        )
        // This will never reach due to the previous statement stopping the application
        exitProcess(0)
    }
