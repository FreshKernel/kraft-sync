package gui.utils

import java.io.File
import javax.swing.JFileChooser

/**
 * @return The selected file
 * */
fun JFileChooser.handleResult(
    result: Int,
    onErrorWhileChoosingFile: () -> Unit,
): File? {
    when (result) {
        JFileChooser.CANCEL_OPTION -> {
            return null
        }

        JFileChooser.ERROR_OPTION -> {
            onErrorWhileChoosingFile()
            return null
        }

        JFileChooser.APPROVE_OPTION -> {
            return selectedFile
        }
    }
    return null
}
