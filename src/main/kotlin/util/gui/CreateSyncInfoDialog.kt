package util.gui

import java.awt.FlowLayout
import javax.swing.JDialog
import javax.swing.JTextField
import javax.swing.JButton
import javax.swing.WindowConstants
import javax.swing.JPanel
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import ScriptConfig
import util.terminateWithError

class CreateSyncInfoDialog : JDialog() {
    private val textField: JTextField
    private val acceptButton: JButton
    private val closeButton: JButton

    init {
        title = "Enter Sync Info Url"
        isModal = true
        // TODO: Should do the same thing as the close button
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        val panel = JPanel(FlowLayout())

        textField = JTextField(20)

        acceptButton = JButton("Accept")
        closeButton = JButton("Close")

        acceptButton.addActionListener {
            val url = textField.text
            val config = ScriptConfig(syncInfoUrl = url)
            val configFile = Constants.MinecraftInstanceFiles.MinecraftSyncData.ScriptConfig.file
            if (!configFile.parentFile.exists()) {
                configFile.parentFile.mkdirs()
            }
            configFile.writeText(Json.encodeToString(config))
            dispose()
        }

        closeButton.addActionListener {
            dispose()
            println(
                "Closing the script as requested data wasn't provided 🙃.",
            )
            terminateWithError()
        }

        panel.add(textField)
        panel.add(acceptButton)
        panel.add(closeButton)

        contentPane = panel
        pack()

        setLocationRelativeTo(null)
    }

    fun showDialog() {
        isVisible = true
    }
}
