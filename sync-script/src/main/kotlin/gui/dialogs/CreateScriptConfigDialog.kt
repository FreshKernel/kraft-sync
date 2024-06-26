package gui.dialogs

import config.models.ScriptConfig
import gui.components.HintTextField
import gui.components.labeledInputField
import gui.utils.GuiUtils
import gui.utils.column
import gui.utils.getSelectedItemOrThrow
import gui.utils.onClick
import gui.utils.padding
import syncInfo.models.Environment
import utils.isValidUrl
import utils.terminateWithOrWithoutError
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JDialog
import javax.swing.JTextField

/**
 * Enter only the required data to create the [ScriptConfig] in addition to optional data that will usually be configured
 * by the admin such as [ScriptConfig.environment], the player usually doesn't need to configure it
 * */
class CreateScriptConfigDialog : JDialog() {
    private val syncInfoUrlTextField: JTextField = HintTextField(hintText = "URL")
    private val environmentComboBox: JComboBox<Environment> = JComboBox()

    init {
        title = "Missing Configuration"
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(
            object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    super.windowClosing(e)
                    terminateWithOrWithoutError()
                }
            },
        )
        setModalityType(ModalityType.APPLICATION_MODAL)
        minimumSize = Dimension(400, 150)
        maximumSize = Dimension(600, 180)
        add(getContent())
        pack()
        setLocationRelativeTo(null)
    }

    private fun getContent(): JComponent {
        return column(
            labeledInputField(
                labelText = "Sync URL",
                tooltipText = "The URL that will be used to get the information from in order to start the sync process.",
                inputComponent = syncInfoUrlTextField,
            ),
            labeledInputField(
                labelText = "Environment",
                tooltipText =
                    "Will be used to have different sync logic, for example shaders and resource-packs is " +
                        "not supported on the server. Depending on the admin configuration, the script also might install" +
                        " mods for the selected environment only.",
                inputComponent =
                    environmentComboBox
                        .apply {
                            Environment.entries.forEach { addItem(it) }
                            selectedItem = Environment.Client
                        },
            ),
            JButton("Continue").onClick {
                // TODO: Extract all the logic in here to support non GUI mode
                if (syncInfoUrlTextField.text.isBlank()) {
                    GuiUtils.showErrorMessage(
                        title = "🚫 Empty URL",
                        message = "Provide the sync URL to proceed 🌐",
                        parentComponent = this,
                    )
                    return@onClick
                }
                if (!syncInfoUrlTextField.text.isValidUrl()) {
                    GuiUtils.showErrorMessage(
                        title = "⚠️ Invalid URL",
                        message = "The provided URL is invalid ❌.",
                        parentComponent = this,
                    )
                    return@onClick
                }
                dispose()
            },
        ) {
            padding(10, 10, 10, 10)
        }
    }

    fun showDialog(): ScriptConfig {
        isVisible = true
        return ScriptConfig(
            syncInfoUrl = syncInfoUrlTextField.text,
            environment = environmentComboBox.getSelectedItemOrThrow(),
        )
    }
}
