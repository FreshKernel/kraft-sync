package gui.tabs

import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.extras.FlatAnimatedLafChange
import gui.Tab
import gui.components.labeledInputField
import gui.theme.Theme
import gui.theme.ThemeMode
import gui.utils.GuiUtils
import gui.utils.column
import gui.utils.getSelectedItemOrThrow
import gui.utils.onItemChanged
import javax.swing.JComboBox
import javax.swing.JComponent

class SettingsTab : Tab() {
    private val themeComboBox: JComboBox<Theme> = JComboBox()
    private val themeModeComboBox: JComboBox<ThemeMode> = JComboBox()

    init {
        setupTabContent()
    }

    override fun getTabContent(): JComponent =
        column(
            labeledInputField(
                labelText = "Theme",
                tooltipText = "The theme to be used",
                inputComponent =
                    themeComboBox
                        .apply {
                            Theme.entries.forEach { addItem(it) }
                            selectedItem = Theme.Auto
                        }.onItemChanged { item, _ ->
                            GuiUtils.applyThemeWithUiAnimatedChange(
                                item,
                                themeModeComboBox.getSelectedItemOrThrow(),
                            )
                        },
            ),
            labeledInputField(
                labelText = "Theme Mode",
                tooltipText = "If you want to use the system theme mode or choose if you want dark or light",
                inputComponent =
                    themeModeComboBox
                        .apply {
                            ThemeMode.entries.forEach { addItem(it) }
                            selectedItem = ThemeMode.System
                        }.onItemChanged { item, _ ->
                            FlatAnimatedLafChange.showSnapshot()
                            GuiUtils.applyThemeIfNeeded(themeComboBox.getSelectedItemOrThrow(), item)

                            FlatLaf.updateUI()
                            FlatAnimatedLafChange.hideSnapshotWithAnimation()
                        },
            ),
        )
}
