package gui.components

import gui.utils.onClick
import gui.utils.row
import javax.swing.JButton
import javax.swing.JFileChooser
import javax.swing.JTextField

fun filePicker(
    filePathTextField: JTextField,
    fileChooser: JFileChooser,
    onErrorWhileChoosingFile: () -> Unit,
) = row(
    filePathTextField,
    JButton("Browse").onClick {
        val result = fileChooser.showOpenDialog(filePathTextField)
        when (result) {
            JFileChooser.CANCEL_OPTION -> {
                return@onClick
            }

            JFileChooser.ERROR_OPTION -> {
                onErrorWhileChoosingFile()
            }
        }

        filePathTextField.text = fileChooser.selectedFile.path
    },
)
