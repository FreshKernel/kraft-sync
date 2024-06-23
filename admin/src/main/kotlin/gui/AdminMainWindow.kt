package gui

import constants.ProjectInfoConstants
import gui.components.BaseJFrame
import gui.tabs.ModsConverterTab
import gui.tabs.SettingsTab
import gui.tabs.SetupTab
import java.awt.Dimension
import javax.swing.JTabbedPane

class AdminMainWindow : BaseJFrame() {
    init {
        title = "${ProjectInfoConstants.DISPLAY_NAME} Admin"
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(450, 250)
        maximumSize = Dimension(800, 350)

        val tabbedPane = JTabbedPane()

        tabbedPane.addTab("Mods Info Converter", ModsConverterTab())
        tabbedPane.addTab("Setup", SetupTab())
        tabbedPane.addTab("Settings", SettingsTab())

        add(tabbedPane)
        pack()
        setLocationRelativeTo(null)
    }
}
