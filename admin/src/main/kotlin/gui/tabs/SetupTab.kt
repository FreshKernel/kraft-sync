package gui.tabs

import gui.Tab
import javax.swing.JComponent
import javax.swing.JLabel

class SetupTab : Tab() {
    init {
        setupTabContent()
    }

    override fun getTabContent(): JComponent {
        return JLabel("Empty for now")
    }
}
