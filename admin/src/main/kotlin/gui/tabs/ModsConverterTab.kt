package gui.tabs

import constants.AdminConstants
import constants.ProjectInfoConstants
import gui.Tab
import gui.components.HtmlTextWithLinks
import gui.components.instanceDirectoryInputField
import gui.components.labeledInputField
import gui.utils.ComboItem
import gui.utils.GuiUtils
import gui.utils.SwingDialogManager
import gui.utils.column
import gui.utils.getSelectedItemOrThrow
import gui.utils.handleResult
import gui.utils.onClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import launchers.Instance
import launchers.MinecraftLauncher
import services.modsConverter.ModsConvertError
import services.modsConverter.ModsConvertResult
import services.modsConverter.ModsConverterInstance
import services.modsConverter.models.ModsConvertMode
import services.modsConverter.models.ModsConvertOutputOption
import utils.buildHtml
import utils.copyToClipboard
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JFileChooser
import javax.swing.JPanel
import javax.swing.filechooser.FileNameExtensionFilter

class ModsConverterTab : Tab() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val launcherComboBox: JComboBox<MinecraftLauncher> = JComboBox()
    private val launcherInstanceDirectoryComboBox: JComboBox<ComboItem<Instance>> = JComboBox()
    private val modsConvertModeComboBox: JComboBox<ModsConvertMode> = JComboBox()
    private val outputModeComboBox: JComboBox<ModsConvertOutputOption> = JComboBox()

    private val prettyFormatCheckBox: JCheckBox = JCheckBox()

    init {
        setupTabContent()
    }

    companion object {
        /**
         * For [labeledInputField]
         * */
        private const val PREFERRED_LABEL_WIDTH = 140
    }

    override fun getTabContent(): JPanel =
        column(
            HtmlTextWithLinks {
                link(ProjectInfoConstants.DISPLAY_NAME, ProjectInfoConstants.WEBSITE_URL)
                text(
                    " Admin can convert the downloaded mods' info from your Minecraft Launcher into the script format. ",
                )
                boldText("Ensure the launcher is closed, as some launchers save changes upon exiting.")
            },
            labeledInputField(
                labelText = "Launcher",
                tooltipText = "The Minecraft launcher to convert the info from.",
                inputComponent =
                    launcherComboBox
                        .apply {
                            MinecraftLauncher.entriesWithBuiltInModDownloadSupport().forEach { addItem(it) }
                        },
                preferredLabelWidth = PREFERRED_LABEL_WIDTH,
            ),
            instanceDirectoryInputField(
                inputComboBox = launcherInstanceDirectoryComboBox,
                launcherComboBox = launcherComboBox,
                parentComponent = this@ModsConverterTab,
                preferredLabelWidth = PREFERRED_LABEL_WIDTH,
            ),
            labeledInputField(
                labelText = "Convert mode",
                tooltipText =
                    "Either copy the mods info as mods list or inside a new sync info. Notice that the " +
                        "second option is easier as it will reset the configurations if you have any.",
                inputComponent =
                    modsConvertModeComboBox
                        .apply {
                            ModsConvertMode.entries.forEach { addItem(it) }
                        },
                preferredLabelWidth = PREFERRED_LABEL_WIDTH,
            ),
            labeledInputField(
                labelText = "Output mode",
                tooltipText =
                    "Choose to either copy the output to your clipboard or save it as a file on your local system.",
                inputComponent =
                    outputModeComboBox
                        .apply {
                            ModsConvertOutputOption.entries.forEach { addItem(it) }
                        },
                preferredLabelWidth = PREFERRED_LABEL_WIDTH,
            ),
            labeledInputField(
                labelText = "Pretty Format",
                tooltipText =
                    "Check if you want the file format to be human-readable if you're planning on editing it manually. " +
                        "minifying the file can save up to 10-20% for larger data.",
                inputComponent = prettyFormatCheckBox,
                preferredLabelWidth = PREFERRED_LABEL_WIDTH,
            ),
            JButton("Convert").onClick {
                coroutineScope.launch {
                    convertMods(
                        overrideCurseForgeApiKey = null,
                        isCurseForgeForStudiosTermsOfServiceAccepted = false,
                    )
                }
            },
        )

    private suspend fun convertMods(
        overrideCurseForgeApiKey: String?,
        isCurseForgeForStudiosTermsOfServiceAccepted: Boolean,
    ) {
        val result =
            ModsConverterInstance.convertMods(
                launcher = launcherComboBox.getSelectedItemOrThrow(),
                // TODO: Throw an exception with a readable message instead
                launcherInstanceDirectoryPath = launcherInstanceDirectoryComboBox.selectedItem as String,
                convertMode = modsConvertModeComboBox.getSelectedItemOrThrow(),
                prettyFormat = prettyFormatCheckBox.isSelected,
                overrideCurseForgeApiKey = overrideCurseForgeApiKey,
                isCurseForgeForStudiosTermsOfServiceAccepted = isCurseForgeForStudiosTermsOfServiceAccepted,
            )
        when (result) {
            is ModsConvertResult.Failure -> {
                when (result.error) {
                    ModsConvertError.EmptyLauncherInstanceDirectoryPath -> {
                        GuiUtils.showErrorMessage(
                            title = "🚫 Empty Directory Path",
                            message = "The instance directory path is needed to proceed.",
                            parentComponent = this@ModsConverterTab,
                        )
                    }

                    ModsConvertError.LauncherInstanceDirectoryNotFound -> {
                        GuiUtils.showErrorMessage(
                            title = "❌ Directory Not Found",
                            message = "It seems like the selected instance directory doesn't exist. 📁",
                            parentComponent = this@ModsConverterTab,
                        )
                    }

                    is ModsConvertError.InvalidLauncherInstanceDirectory -> {
                        GuiUtils.showErrorMessage(
                            title = "❌ Incorrect instance path",
                            message =
                                "It seems that the provided instance path might be incorrect: ${result.error.message}",
                            parentComponent = this@ModsConverterTab,
                        )
                    }

                    is ModsConvertError.CurseForgeApiCheckError -> {
                        GuiUtils.showErrorMessage(
                            title = "❌ Unexpected error",
                            message =
                                "An error occurred while checking if Curse Forge API HTTP request is needed " +
                                    "\uD83D\uDEE0: ${result.error.message}️",
                            parentComponent = this@ModsConverterTab,
                        )
                    }

                    is ModsConvertError.ModsAvailabilityCheckError -> {
                        GuiUtils.showErrorMessage(
                            title = "❌ Mods availability check error",
                            message = "An error occurred while checking if the instance has mods: ${result.error.message}️",
                            parentComponent = this@ModsConverterTab,
                        )
                    }

                    is ModsConvertError.CouldNotConvertMods -> {
                        GuiUtils.showErrorMessage(
                            title = "❌ Error Converting Mods",
                            message = "An error occurred while converting the mods \uD83D\uDEE0: ${result.error.message}️",
                            parentComponent = this@ModsConverterTab,
                        )
                    }

                    is ModsConvertError.ModsUnavailable -> {
                        GuiUtils.showErrorMessage(
                            title = "Mods Data Unavailable",
                            message =
                                buildHtml {
                                    if (result.error.happenedWhileConvertingMods) {
                                        text("Could not find the mods' info while converting the mods.")
                                    } else {
                                        text("The mods' info couldn't be found.")
                                    }
                                    newLine()
                                    text(
                                        "Double-check to see if you have some mods installed" +
                                            " on the selected instance.",
                                    )
                                    newLines(2)
                                    text(
                                        "Some launchers might save the changes after closing the launcher/app.",
                                    )
                                    newLine()
                                    text("If you created the instance/profile recently, try closing the launcher and try again.")
                                }.buildBodyAsText(),
                            parentComponent = this@ModsConverterTab,
                        )
                    }

                    is ModsConvertError.UnknownError -> {
                        GuiUtils.showErrorMessage(
                            title = "❌ Unexpected error",
                            message = "A unknown error occurred: ${result.error.message}\uFE0F",
                            parentComponent = this@ModsConverterTab,
                        )
                    }
                }
            }

            ModsConvertResult.RequiresAcceptanceOfCurseForgeForStudiosTermsOfUse -> {
                val hasAcceptedCurseForgeForStudiosTermsOfUse =
                    SwingDialogManager
                        .showConfirmDialog(
                            title = "CurseForge for Studios Terms of Use",
                            message =
                                HtmlTextWithLinks {
                                    text(
                                        "You're using Curse Forge mods in the launcher, a network request must sent to Curse Forge API",
                                    )
                                    newLine()
                                    text("to fetch the mods data as the selected launcher doesn't store the mod download URLs.")
                                    newLine()
                                    newLine()

                                    boldText("Do you agree to ")
                                    link(
                                        labelText = "Curse Forge API Terms of Service",
                                        linkUrl = AdminConstants.CURSE_FORGE_FOR_STUDIOS_TERMS_OF_SERVICE_URL,
                                    )
                                    boldText("?")
                                    newLine()
                                    text("The link can also be found here:")
                                    newLine()
                                    link(
                                        labelText = AdminConstants.CURSE_FORGE_FOR_STUDIOS_TERMS_OF_SERVICE_URL,
                                        linkUrl = AdminConstants.CURSE_FORGE_FOR_STUDIOS_TERMS_OF_SERVICE_URL,
                                    )
                                },
                            parentComponent = this@ModsConverterTab,
                            messageType = SwingDialogManager.MessageType.Question,
                        ).isConfirmed()
                if (!hasAcceptedCurseForgeForStudiosTermsOfUse) {
                    return
                }
                val userCurseForgeApiKey: String? =
                    SwingDialogManager.showInputDialog(
                        title = "Curse Forge API Key",
                        message =
                            "Most launchers don't store the download URL of the mods and instead store the project " +
                                "and file id of the mod on Curse Forge. will attempt to send a request to Curse Forge API to " +
                                "get the info about the mod, the API key will needed to authenticate with Curse Forge. " +
                                "We already provided an API key to make the process easier with " +
                                "less required steps. You can override the API Key if needed.",
                        parentComponent = this@ModsConverterTab,
                        selectionValues = null,
                        initialSelectionValue = null,
                    )
                if (userCurseForgeApiKey == null) {
                    // User canceled the process
                    return
                }
                convertMods(
                    overrideCurseForgeApiKey = userCurseForgeApiKey,
                    isCurseForgeForStudiosTermsOfServiceAccepted = true,
                )
            }

            is ModsConvertResult.Success -> {
                when (outputModeComboBox.getSelectedItemOrThrow()) {
                    ModsConvertOutputOption.CopyToClipboard -> {
                        result.modsOutputText.copyToClipboard()
                    }

                    ModsConvertOutputOption.SaveAsFile -> {
                        val outputFileChooser =
                            JFileChooser().apply {
                                dialogTitle = "The location where the file will be saved"
                                fileSelectionMode = JFileChooser.FILES_ONLY
                                fileFilter = FileNameExtensionFilter("JSON Files", "json")
                            }

                        val filePickResult = outputFileChooser.showSaveDialog(this@ModsConverterTab)

                        val outputFile =
                            outputFileChooser.handleResult(
                                result = filePickResult,
                                onErrorWhileChoosingFile = {},
                            ) ?: return
                        try {
                            outputFile.writeText(result.modsOutputText)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            GuiUtils.showErrorMessage(
                                title = "Error Saving File ❌",
                                message = "An error occurred while saving the file \uD83D\uDEE0: ${e.message}️",
                                parentComponent = this@ModsConverterTab,
                            )
                            return
                        }
                    }
                }
                SwingDialogManager.showMessageDialog(
                    title = "Success",
                    message = "The task has been successfully finished.",
                    parentComponent = this@ModsConverterTab,
                )
            }
        }
    }
}
