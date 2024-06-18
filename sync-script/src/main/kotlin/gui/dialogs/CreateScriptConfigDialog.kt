package gui.dialogs

import config.models.ScriptConfig
import gui.components.HintTextField
import gui.components.labeledInputPanel
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

// TODO: When the user enter incorrect url or doesn't return the expected result, I should handle it to use this dialog again
// TODO: Allow the user to enter a different url in case if it's not found ot return unexpected/incorrect response

/**
 * Enter only the required data to create the [ScriptConfig] in addition to optional data that will usually be configured
 * by the admin such as [ScriptConfig.environment], the player usually doesn't need to configure it
 * */
class CreateScriptConfigDialog : JDialog() {
    private lateinit var syncInfoUrlTextField: JTextField
    private lateinit var environmentComboBox: JComboBox<Environment>

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
            labeledInputPanel(
                labelText = "Sync URL",
                tooltipText = "The URL that will be used to get the information from in order to start the sync process.",
                inputComponent =
                    HintTextField(hintText = "URL").also { syncInfoUrlTextField = it },
            ),
            labeledInputPanel(
                labelText = "Environment",
                tooltipText =
                    "Will be used to have different sync logic, for example shaders and resource-packs is " +
                        "not supported on the server.",
                inputComponent =
                    JComboBox<Environment>()
                        .apply {
                            Environment.entries.forEach { addItem(it) }
                            selectedItem = Environment.Client
                        }
                        .also { environmentComboBox = it },
            ),
            JButton("Continue").onClick {
                // TODO: Extract all the logic in here to support non GUI mode
                if (syncInfoUrlTextField.text.isBlank()) {
                    GuiUtils.showErrorMessage(
                        title = "🚫 Empty URL",
                        message = "We need a URL to continue 🌐",
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
