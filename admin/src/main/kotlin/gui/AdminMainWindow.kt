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
        minimumSize = Dimension(300, 300)
        preferredSize = Dimension(450, 350)

        val tabbedPane = JTabbedPane()

        tabbedPane.addTab("Mods Info Converter", ModsConverterTab())
        tabbedPane.addTab("Installer", SyncScriptInstallerTab())
        tabbedPane.addTab("Settings", SettingsTab())

        add(tabbedPane)
        pack()
        setLocationRelativeTo(null)
    }
}
