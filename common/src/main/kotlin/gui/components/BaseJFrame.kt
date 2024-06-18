package gui.components

import gui.utils.GuiUtils
import javax.swing.JFrame

open class BaseJFrame : JFrame() {
    init {
        iconImage = GuiUtils.getIconAsImageIcon().image
    }
}
