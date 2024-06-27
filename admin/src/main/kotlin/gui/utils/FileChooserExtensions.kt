package gui.utils

import java.nio.file.Path
import javax.swing.JFileChooser

/**
 * @return The selected file
 * */
fun JFileChooser.handleResult(
    result: Int,
    onErrorWhileChoosingFile: () -> Unit,
): Path? {
    when (result) {
        JFileChooser.CANCEL_OPTION -> {
            return null
        }

        JFileChooser.ERROR_OPTION -> {
            onErrorWhileChoosingFile()
            return null
        }

        JFileChooser.APPROVE_OPTION -> {
            return selectedFile.toPath()
        }
    }
    return null
}
