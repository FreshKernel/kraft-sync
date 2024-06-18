package gui.theme

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The theme mode for the GUI
 * */
@Serializable
enum class ThemeMode {
    @SerialName("system")
    System,

    @SerialName("dark")
    Dark,

    @SerialName("light")
    Light,

    @SerialName("random")
    Random,

    /**
     * Determined automatically based on system date/time
     * */
    @SerialName("auto")
    Auto,

    ;

    fun isSystem(): Boolean = this == System

    fun isDark(): Boolean = this == Dark

    fun isLight(): Boolean = this == Light

    fun isRandom(): Boolean = this == Random

    fun isAuto(): Boolean = this == Auto

    override fun toString(): String {
        return when (this) {
            System -> "System"
            Dark -> "Dark"
            Light -> "Light"
            Random -> "Random"
            Auto -> "Auto"
        }
    }
}
