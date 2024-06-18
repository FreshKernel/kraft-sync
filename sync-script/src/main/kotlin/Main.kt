import config.data.LocalJsonScriptConfigDataSource
import config.data.ScriptConfigDataSource
import config.models.ScriptConfig
import constants.Constants
import constants.SyncScriptInstanceFiles
import gui.GuiState
import gui.dialogs.CreateScriptConfigDialog
import gui.dialogs.QuickPreferencesDialog
import gui.dialogs.TrustAdminDialog
import gui.utils.GuiUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import preferences.ScriptPreferencesDataSource
import preferences.SystemScriptPreferencesDataSource
import syncInfo.data.RemoteSyncInfoDataSource
import syncInfo.data.SyncInfoDataSource
import syncInfo.models.SyncInfo
import syncInfo.models.instance
import syncService.ModsSyncService
import syncService.SyncService
import utils.ExecutionTimer
import utils.HttpService
import utils.SystemInfoProvider
import utils.os.LinuxDesktopEnvironment
import utils.os.OperatingSystem
import utils.showErrorMessageAndTerminate
import utils.terminateWithOrWithoutError
import java.awt.GraphicsEnvironment
import kotlin.system.exitProcess

val scriptConfigDataSource: ScriptConfigDataSource = LocalJsonScriptConfigDataSource()
val scriptPreferencesDataSource: ScriptPreferencesDataSource = SystemScriptPreferencesDataSource()

lateinit var passedArgs: Array<String>

suspend fun main(args: Array<String>) {
    val applicationExecutionTimer = ExecutionTimer()
    applicationExecutionTimer.setStartTime()

    passedArgs = args

    println("\uD83D\uDCC1 Current working directory: ${SystemInfoProvider.getCurrentWorkingDirectoryPath()}")

    when (OperatingSystem.current) {
        OperatingSystem.Linux -> "\uD83D\uDC27 You are using Linux with the desktop environment ${LinuxDesktopEnvironment.current}. Enjoy!"
        OperatingSystem.MacOS -> "\uD83C\uDF4F You are using macOS \uF8FF. Welcome to the world of Apple \uD83C\uDF4E."
        OperatingSystem.Windows -> "\uD83E\uDE9F You are using Windows. Be safe!"
        OperatingSystem.Unknown -> "❓Your operating system couldn't be identified. Let's hope everything works smoothly."
    }.also { println(it) }

    if (!GraphicsEnvironment.isHeadless()) {
        if (GuiUtils.isSystemInDarkMode) {
            "🌙 Welcome to the dark side! Your system is in dark mode. Enjoy the soothing darkness! 🌃"
        } else {
            "☀️ Brighten up your day! Your system is in the light mode. Embrace the light! 🌅"
        }.also { println(it) }
    }

    SyncScriptInstanceFiles.SyncScriptData.Temp.file.apply {
        if (exists()) {
            println(
                "ℹ\uFE0F The temporary folder: $path exist. " +
                    "The script might not finished last time. Removing the folder.",
            )
            deleteRecursively()
        }
    }

    // The script config file is not initialized/loaded yet, calling this is necessary to set the value

    GuiState.updateIsGuiEnabled()

    // TODO: Currently calling this before loading the config file to load default theme in :
    //  A empty window is being created in non GUI Mode,
    //  only when using it in the config file because the config file hasn't loaded yet¬
    //  also we should test this on the server or systems doesn't support GUI
    // Initially use a default theme to use in case of error while loading the config or creating it
    if (GuiState.isGuiEnabled) {
        GuiUtils.setupSwingGui()
        GuiUtils.applyThemeIfNeeded(
            theme = null,
            themeMode = null,
        )
    }

    // Loading the script config file from json file

    val scriptConfigFile = SyncScriptInstanceFiles.SyncScriptData.ScriptConfig.file
    if (!scriptConfigFile.exists()) {
        if (GuiState.isGuiEnabled) {
            println(
                "Configuration Missing! ⚠\uFE0F. Since you're in GUI mode, we'll launch a quick " +
                    "dialog to gather the necessary information",
            )

            val scriptConfig = CreateScriptConfigDialog().showDialog()
            runBlocking { scriptConfigDataSource.replaceConfig(scriptConfig) }

            // No need to set the value of the script config due to the statement after the current one

            // Don't update `doesUserTrustSource` to true,
            // as the user might have downloaded the script and set up it manually
            // doesn't necessarily mean he/she is the admin
        } else {
            // TODO: Might request the data using readln()
            showErrorMessageAndTerminate(
                title = "Configuration Missing! ⚠\uFE0F",
                message =
                    """
                    The script configuration file, `${scriptConfigFile.path}`, couldn't be found. 

                    To get started, create this file in the same directory where you're running the script or 
                    in the working directory. 

                    This file will contain important settings for the script to function properly. ⚙️
                    """.trimIndent(),
            )
        }
    }

    val scriptConfig =
        scriptConfigDataSource.getConfig().getOrElse {
            it.printStackTrace()
            showErrorMessageAndTerminate(
                title = "Configuration Error ⚠\uFE0F",
                message =
                    buildString {
                        append("An error occurred while parsing your script configuration file (${scriptConfigFile.path})\n\n")
                        append("Ensure it's valid JSON format.\n\n")
                        append("Error details: ${it.message?.trim()}")
                    },
            )
            return
        }
    ScriptConfig.instance = scriptConfig

    // The script config has been loaded
    // Allow overriding and use the one from the config instead of the launching args

    GuiState.updateIsGuiEnabled()

    println("ℹ\uFE0F Script Configuration: ${ScriptConfig.instance}")

    // This will not show as the script is configured to disable GUI mode on headless environments

    // Switch to the themes specified by config
    if (GuiState.isGuiEnabled) {
        GuiUtils.applyThemeIfNeeded(
            theme = ScriptConfig.getInstanceOrThrow().theme,
            themeMode = ScriptConfig.getInstanceOrThrow().themeMode,
        )
    }

    // TODO: Plan if we should implement this in non GUI mode
    if (GuiState.isGuiEnabled && !SyncScriptInstanceFiles.SyncScriptData.IsPreferencesConfigured.file.exists()) {
        val newScriptConfig = QuickPreferencesDialog().showDialog()

        scriptConfigDataSource.replaceConfig(newScriptConfig).getOrElse {
            showErrorMessageAndTerminate(
                title = "Error updating the config file \uD83D\uDEA8",
                message =
                    "⚠ An error occurred while updating the config file.",
            )
            return
        }
        ScriptConfig.instance = newScriptConfig

        // The script config has been updated, update isGuiEnabled
        GuiState.updateIsGuiEnabled()

        withContext(Dispatchers.IO) {
            SyncScriptInstanceFiles.SyncScriptData.IsPreferencesConfigured.file.createNewFile()
        }
    }

    // Fetch the sync info data from the url

    val syncInfoDataSource: SyncInfoDataSource =
        RemoteSyncInfoDataSource(
            client = HttpService.client,
        )
    // TODO: Might want to add a loading screen for this if taking longer than expected
    SyncInfo.instance =
        syncInfoDataSource.fetchSyncInfo(
            url = ScriptConfig.getInstanceOrThrow().syncInfoUrl,
        ).getOrElse {
            showErrorMessageAndTerminate(
                title = "Sync Info Unavailable \uD83D\uDD04",
                message = "An error occurred while trying to fetch sync info from the server: ${it.message}",
            )
            return
        }

    // Make sure the user trusts the admin

    // TODO: Plan on how we will implement this in non GUI mode
    if (GuiState.isGuiEnabled && Constants.Features.TRUST_ADMIN_ENABLED) {
        val currentDoesUserTrustThisSource =
            scriptPreferencesDataSource.doesUserTrustSource(ScriptConfig.getInstanceOrThrow().syncInfoUrl)
                .getOrElse {
                    showErrorMessageAndTerminate(
                        title = "Error Loading Data \uD83D\uDED1",
                        message =
                            "It seems we ran into an error while verifying if this is your first time using the" +
                                "script from this source. \uD83D\uDED1",
                    )
                    return
                }
        if (!currentDoesUserTrustThisSource) {
            // If the user doesn't say he/she trusts the admin yet, then we will ask him
            val doesUserTrustThisSource = TrustAdminDialog.showDialog()
            if (!doesUserTrustThisSource) {
                println(
                    "\uD83D\uDD12 Script is closing because trust in the administration is lacking. " +
                        "Feel free to reach out if you have any concerns.",
                )
                terminateWithOrWithoutError()
            }
            runBlocking {
                scriptPreferencesDataSource.updateDoesUserTrustSource(
                    ScriptConfig.getInstanceOrThrow().syncInfoUrl,
                    // Should be true
                    doesUserTrustThisSource,
                ).getOrThrow()
            }
        }
    }

    // Use sync services to sync the content

    val syncServices: List<SyncService> =
        listOf(
            ModsSyncService(),
        )

    syncServices.forEach { it.syncData() }

    // Finally, finish the script

    // The temporary folder usually contains the downloaded files which will be moved once finished
    // after finish syncing the contents successfully, we don't need it anymore.
    SyncScriptInstanceFiles.SyncScriptData.Temp.file.apply {
        if (exists()) {
            println("\uD83D\uDEAB Deleting the temporary folder: '$path' (no longer needed).")
            deleteRecursively()
        }
    }

    println(
        "\n\uD83C\uDF89 The script has successfully completed in " +
            "(${applicationExecutionTimer.getRunningUntilNowDuration().inWholeMilliseconds}ms)! \uD83D\uDE80",
    )
    exitProcess(0)
}
