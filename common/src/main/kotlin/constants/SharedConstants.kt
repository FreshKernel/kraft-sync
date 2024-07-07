package constants

/**
 * General shared constants
 * */
object SharedConstants {
    /**
     * Enable GUI mode when available (error messages, download loading indicator, etc...) for the sync script.
     * */
    const val GUI_ENABLED_WHEN_AVAILABLE_DEFAULT = true

    /**
     * The argument name that will be used in the launch args in order to not use the GUI
     * will use `nogui` because it's the same as
     * [Minecraft Java Server](https://minecraft.fandom.com/wiki/Tutorials/Setting_up_a_server)
     * TODO: Might rename it to `--disable-gui`, also add a shorter version and better way to parse it
     * */
    const val DISABLE_GUI_ARG_NAME = "nogui"
}
