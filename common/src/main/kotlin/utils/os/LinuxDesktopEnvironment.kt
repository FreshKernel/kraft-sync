package utils.os

import utils.SystemInfoProvider

enum class LinuxDesktopEnvironment {
    Gnome,
    KdePlasma,
    Unknown,
    Cinnamon,
    ;

    fun isGnome() = this == Gnome

    fun isKdePlasma() = this == KdePlasma

    fun isCinnamon() = this == Cinnamon

    fun isUnknown() = this == Unknown

    companion object {
        /**
         * @throws UnsupportedOperationException if the current operating system is not Linux
         * */
        val current: LinuxDesktopEnvironment by lazy {
            if (OperatingSystem.current != OperatingSystem.Linux) {
                throw UnsupportedOperationException(
                    "🚫 Unable to determine Linux desktop environment when not running on a Linux \uD83D\uDC27 system.",
                )
            }
            val desktopEnvironmentName = SystemInfoProvider.getCurrentLinuxDesktopEnvironmentName()
            when {
                desktopEnvironmentName.contains("gnome", ignoreCase = true) -> Gnome
                desktopEnvironmentName.contains("kde", ignoreCase = true) ||
                    SystemInfoProvider.getKdeFullSession()?.isNotEmpty() == true -> KdePlasma

                desktopEnvironmentName.contains("cinnamon", ignoreCase = true) -> Cinnamon

                else -> Unknown
            }
        }
    }
}
