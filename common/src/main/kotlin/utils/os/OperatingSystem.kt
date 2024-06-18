package utils.os

import utils.SystemInfoProvider

/**
 * An enum class represent the supported operating systems for this project.
 * */
enum class OperatingSystem {
    Linux,
    MacOS,
    Windows,
    Unknown,
    ;

    fun isLinux() = this == Linux

    fun isMacOS() = this == MacOS

    fun isWindows() = this == Windows

    fun isUnknown() = this == Unknown

    companion object {
        val current: OperatingSystem by lazy {
            getCurrentByJavaSystemProperty()
        }

        private fun getCurrentByJavaSystemProperty(): OperatingSystem {
            val operatingSystemName = SystemInfoProvider.getOperatingSystemName() ?: return Unknown
            return when {
                listOf("nix", "nux", "aix", "linux").any {
                    operatingSystemName.contains(it, ignoreCase = true)
                } -> Linux

                operatingSystemName == "Mac OS X" || operatingSystemName.contains("mac", ignoreCase = true) -> MacOS
                listOf("win", "windows").any { operatingSystemName.contains(it, ignoreCase = true) } -> Windows
                else -> Unknown
            }
        }
    }
}
