package gui

import config.models.ScriptConfig
import constants.Constants
import passedArgs
import java.awt.GraphicsEnvironment

object GuiState {
    /**
     * By default, will load it from the program arguments by [Constants.DISABLE_GUI_ARG_NAME]
     * the value can be overridden using [ScriptConfig.guiEnabled] when the [ScriptConfig] is initialized
     * */
    var isGuiEnabled: Boolean = Constants.GUI_ENABLED_WHEN_AVAILABLE_DEFAULT
        private set

    /**
     * A function to update the value of the [isGuiEnabled] based on [ScriptConfig.guiEnabled] and the [passedArgs]
     * if any of those changes, this function should be called
     *
     * */
    fun updateIsGuiEnabled() {
        // Check if the user overrides the default value in the config file
        ScriptConfig.instance?.guiEnabled?.let {
            isGuiEnabled = it
            return
        }
        // Check if the user overrides the default value in the launch arguments
        val isDisableGuiArgumentPassed = passedArgs.isNotEmpty() && passedArgs[0] == Constants.DISABLE_GUI_ARG_NAME
        if (isDisableGuiArgumentPassed) {
            isGuiEnabled = false
            return
        }

        // If the user didn't specify a value and GUI is not supported, make sure to disable the GUI mode as a default
        if (GraphicsEnvironment.isHeadless()) {
            isGuiEnabled = false
            return
        }
    }
}
