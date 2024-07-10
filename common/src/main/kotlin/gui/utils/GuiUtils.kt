package gui.utils

import com.formdev.flatlaf.FlatDarculaLaf
import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatIntelliJLaf
import com.formdev.flatlaf.FlatLaf
import com.formdev.flatlaf.FlatLightLaf
import com.formdev.flatlaf.extras.FlatAnimatedLafChange
import com.formdev.flatlaf.themes.FlatMacDarkLaf
import com.formdev.flatlaf.themes.FlatMacLightLaf
import constants.SharedAssetConstants
import gui.theme.Theme
import gui.theme.ThemeMode
import utils.Logger
import utils.getResourceAsURLOrThrow
import utils.os.OperatingSystem
import java.awt.Component
import java.awt.Taskbar
import java.time.LocalDateTime
import javax.swing.ImageIcon
import javax.swing.ToolTipManager
import javax.swing.UIManager
import javax.swing.UnsupportedLookAndFeelException
import kotlin.random.Random

object GuiUtils {
    fun showErrorMessage(
        title: String,
        message: String,
        parentComponent: Component?,
    ) {
        SwingDialogManager.showMessageDialog(
            message = message,
            title = title,
            parentComponent = parentComponent,
            messageType = SwingDialogManager.MessageType.Error,
        )
    }

    fun setupSwingGui() {
        if (Taskbar.isTaskbarSupported()) {
            if (Taskbar.getTaskbar().isSupported(Taskbar.Feature.ICON_IMAGE)) {
                Taskbar.getTaskbar().iconImage = getIconAsImageIcon().image
            }
        }

        ToolTipManager.sharedInstance().apply {
            dismissDelay = Integer.MAX_VALUE
            initialDelay = 0
        }

        System.setProperty("awt.useSystemAAFontSettings", "on")
        System.setProperty("swing.aatext", "true")
    }

    /**
     * The icon for the script
     * */
    fun getIconAsImageIcon(): ImageIcon {
        val icon = ImageIcon(getResourceAsURLOrThrow(SharedAssetConstants.PROJECT_ICON_FILE_NAME))
        return icon
    }

    /**
     * Update the look and feel / the theme and the theme mode (dark or light)
     * Will ignore the function call if the current theme and theme mode are the same as the new [theme] and [themeMode]
     * */
    fun applyThemeIfNeeded(
        theme: Theme?,
        themeMode: ThemeMode?,
    ) {
        fun executeThemeSwitchIfNeeded(
            shouldSwitchTheme: Boolean,
            switchTheme: () -> Unit,
        ) = if (shouldSwitchTheme) switchTheme() else Unit

        /**
         * Get the current Swing look and feel name
         * */
        fun getCurrentSwingLookAndFeelName() = UIManager.getLookAndFeel().name

        /**
         * Get the java class name for the current look and feel, try to use getCurrentSwingLookAndFeelName()
         * if possible
         * */
        fun getCurrentSwingLookAndFeelClassName() = UIManager.getLookAndFeel()?.javaClass?.name

        try {
            when (theme) {
                null, Theme.Auto -> {
                    when (OperatingSystem.current) {
                        OperatingSystem.MacOS -> {
                            if (isDarkMode(themeMode)) {
                                executeThemeSwitchIfNeeded(
                                    FlatMacDarkLaf.NAME != getCurrentSwingLookAndFeelName(),
                                ) { FlatMacDarkLaf.setup() }
                                return
                            }
                            executeThemeSwitchIfNeeded(FlatMacLightLaf.NAME != getCurrentSwingLookAndFeelName()) { FlatMacLightLaf.setup() }
                        }

                        else -> {
                            if (isDarkMode(themeMode)) {
                                executeThemeSwitchIfNeeded(FlatDarkLaf.NAME != getCurrentSwingLookAndFeelName()) { FlatDarkLaf.setup() }
                                return
                            }
                            executeThemeSwitchIfNeeded(FlatLightLaf.NAME != getCurrentSwingLookAndFeelName()) { FlatLightLaf.setup() }
                        }
                    }
                }

                Theme.DefaultSystemSwing -> {
                    executeThemeSwitchIfNeeded(getCurrentSwingLookAndFeelClassName() != UIManager.getSystemLookAndFeelClassName()) {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
                    }
                }

                Theme.DefaultCrossPlatformSwing -> {
                    executeThemeSwitchIfNeeded(getCurrentSwingLookAndFeelClassName() != UIManager.getCrossPlatformLookAndFeelClassName()) {
                        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName())
                    }
                }

                Theme.FlatLaf -> {
                    if (isDarkMode(themeMode)) {
                        executeThemeSwitchIfNeeded(FlatDarkLaf.NAME != getCurrentSwingLookAndFeelName()) { FlatDarkLaf.setup() }
                        return
                    }
                    executeThemeSwitchIfNeeded(FlatLightLaf.NAME != getCurrentSwingLookAndFeelName()) { FlatLightLaf.setup() }
                }

                Theme.FlatIntelliJLaf -> {
                    executeThemeSwitchIfNeeded(FlatIntelliJLaf.NAME != getCurrentSwingLookAndFeelName()) {
                        FlatIntelliJLaf.setup()
                    }
                }

                Theme.FlatDarculaLaf -> {
                    executeThemeSwitchIfNeeded(FlatDarculaLaf.NAME != getCurrentSwingLookAndFeelName()) {
                        FlatDarculaLaf.setup()
                    }
                }

                Theme.FlatMacLaf -> {
                    if (isDarkMode(themeMode)) {
                        executeThemeSwitchIfNeeded(FlatMacDarkLaf.NAME != getCurrentSwingLookAndFeelName()) { FlatMacDarkLaf.setup() }
                        return
                    }
                    executeThemeSwitchIfNeeded(FlatMacLightLaf.NAME != getCurrentSwingLookAndFeelName()) { FlatMacLightLaf.setup() }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()

            if (e is UnsupportedLookAndFeelException) {
                Logger.error {
                    "⚠️ Couldn't switch to the selected theme \uD83C\uDFA8. It looks like this theme is not supported on your system."
                }
                return
            }
            Logger.error {
                "❌ Failed to switch to the selected theme \uD83C\uDFA8: ${e.message}"
            }
        }
    }

    fun applyThemeWithUiAnimatedChange(
        theme: Theme?,
        themeMode: ThemeMode?,
        isAnimatedChange: Boolean = true,
    ) {
        if (isAnimatedChange) {
            FlatAnimatedLafChange.showSnapshot()
        }
        applyThemeIfNeeded(
            theme,
            themeMode,
        )
        FlatLaf.updateUI()
        if (isAnimatedChange) {
            FlatAnimatedLafChange.hideSnapshotWithAnimation()
        }
    }

    /**
     * Is the GUI configured to be in dark mode, this can be independent from the [isSystemInDarkMode]
     * as it will be returned based on [ThemeMode]
     *
     * @param themeMode Needed to know if the user wants to use the system appearance or the application theme mode
     * @return If the application should use dark or light mode depending on the selected [themeMode]
     * */
    private fun isDarkMode(themeMode: ThemeMode?): Boolean =
        when (themeMode) {
            null, ThemeMode.System -> isSystemInDarkMode
            ThemeMode.Dark -> true
            ThemeMode.Light -> false
            ThemeMode.Random -> Random.nextBoolean()
            ThemeMode.Auto -> {
                val currentHour = LocalDateTime.now().hour
                if (currentHour in 6..18) {
                    // Daytime (6 AM to 6 PM)
                    false
                } else {
                    // Nighttime (6 PM to 6 AM)
                    true
                }
            }
        }

    /**
     * if the system in dark mode or not.
     * uses the command line to get the result and will run only once
     * */
    val isSystemInDarkMode: Boolean by lazy {
        ThemeDetector.isSystemInDarkModeByCommandLine()
    }
}
