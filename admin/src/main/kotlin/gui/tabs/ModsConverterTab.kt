package gui.tabs

import constants.AdminConstants
import constants.ProjectInfoConstants
import gui.Tab
import gui.components.HintTextField
import gui.components.HtmlTextWithLinks
import gui.components.instanceDirectoryLabeledInput
import gui.components.labeledInputPanel
import gui.utils.GuiUtils
import gui.utils.SwingDialogManager
import gui.utils.column
import gui.utils.getSelectedItemOrThrow
import gui.utils.onClick
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
import javax.swing.JTextField
import javax.swing.filechooser.FileNameExtensionFilter

class ModsConverterTab : Tab() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var launcherComboBox: JComboBox<MinecraftLauncher>
    private lateinit var launcherInstanceDirectoryTextField: JTextField
    private lateinit var modsConvertModeComboBox: JComboBox<ModsConvertMode>
    private lateinit var outputModeComboBox: JComboBox<ModsConvertOutputOption>

    private lateinit var prettyFormatCheckBox: JCheckBox

    init {
        setupTabContent()
    }

    companion object {
        /**
         * For [labeledInputPanel]
         * */
        private const val PREFERRED_LABEL_WIDTH = 140
    }

    override fun getTabContent(): JPanel =
        column(
            HtmlTextWithLinks {
                text("With ")
                link(ProjectInfoConstants.DISPLAY_NAME, ProjectInfoConstants.WEBSITE_URL)
                text(
                    " Admin, you can create an instance in your favorite Minecraft launcher and download your desired " +
                        "mods. Simply select the launcher and instance, and the utility will convert " +
                        "the mods' metadata into script format.",
                )
            },
            labeledInputPanel(
                labelText = "Launcher",
                tooltipText = "The Minecraft launcher to convert the info from.",
                inputComponent =
                    JComboBox<MinecraftLauncher>()
                        .apply {
                            MinecraftLauncher.entriesWithBuiltInModDownloadSupport().forEach { addItem(it) }
                        }.also { launcherComboBox = it },
                preferredLabelWidth = PREFERRED_LABEL_WIDTH,
            ),
            instanceDirectoryLabeledInput(
                textField = HintTextField(hintText = "Path").also { launcherInstanceDirectoryTextField = it },
                preferredLabelWidth = PREFERRED_LABEL_WIDTH,
                parentComponent = this@ModsConverterTab,
            ),
            labeledInputPanel(
                labelText = "Convert mode",
                tooltipText =
                    "Either copy the mods info as mods list or inside a new sync info. Notice that the " +
                        "second option is easier as it will reset the configurations if you have any.",
                inputComponent =
                    JComboBox<ModsConvertMode>()
                        .apply {
                            ModsConvertMode.entries.forEach { addItem(it) }
                        }.also { modsConvertModeComboBox = it },
                preferredLabelWidth = PREFERRED_LABEL_WIDTH,
            ),
            labeledInputPanel(
                labelText = "Output mode",
                tooltipText =
                    "Choose to either copy the output to your clipboard or save it as a file on your local system.",
                inputComponent =
                    JComboBox<ModsConvertOutputOption>()
                        .apply {
                            ModsConvertOutputOption.entries.forEach { addItem(it) }
                        }.also { outputModeComboBox = it },
                preferredLabelWidth = PREFERRED_LABEL_WIDTH,
            ),
            labeledInputPanel(
                labelText = "Pretty Format",
                tooltipText =
                    "Check if you want the format to be human-readable if you're planning on editing it manually. " +
                        "minifying the file can save up to 10-20% for larger data.",
                inputComponent = JCheckBox().also { prettyFormatCheckBox = it },
                preferredLabelWidth = PREFERRED_LABEL_WIDTH,
            ),
            JButton("Continue").onClick {
                convertMods(
                    curseForgeApiKeyOverride = null,
                    isCurseForgeForStudiosTermsOfServiceAccepted = false,
                )
            },
        )

    private fun convertMods(
        curseForgeApiKeyOverride: String?,
        isCurseForgeForStudiosTermsOfServiceAccepted: Boolean,
    ) {
        coroutineScope.launch {
            val result =
                ModsConverterInstance.convertMods(
                    launcher = launcherComboBox.getSelectedItemOrThrow(),
                    launcherInstanceDirectoryPath = launcherInstanceDirectoryTextField.text,
                    convertMode = modsConvertModeComboBox.getSelectedItemOrThrow(),
                    prettyFormat = prettyFormatCheckBox.isSelected,
                    curseForgeApiKeyOverride = curseForgeApiKeyOverride,
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
                        return@launch
                    }
                    val userCurseForgeApiKey: String? =
                        SwingDialogManager.showInputDialog(
                            title = "Curse Forge API Key",
                            message =
                                "Most launchers doesn't store the download url of the mods and instead store the project " +
                                    "and file id of the mod on Curse Forge. will attempt to send request to Curse Forge API to " +
                                    "get the info about the mod, the API key will needed to authenticate with Curse Forge. " +
                                    "We already provide an API key of our account to make the process easier with " +
                                    "less required steps. You can override the API Key if needed.",
                            parentComponent = this@ModsConverterTab,
                            selectionValues = null,
                            initialSelectionValue = null,
                        )
                    if (userCurseForgeApiKey == null) {
                        // User canceled the process
                        return@launch
                    }
                    convertMods(
                        curseForgeApiKeyOverride = userCurseForgeApiKey,
                        isCurseForgeForStudiosTermsOfServiceAccepted = true,
                    )
                }

                is ModsConvertResult.Success -> {
                    when (outputModeComboBox.getSelectedItemOrThrow()) {
                        ModsConvertOutputOption.CopyToClipboard -> {
                            result.modsOutputText.copyToClipboard()
                        }

                        ModsConvertOutputOption.SaveAsFile -> {
                            val outputFileChooser = JFileChooser()
                            outputFileChooser.dialogTitle = "The location where the file will be saved"
                            outputFileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
                            outputFileChooser.fileFilter = FileNameExtensionFilter("JSON Files", "json")
                            val dialogResult = outputFileChooser.showSaveDialog(this@ModsConverterTab)
                            if (dialogResult != JFileChooser.APPROVE_OPTION) {
                                return@launch
                            }
                            val outputFile = outputFileChooser.selectedFile
                            try {
                                outputFile.writeText(result.modsOutputText)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                GuiUtils.showErrorMessage(
                                    title = "Error Saving File ❌",
                                    message = "An error occurred while saving the file \uD83D\uDEE0: ${e.message}️",
                                    parentComponent = this@ModsConverterTab,
                                )
                                return@launch
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
}
