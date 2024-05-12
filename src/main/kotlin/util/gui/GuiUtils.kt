package util.gui

import Constants
import util.getResourceURL
import javax.swing.ImageIcon
import javax.swing.JOptionPane

object GuiUtils {
    fun showErrorMessage(
        title: String,
        message: String,
    ) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE, getImageIcon())
    }

    /**
     * The icon for the script
     * */
    fun getImageIcon(): ImageIcon {
        val icon = ImageIcon(getResourceURL(Constants.SCRIPT_ICON_FILE_NAME))
        return icon
    }
}
