package gui.tabs

import constants.AdminConstants
import constants.ProjectInfoConstants
import gui.Tab
import gui.components.HintTextField
import gui.components.HtmlTextWithLinks
import gui.components.filePicker
import gui.components.labeledInputPanel
import gui.utils.GuiUtils
import gui.utils.SwingDialogManager
import gui.utils.column
import gui.utils.getCurrentWindowFrame
import gui.utils.getSelectedItemOrThrow
import gui.utils.onClick
import gui.utils.padding
import gui.utils.row
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import launchers.MinecraftLauncher
import minecraftAssetProviders.MinecraftAssetProvider
import services.modsConverter.ModsConvertError
import services.modsConverter.ModsConvertResult
import services.modsConverter.ModsConverterInstance
import services.modsConverter.models.ModsConvertMode
import services.modsConverter.models.ModsConvertOutputOption
import utils.copyToClipboard
import java.awt.Dimension
import java.awt.Frame
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JFileChooser
import javax.swing.JPanel
import javax.swing.JTextField
import javax.swing.filechooser.FileNameExtensionFilter

class ModsConverterTab : Tab() {
    init {
        setupTabContent()
    }

    override fun getTabContent(): JPanel =
        row(
            HtmlTextWithLinks {
                text("With ")
                link(ProjectInfoConstants.DISPLAY_NAME, ProjectInfoConstants.WEBSITE_URL)
                text(
                    " Admin, you can create an instance in your favorite Minecraft launcher and download your desired " +
                        "mods. Simply select the launcher and instance, and the utility will convert " +
                        "the mods' metadata into script format.",
                )
                newLines(2)
                text("Most launchers do not store the download URLs for mods downloaded from Curse Forge. ")
                text("The application attempts to retrieve this information by sending a network request to the ")
                link(labelText = "Curse Forge", linkUrl = MinecraftAssetProvider.CurseForge.link)
                text(" API. Curse Forge needs an API key, we created one to make the process easier. ")
                text("For a faster, offline experience with fewer errors and no reliance on network connectivity, ")
                text("we recommend using alternative providers like ")
                link("Modrinth", MinecraftAssetProvider.Modrinth.link)
            }.padding(10, 10, 10, 10),
            JButton("Convert")
                .onClick {
                    ConversionInputDialog(this.getCurrentWindowFrame()).isVisible = true
                },
        )
}

private class ConversionInputDialog(
    owner: Frame,
) : JDialog(owner) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private lateinit var launcherComboBox: JComboBox<MinecraftLauncher>
    private lateinit var launcherInstanceDirectoryTextField: JTextField
    private lateinit var modsConvertModeComboBox: JComboBox<ModsConvertMode>
    private lateinit var outputModeComboBox: JComboBox<ModsConvertOutputOption>

    private lateinit var prettyFormatCheckBox: JCheckBox
    private lateinit var closeDialogOnCompletionCheckBox: JCheckBox

    init {
        title = "Convert Mods Info"
        defaultCloseOperation = DISPOSE_ON_CLOSE
        minimumSize = Dimension(400, 275)
        maximumSize = Dimension(600, 300)
        add(getContent())
        setLocationRelativeTo(owner)
        pack()
    }

    companion object {
        /**
         * For [labeledInputPanel]
         * */
        private const val PREFERRED_LABEL_WIDTH = 140
    }

    private fun getContent(): JComponent =
        column(
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
            labeledInputPanel(
                labelText = "Instance directory",
                tooltipText = "The Minecraft instance directory that has the mods folder inside it.",
                inputComponent =
                    filePicker(
                        filePathTextField =
                            HintTextField(hintText = "Path").also {
                                launcherInstanceDirectoryTextField = it
                            },
                        fileChooser =
                            JFileChooser().apply {
                                dialogTitle = "Choose the instance directory"
                                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                            },
                        onErrorWhileChoosingFile = {
                            GuiUtils.showErrorMessage(
                                title = "Unexpected Error",
                                message = "An error occurred while trying to pick the launcher instance directory.",
                                parentComponent = this@ConversionInputDialog,
                            )
                        },
                    ),
                preferredLabelWidth = PREFERRED_LABEL_WIDTH,
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
            labeledInputPanel(
                labelText = "Close Dialog",
                tooltipText =
                    "Check to close the dialog once the conversation finishes. This option doesn't affect the converted data.",
                inputComponent =
                    JCheckBox().apply {
                        isSelected = true
                        closeDialogOnCompletionCheckBox = this
                    },
                preferredLabelWidth = PREFERRED_LABEL_WIDTH,
            ),
            JButton("Continue").onClick {
                convertMods(
                    curseForgeApiKeyOverride = null,
                    isCurseForgeForStudiosTermsOfServiceAccepted = false,
                )
            },
        ) {
            padding(10, 10, 10, 10)
        }

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
                                parentComponent = this@ConversionInputDialog,
                            )
                        }

                        ModsConvertError.LauncherInstanceDirectoryNotFound -> {
                            GuiUtils.showErrorMessage(
                                title = "❌ Directory Not Found",
                                message = "It seems like the selected instance directory doesn't exist. 📁",
                                parentComponent = this@ConversionInputDialog,
                            )
                        }

                        is ModsConvertError.InvalidLauncherInstanceDirectory -> {
                            GuiUtils.showErrorMessage(
                                title = "❌ Incorrect instance path",
                                message =
                                    "It seems that the provided instance path might be incorrect: ${result.error.message}",
                                parentComponent = this@ConversionInputDialog,
                            )
                        }

                        is ModsConvertError.CurseForgeApiCheckError -> {
                            GuiUtils.showErrorMessage(
                                title = "❌ Unexpected error",
                                message =
                                    "An error occurred while checking if Curse Forge API HTTP request is needed " +
                                        "\uD83D\uDEE0: ${result.error.message}️",
                                parentComponent = this@ConversionInputDialog,
                            )
                        }

                        is ModsConvertError.ModsAvailabilityCheckError -> {
                            GuiUtils.showErrorMessage(
                                title = "❌ Mods availability check error",
                                message = "An error occurred while checking if the instance has mods: ${result.error.message}️",
                                parentComponent = this@ConversionInputDialog,
                            )
                        }

                        is ModsConvertError.CouldNotConvertMods -> {
                            GuiUtils.showErrorMessage(
                                title = "❌ Error Converting Mods",
                                message = "An error occurred while converting the mods \uD83D\uDEE0: ${result.error.message}️",
                                parentComponent = this@ConversionInputDialog,
                            )
                        }

                        is ModsConvertError.ModsUnavailable -> {
                            GuiUtils.showErrorMessage(
                                title = "Mods Data Unavailable",
                                message =
                                    if (result.error.happenedWhileConvertingMods) {
                                        "Could not find the mods' info while converting the mods"
                                    } else {
                                        "The mods' info couldn't be found"
                                    } +
                                        ". Double-check to see if you have some mods installed" +
                                        " on the selected instance.",
                                parentComponent = this@ConversionInputDialog,
                            )
                        }

                        is ModsConvertError.UnknownError -> {
                            GuiUtils.showErrorMessage(
                                title = "❌ Unexpected error",
                                message = "A unknown error occurred: ${result.error.message}\uFE0F",
                                parentComponent = this@ConversionInputDialog,
                            )
                        }
                    }
                }

                ModsConvertResult.RequiresAcceptanceOfCurseForgeForStudiosTermsOfUse -> {
                    val hasUserAcceptedCurseForgeForStudiosTermsOfUse =
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
                                parentComponent = this@ConversionInputDialog,
                                messageType = SwingDialogManager.MessageType.Question,
                            ).isConfirmed()
                    if (!hasUserAcceptedCurseForgeForStudiosTermsOfUse) {
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
                            parentComponent = this@ConversionInputDialog,
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
                            val dialogResult = outputFileChooser.showSaveDialog(this@ConversionInputDialog)
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
                                    parentComponent = this@ConversionInputDialog,
                                )
                                return@launch
                            }
                        }
                    }
                    SwingDialogManager.showMessageDialog(
                        title = "Success",
                        message = "The task has been successfully finished.",
                        parentComponent = this@ConversionInputDialog,
                    )
                    if (closeDialogOnCompletionCheckBox.isSelected) {
                        dispose()
                    }
                }
            }
        }
    }
}
