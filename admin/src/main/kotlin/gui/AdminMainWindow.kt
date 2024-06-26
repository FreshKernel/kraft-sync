package gui

import constants.ProjectInfoConstants
import gui.components.BaseJFrame
import gui.tabs.ModsConverterTab
import gui.tabs.SettingsTab
import gui.tabs.SyncScriptInstallerTab
import java.awt.Dimension
import javax.swing.JTabbedPane

class AdminMainWindow : BaseJFrame() {
    init {
        title = "${ProjectInfoConstants.DISPLAY_NAME} Admin"
        defaultCloseOperation = EXIT_ON_CLOSE

        val size = Dimension(450, 350)

        minimumSize = size
        preferredSize = size

        val tabbedPane = JTabbedPane()

        tabbedPane.addTab("Mods Info Converter", ModsConverterTab())
        tabbedPane.addTab("Installer", SyncScriptInstallerTab())
        tabbedPane.addTab("Settings", SettingsTab())

        add(tabbedPane)
        pack()
        setLocationRelativeTo(null)
    }
}
