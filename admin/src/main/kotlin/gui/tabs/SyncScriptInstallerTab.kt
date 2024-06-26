package gui.tabs

import constants.ProjectInfoConstants
import gui.Tab
import gui.components.instanceDirectoryInputField
import gui.components.labeledInputField
import gui.utils.ComboItem
import gui.utils.GuiUtils
import gui.utils.SwingDialogManager
import gui.utils.column
import gui.utils.getSelectedItemOrThrow
import gui.utils.handleResult
import gui.utils.onClick
import gui.utils.padding
import gui.utils.row
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import launchers.Instance
import launchers.MinecraftLauncher
import services.syncScriptInstaller.SyncScriptInstallationConfig
import services.syncScriptInstaller.SyncScriptInstallationError
import services.syncScriptInstaller.SyncScriptInstallationResult
import services.syncScriptInstaller.SyncScriptInstallerInstance
import utils.buildHtml
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JFileChooser
import javax.swing.JLabel
import javax.swing.filechooser.FileNameExtensionFilter

class SyncScriptInstallerTab : Tab() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val launcherInstanceDirectoryComboBox: JComboBox<ComboItem<Instance>> = JComboBox()
    private val launcherComboBox: JComboBox<MinecraftLauncher> = JComboBox()

    init {
        setupTabContent()
    }

    companion object {
        /**
         * For [labeledInputField]
         * */
        private const val PREFERRED_LABEL_WIDTH = 140
    }

    override fun getTabContent(): JComponent =
        column(
            JLabel(
                buildHtml {
                    text("Install or uninstall the sync script.")
                    newLine()
                    boldText("Make sure the launcher is closed to avoid losing the changes.")
                }.buildBodyAsText(),
            ).padding(bottom = 16),
            labeledInputField(
                labelText = "Launcher",
                tooltipText = "The Minecraft launcher to convert the info from.",
                inputComponent =
                    launcherComboBox
                        .apply {
                            MinecraftLauncher.entriesWithOptimalDataSyncSupport().forEach { addItem(it) }
                        },
                preferredLabelWidth = PREFERRED_LABEL_WIDTH,
            ),
            instanceDirectoryInputField(
                inputComboBox = launcherInstanceDirectoryComboBox,
                launcherComboBox = launcherComboBox,
                preferredLabelWidth = PREFERRED_LABEL_WIDTH,
                parentComponent = this@SyncScriptInstallerTab,
            ).padding(bottom = 24),
            row(
                JButton("Install").onClick {
                    coroutineScope.launch {
                        configureInstallation(
                            installationConfig =
                                SyncScriptInstallationConfig.Install(
                                    getSyncScriptJarFilePath = {
                                        val fileChooser =
                                            JFileChooser().apply {
                                                dialogTitle = "Choose the JAR File for the sync script."
                                                fileSelectionMode = JFileChooser.FILES_ONLY
                                                fileFilter = FileNameExtensionFilter("JAR Files", "jar")
                                            }
                                        val result = fileChooser.showOpenDialog(this@SyncScriptInstallerTab)
                                        val selectedFile =
                                            fileChooser.handleResult(
                                                result = result,
                                                onErrorWhileChoosingFile = {},
                                            ) ?: return@Install null
                                        selectedFile.path
                                    },
                                ),
                            confirmReplaceExistingPreLaunchCommand = false,
                        )
                    }
                },
                JButton("Uninstall").onClick {
                    coroutineScope.launch {
                        configureInstallation(
                            installationConfig = SyncScriptInstallationConfig.UnInstall,
                            confirmReplaceExistingPreLaunchCommand = false,
                        )
                    }
                },
            ),
        )

    private suspend fun configureInstallation(
        installationConfig: SyncScriptInstallationConfig,
        confirmReplaceExistingPreLaunchCommand: Boolean,
    ) {
        val result =
            SyncScriptInstallerInstance.configureInstallation(
                installationConfig = installationConfig,
                launcherInstanceDirectoryPath =
                    (launcherInstanceDirectoryComboBox.selectedItem as? String) ?: throw IllegalStateException(
                        "The selected item of ${::launcherInstanceDirectoryComboBox.name} is null",
                    ),
                launcher = launcherComboBox.getSelectedItemOrThrow(),
                confirmReplaceExistingPreLaunchCommand = confirmReplaceExistingPreLaunchCommand,
            )
        when (result) {
            is SyncScriptInstallationResult.Failure -> {
                when (result.error) {
                    SyncScriptInstallationError.EmptyLauncherInstanceDirectoryPath -> {
                        GuiUtils.showErrorMessage(
                            title = "🚫 Empty Directory Path",
                            message = "The instance directory path is needed to proceed.",
                            parentComponent = this@SyncScriptInstallerTab,
                        )
                    }

                    SyncScriptInstallationError.LauncherInstanceDirectoryNotFound -> {
                        GuiUtils.showErrorMessage(
                            title = "❌ Directory Not Found",
                            message = "It seems like the selected instance directory doesn't exist. 📁",
                            parentComponent = this@SyncScriptInstallerTab,
                        )
                    }

                    is SyncScriptInstallationError.InvalidLauncherInstanceDirectory -> {
                        GuiUtils.showErrorMessage(
                            title = "❌ Incorrect instance path",
                            message =
                                "It seems that the provided instance path might be incorrect: ${result.error.message}",
                            parentComponent = this@SyncScriptInstallerTab,
                        )
                    }

                    SyncScriptInstallationError.EmptySyncScriptJarFilePath -> {
                        GuiUtils.showErrorMessage(
                            title = "🚫 Empty Directory Path",
                            message = "The sync script JAR file path is needed to proceed.",
                            parentComponent = this@SyncScriptInstallerTab,
                        )
                    }

                    SyncScriptInstallationError.SyncScriptJarFileNotFound -> {
                        GuiUtils.showErrorMessage(
                            title = "❌ File Not Found",
                            message = "It seems like the selected sync script JAR file doesn't exist. 📁",
                            parentComponent = this@SyncScriptInstallerTab,
                        )
                    }

                    SyncScriptInstallationError.CouldNotDeleteSyncScriptJarFileWhileUninstall -> {
                        GuiUtils.showErrorMessage(
                            title = "❌ Unexpected error",
                            message = "An error occurred while deleting the sync script JAR file.",
                            parentComponent = this@SyncScriptInstallerTab,
                        )
                    }

                    SyncScriptInstallationError.CouldNotDeleteSyncScriptDataWhileUninstall -> {
                        GuiUtils.showErrorMessage(
                            title = "❌ Unexpected error",
                            message = "An error occurred while deleting the sync script data \uD83D\uDCC1.",
                            parentComponent = this@SyncScriptInstallerTab,
                        )
                    }

                    is SyncScriptInstallationError.CouldNotSetPreLaunchCommand -> {
                        GuiUtils.showErrorMessage(
                            title = "❌ Unexpected error",
                            message =
                                "An error occurred while updating the Pre Launch command/hook: \uD83D\uDEE0: ${result.error.message}",
                            parentComponent = this@SyncScriptInstallerTab,
                        )
                    }

                    is SyncScriptInstallationError.UnknownError -> {
                        GuiUtils.showErrorMessage(
                            title = "❌ Unexpected error",
                            message = "A unknown error occurred: ${result.error.message}\uFE0F",
                            parentComponent = this@SyncScriptInstallerTab,
                        )
                    }
                }
            }

            SyncScriptInstallationResult.Success -> {
                SwingDialogManager.showMessageDialog(
                    title = "Success",
                    message =
                        when (installationConfig) {
                            is SyncScriptInstallationConfig.Install ->
                                "You can launch the the game using the provided instance/profile."

                            SyncScriptInstallationConfig.UnInstall ->
                                "${ProjectInfoConstants.DISPLAY_NAME} has been removed form the provided instance/profile."
                        },
                    parentComponent = this@SyncScriptInstallerTab,
                )
            }

            is SyncScriptInstallationResult.RequiresUserConfirmationToReplacePreLaunchCommand -> {
                val hasConfirmedPreLaunchCommandReplacement =
                    SwingDialogManager
                        .showConfirmDialog(
                            title = "Pre-Launch Command Conflict",
                            message =
                                buildHtml {
                                    text("Pre-Launch command is already set.")
                                    newLine()
                                    text("Current Command: ${result.existingCommand}")
                                    newLine()
                                    text("New command: ${result.newCommand}")
                                    newLines(2)
                                    boldText("Would you like to replace it with the new one?")
                                }.buildBodyAsText(),
                            parentComponent = this@SyncScriptInstallerTab,
                            messageType = SwingDialogManager.MessageType.Question,
                        ).isConfirmed()
                if (!hasConfirmedPreLaunchCommandReplacement) {
                    return
                }
                configureInstallation(
                    installationConfig = installationConfig,
                    confirmReplaceExistingPreLaunchCommand = true,
                )
            }

            SyncScriptInstallationResult.Cancelled -> Unit
        }
    }
}
