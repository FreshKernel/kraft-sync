package config.models

import constants.Constants
import constants.SyncScriptInstanceFiles
import gui.theme.Theme
import gui.theme.ThemeMode
import kotlinx.serialization.Serializable
import syncInfo.models.Environment
import syncInfo.models.SyncInfo

/**
 * A data class that represent the json for the [SyncScriptInstanceFiles.SyncScriptData.ScriptConfig]
 * where the user can pass some data that is needed to launch the script
 * */
@Serializable
data class ScriptConfig(
    /**
     * The public download url for the [SyncInfo] json file
     * it's needed so the script can fetch the [SyncInfo] from it as it's containing the server data
     * to get the latest info about the mods and other resources in addition to another server customization
     * */
    val syncInfoUrl: String,
    /**
     * Override the value set using launch arguments, by default,
     * the script will use [Constants.GUI_ENABLED_WHEN_AVAILABLE_DEFAULT] if GUI mode supported.
     *
     * If not, then will automatically disable the GUI mode.
     *
     * If you want to explicit disable the GUI mode, use [Constants.DISABLE_GUI_ARG_NAME] instead of overriding it
     * in here because if an error occurred while loading [ScriptConfig] from the file,
     * we will fall back to use the one from application arguments if set,
     * if not then will use [Constants.GUI_ENABLED_WHEN_AVAILABLE_DEFAULT]
     * if GUI is supported, otherwise will disable the GUI.
     *
     * This will **only take effect** after loading the [ScriptConfig].
     *
     * If you are using [Environment.Server] and the server doesn't support graphical interface
     * it should be automatically disabled.
     *
     * */
    val isGuiEnabledOverride: Boolean? = null,
    /**
     * The theme mode for the GUI, this only take effect if the GUI mode is enabled.
     * */
    val themeMode: ThemeMode = ThemeMode.System,
    /**
     * The ued theme for the GUI, each theme have dark and light mode which can be edited by [themeMode]
     * this only take effect if the GUI mode is enabled.
     * */
    val theme: Theme = Theme.Auto,
    /**
     * When the script doesn't successfully finish for some reason or the user closed the script
     * should we exit with error, so the launcher you are using will stop launching the game to indicate there is an error
     * or continue and launch it which can result in unexpected behavior or un-synced content or not in a correct way
     * can be fixed by launching the script again
     * */
    val launchOnError: Boolean = Constants.LAUNCH_ON_ERROR_DEFAULT,
    /**
     * The script need to know it's syncing the content to a server or client,
     * for example, it will only install the server side mods if the environment is [Environment.Server]
     *
     * The accepted values are `client` or `server`
     *
     * If you are using [Environment.Server] and the server doesn't support graphical interface
     * then consider passing false to [isGuiEnabledOverride]
     * */
    val environment: Environment = Environment.Client,
    /**
     * Currently, the auto-update feature is **highly experimental**, and might be removed, or changed at any time.
     * And for now, this feature has the following known issues:
     * 1. It will always update even if the next version has **breaking changes**
     * that can't be automatically migrated.
     * 2. It will always update even if the next version is not a stable release;
     * we haven't implemented an update channel for now (e.g., stable, beta, alpha, development, etc...).
     * 3. Once the update is finished, the application will close with
     * exit code 1 which will indicate an error by the launcher.
     * The user will have to launch once again to run the updated JAR.
     * 4. Currently, it lacks the ability to check for updates, such as on a weekly basis.
     * 5. Lacks the option to ask if the user wants to update or skip.
     * 6. At the moment we have minimized JAR and the fat JAR,
     * the update process will always update to the minimized JAR.
     * */
    val autoUpdateEnabled: Boolean = false,
) {
    companion object {
        var instance: ScriptConfig? = null

        fun getInstanceOrThrow() =
            instance
                ?: throw IllegalStateException(
                    "The ${ScriptConfig::class.simpleName} is not initialized yet, should use the nullable access instead.",
                )
    }
}
