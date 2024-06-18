package gui.tabs

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.extras.FlatAnimatedLafChange
import gui.Tab
import gui.components.labeledInputPanel
import gui.theme.Theme
import gui.theme.ThemeMode
import gui.utils.GuiUtils
import gui.utils.column
import gui.utils.getSelectedItemOrThrow
import gui.utils.onItemSelected
import javax.swing.JComboBox
import javax.swing.JComponent

class SettingsTab : Tab() {
    private lateinit var themeComboBox: JComboBox<Theme>
    private lateinit var themeModeComboBox: JComboBox<ThemeMode>

    init {
        setupTabContent()
    }

    override fun getTabContent(): JComponent {
        return column(
            labeledInputPanel(
                labelText = "Theme",
                tooltipText = "The theme to be used",
                inputComponent =
                    JComboBox<Theme>().apply {
                        Theme.entries.forEach { addItem(it) }
                        selectedItem = Theme.Auto
                    }.onItemSelected { item, _ ->
                        GuiUtils.applyThemeWithUiAnimatedChange(
                            item,
                            themeModeComboBox.getSelectedItemOrThrow(),
                        )
                    }.also { themeComboBox = it },
            ),
            labeledInputPanel(
                labelText = "Theme Mode",
                tooltipText = "If you want to use the system theme mode or choose if you want dark or light",
                inputComponent =
                    JComboBox<ThemeMode>().apply {
                        ThemeMode.entries.forEach { addItem(it) }
                        selectedItem = ThemeMode.System
                    }.onItemSelected { item, _ ->
                        FlatAnimatedLafChange.showSnapshot()
                        GuiUtils.applyThemeIfNeeded(themeComboBox.getSelectedItemOrThrow(), item)

                        FlatLaf.updateUI()
                        FlatAnimatedLafChange.hideSnapshotWithAnimation()
                    }.also { themeModeComboBox = it },
            ),
        )
    }
}
