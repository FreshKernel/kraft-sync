package constants

import config.models.ScriptConfig

object Constants {
    /**
     * Should we by default use GUI for the (error messages, download loading indicator etc...) as default value?
     * */
    const val GUI_ENABLED_WHEN_AVAILABLE_DEFAULT = true

    /**
     * The argument name that will be used in the launch args in order to not use the gui
     * will use `nogui` because it's the same as
     * [Minecraft Java Server](https://minecraft.fandom.com/wiki/Tutorials/Setting_up_a_server)
     * TODO: Might rename it to `--disable-gui`, also add a shorter version and better way to parse it
     * */
    const val DISABLE_GUI_ARG_NAME = "nogui"

    /**
     * Should we tell the launcher to launch the game anyway when error during the sync process?
     * read [ScriptConfig.launchOnError] for more info
     * */
    const val LAUNCH_ON_ERROR_DEFAULT = false

    /**
     * All system preference keys are stored on the system will start with
     * */
    const val SYSTEM_PREFERENCES_PREFIX = "${ProjectInfoConstants.NORMALIZED_NAME}:"

    /**
     * A way to disable or enable certain features without changing too much in the source code.
     * */
    object Features {
        const val TRUST_ADMIN_ENABLED = true
    }
}
