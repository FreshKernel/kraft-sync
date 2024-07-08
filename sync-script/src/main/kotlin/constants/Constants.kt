package constants

import config.models.ScriptConfig

object Constants {
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
