package utils

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

fun String.copyToClipboard() {
    Toolkit.getDefaultToolkit().systemClipboard
        .setContents(StringSelection(this), null)
}
