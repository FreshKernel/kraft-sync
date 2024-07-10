package gui.utils

import utils.Logger
import utils.SystemInfoProvider
import utils.commandLine
import utils.os.LinuxDesktopEnvironment
import utils.os.OperatingSystem
import utils.powerShellCommandLine
import java.nio.file.Paths
import kotlin.io.path.bufferedReader
import kotlin.io.path.name
import kotlin.io.path.pathString

object ThemeDetector {
    private fun LinuxDesktopEnvironment.Companion.isSystemInDarkMode(): Boolean {
        return when (current) {
            LinuxDesktopEnvironment.Gnome -> {
                val colorSchemeOutput =
                    commandLine(
                        "gsettings",
                        "get",
                        "org.gnome.desktop.interface",
                        "color-scheme",
                        reasonOfRunningTheCommand = "to check if the Gnome desktop environment is in dark mode",
                    ).getOrDefault("")
                if (colorSchemeOutput.contains("'prefer-dark'", ignoreCase = true) ||
                    colorSchemeOutput.contains("dark", ignoreCase = true)
                ) {
                    return true
                }
                val gnomeThemeNameOutput =
                    commandLine(
                        "gsettings",
                        "get",
                        "org.gnome.desktop.interface",
                        "gtk-theme",
                        reasonOfRunningTheCommand = "to check if the Gnome desktop environment is in dark mode",
                    ).getOrDefault("")
                if (gnomeThemeNameOutput.contains("dark", ignoreCase = true)) {
                    return true
                }
                false
            }

            LinuxDesktopEnvironment.KdePlasma -> {
                fun getCurrentLookAndFeelPackageName(): Result<String?> {
                    val kdeGlobalsFile =
                        Paths.get(
                            SystemInfoProvider.getUserHomeDirectoryPath(),
                            ".config/kdeglobals",
                        )
                    Logger.info {
                        "\uD83D\uDCC4 Reading the following file to check if the KDE Plasma desktop environment " +
                            "is in dark mode: ${kdeGlobalsFile.pathString}"
                    }
                    return try {
                        val lookAndFeelPackageName =
                            kdeGlobalsFile.bufferedReader().useLines { line ->
                                line.firstOrNull { it.startsWith("LookAndFeelPackage=") }?.substringAfter('=')
                            }
                        Logger.info { "✨ The KDE Plasma look and feel package name: 🎨 $lookAndFeelPackageName" }
                        Result.success(lookAndFeelPackageName)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Logger.error { "❌ Error while reading the file ${kdeGlobalsFile.name}: ${e.message}" }
                        Result.failure(e)
                    }
                }

                val currentLookAndFeelPackageName = getCurrentLookAndFeelPackageName().getOrDefault("") ?: ""
                if (currentLookAndFeelPackageName.contains("dark", ignoreCase = true)) {
                    return true
                }
                // The look and feel package name might not have the dark word in the name like `org.kde.oxygen` and `org.kde.breezedark.desktop`

                // List of known look and feel packages
                val knownDarkLookAndFeelPackageNames =
                    listOf(
                        "org.kde.breezedark.desktop",
                        "org.kde.oxygen",
                        "org.kde.arc-dark",
                        "org.kde.numix-dark",
                        "org.kde.papirus-dwark",
                        "org.kde.suru-dark",
                    )
                return knownDarkLookAndFeelPackageNames.contains(currentLookAndFeelPackageName)
            }

            LinuxDesktopEnvironment.Cinnamon -> {
                val currentCinnamonThemeNameOutput =
                    commandLine(
                        "gsettings",
                        "get",
                        "org.cinnamon.theme",
                        "name",
                        reasonOfRunningTheCommand = "to check if the Cinnamon desktop environment is in dark mode",
                    ).getOrDefault("")

                if (currentCinnamonThemeNameOutput.contains("dark", ignoreCase = true)) {
                    return true
                }
                // The theme name might not have the name dark in it like 'cinnamon' and `Linux-Mint`

                // List of known themes
                val knownDarkThemeNames =
                    listOf(
                        "Mint-Y-Dark",
                        "Mint-Y-Dark-Aqua",
                        "Mint-Y-Dark-Blue",
                        "Mint-Y-Dark-Brown",
                        "Mint-Y-Dark-Grey",
                        "Mint-Y-Dark-Orange",
                        "Mint-Y-Dark-Pink",
                        "Mint-Y-Dark-Purple",
                        "Mint-Y-Dark-Red",
                        "Mint-Y-Dark-Sand",
                        "Cinnamon",
                        "Linux Mint",
                    )

                val isKnownDarkTheme =
                    knownDarkThemeNames.any { currentCinnamonThemeNameOutput.contains(it, ignoreCase = true) }

                if (isKnownDarkTheme) {
                    return true
                }

                false
            }

            LinuxDesktopEnvironment.Unknown -> false
        }
    }

    fun isSystemInDarkModeByCommandLine(): Boolean {
        return when (OperatingSystem.current) {
            OperatingSystem.Linux -> LinuxDesktopEnvironment.isSystemInDarkMode()

            OperatingSystem.MacOS -> {
                // Alternative command `defaults read NSGlobalDomain AppleInterfaceStyle`
                val appleInterfaceStyleOutput =
                    commandLine(
                        "defaults",
                        "read",
                        "-g",
                        "AppleInterfaceStyle",
                        reasonOfRunningTheCommand = "to check if the macOS system is in dark mode",
                    ).getOrDefault("")
                if (appleInterfaceStyleOutput.contains("dark", ignoreCase = true)) {
                    return true
                }
                false
            }

            OperatingSystem.Windows -> {
                val appsUseLightThemeOutput =
                    powerShellCommandLine(
                        "Get-ItemProperty",
                        "-Path",
                        "\"HKCU:\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize\"",
                        "-Name",
                        "\"AppsUseLightTheme\"",
                        "|",
                        "Select-Object",
                        "-ExpandProperty",
                        "AppsUseLightTheme",
                        reasonOfRunningTheCommand = "to check if the Windows system is in dark mode",
                    ).getOrDefault("1")
                // Using trim() is necessary to remove the white spaces
                if (appsUseLightThemeOutput.trim().equals("0", ignoreCase = true)) {
                    return true
                }
                false
            }

            OperatingSystem.Unknown -> false
        }
    }
}
