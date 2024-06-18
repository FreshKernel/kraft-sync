package gui.theme

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO: Might add support for loading themes from a file
// TODO: Might add support for changing the accent color
// TODO: Add support for custom fonts

@Serializable
enum class Theme {
    @SerialName("Auto")
    Auto,

    @SerialName("flatlaf")
    FlatLaf,

    /**
     * This only has a light theme
     * for more [details](https://www.formdev.com/flatlaf/themes/)
     * */
    @SerialName("flatIntelliJLaf")
    FlatIntelliJLaf,

    /**
     * This only has a dark theme
     * for more [details](https://www.formdev.com/flatlaf/themes/)
     * */
    @SerialName("flatDarculaLaf")
    FlatDarculaLaf,

    @SerialName("flatMacLaf")
    FlatMacLaf,

    /**
     * Uses the default system theme provided by Java Swing, adapting to the system's native look and feel.
     * Only has light theme
     */
    @SerialName("defaultSystemSwing")
    DefaultSystemSwing,

    /**
     * Uses the default cross-platform theme provided by Java Swing, ensuring consistent appearance across platforms.
     * Only has light theme
     */
    @SerialName("defaultCrossPlatformSwing")
    DefaultCrossPlatformSwing,

    ;

    override fun toString(): String {
        return when (this) {
            Auto -> "Auto"
            FlatLaf -> "FlatLaf"
            FlatIntelliJLaf -> "Flat IntelliJ Laf (Light Mode Only)"
            FlatDarculaLaf -> "Flat Darcula Laf (Dark Mode Only)"
            FlatMacLaf -> "Flat macOS Laf"
            DefaultSystemSwing -> "Default System Swing (Light Mode Only)"
            DefaultCrossPlatformSwing -> "Default Cross-platform Swing (Light Mode Only)"
        }
    }
}
