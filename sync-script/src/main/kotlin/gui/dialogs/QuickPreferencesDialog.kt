package gui.dialogs

import config.models.ScriptConfig
import constants.SharedConstants
import gui.GuiState
import gui.components.labeledInputField
import gui.theme.Theme
import gui.theme.ThemeMode
import gui.utils.GuiUtils
import gui.utils.column
import gui.utils.getSelectedItemOrThrow
import gui.utils.onClick
import gui.utils.onItemChanged
import gui.utils.padding
import gui.utils.setSelectedItemSafe
import utils.buildHtml
import utils.terminateWithOrWithoutError
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JDialog

/**
 * The player or the admin will usually use this to update the preferences.
 * This data is optional unlike [CreateScriptConfigDialog]
 * */
class QuickPreferencesDialog : JDialog() {
    private val themeComboBox: JComboBox<Theme> = JComboBox()
    private val themeModeComboBox: JComboBox<ThemeMode> = JComboBox()
    private val overrideIsGuiEnabledCheckBox: JCheckBox = JCheckBox()

    init {
        title = "Preferences"
        defaultCloseOperation = DO_NOTHING_ON_CLOSE
        addWindowListener(
            object : WindowAdapter() {
                override fun windowClosing(e: WindowEvent?) {
                    super.windowClosing(e)
                    terminateWithOrWithoutError()
                }
            },
        )
        setModalityType(ModalityType.APPLICATION_MODAL)
        minimumSize = Dimension(400, 180)
        maximumSize = Dimension(800, 600)
        add(getContent())
        pack()
        setLocationRelativeTo(null)
    }

    private fun getContent(): JComponent =
        column(
            labeledInputField(
                labelText = "Theme",
                tooltipText = "Choose the appearance theme.",
                inputComponent =
                    themeComboBox
                        .apply {
                            Theme.entries.forEach { addItem(it) }
                            setSelectedItemSafe(ScriptConfig.getInstanceOrThrow().theme)
                        }.onItemChanged { item, _ ->
                            GuiUtils.applyThemeWithUiAnimatedChange(item, themeModeComboBox.getSelectedItemOrThrow())
                        },
            ),
            labeledInputField(
                labelText = "Theme Mode",
                tooltipText =
                    "Select the mode for the theme to use. Some themes may not support Dark mode.",
                inputComponent =
                    themeModeComboBox
                        .apply {
                            ThemeMode.entries.forEach { addItem(it) }
                            setSelectedItemSafe(ScriptConfig.getInstanceOrThrow().themeMode)
                        }.onItemChanged { item, _ ->
                            GuiUtils.applyThemeWithUiAnimatedChange(themeComboBox.getSelectedItemOrThrow(), item)
                        },
            ),
            labeledInputField(
                labelText = "Enable GUI",
                tooltipText =
                    buildHtml {
                        text("Check to enable the graphical user interface (GUI) version of the script.")
                        newLines(2)
                        text("Consider using ")
                        boldText(SharedConstants.DISABLE_GUI_ARG_NAME)
                        text(" as a launch argument to disable the GUI. This ensures smooth execution, ")
                        text("avoiding potential unexpected behavior during configuration file loading.")
                        newLines(2)
                        text("Because this property will be stored in the config file, if an error happened while loading the ")
                        text("config file and your system support GUI,")
                        newLine()
                        if (SharedConstants.GUI_ENABLED_WHEN_AVAILABLE_DEFAULT) {
                            text("will use GUI mode even if you specified to not use it in the config file.")
                        } else {
                            text("will not GUI mode even if you specified to use it in the config file.")
                        }
                        newLines(2)
                        text("The GUI mode will be automatically disabled if the system doesn't support it.")
                    }.buildAsText(),
                inputComponent =
                    overrideIsGuiEnabledCheckBox.apply { isSelected = GuiState.isGuiEnabled },
            ),
            JButton("Continue").onClick { dispose() },
        ) {
            padding(10, 10, 10, 10)
        }

    /**
     * @return The new [ScriptConfig] that use the user preferences
     * */
    fun showDialog(): ScriptConfig {
        isVisible = true
        return ScriptConfig.getInstanceOrThrow().copy(
            theme = themeComboBox.getSelectedItemOrThrow(),
            themeMode = themeModeComboBox.getSelectedItemOrThrow(),
            guiEnabled = getOverrideIsGuiEnabled(),
        )
    }

    private fun getOverrideIsGuiEnabled(): Boolean? {
        if (overrideIsGuiEnabledCheckBox.isSelected != GuiState.isGuiEnabled) {
            return overrideIsGuiEnabledCheckBox.isSelected
        }
        // If the new value is the same as the current value, there will
        // be no need to explicitly define this in the config file
        return null
    }
}
