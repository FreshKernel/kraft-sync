import kotlinx.serialization.Serializable
import syncInfo.models.Environment
import syncInfo.models.SyncInfo

/**
 * A data class that represent the json for the [Constants.MinecraftInstanceFiles.MinecraftSyncData.ScriptConfig]
 * where the user can pass some data that is needed in order to launch the script
 * */
@Serializable
data class ScriptConfig(
    /**
     * The public download url for the [SyncInfo] json file
     * it's needed so the script can fetch the [SyncInfo] from it as it's contains the server data
     * to get the latest info about the mods and other resources in addition to other server customization
     * */
    val syncInfoUrl: String,
    /**
     * Should we use GUI (for the errors, download indicator etc...)? by default will get the value from [Constants.DISABLE_GUI_ARG_NAME]
     * in the launching options as we might need this before loading the script config but the [useGui] property
     * can be used to override it but will **only take effect** after loading the [ScriptConfig]
     *
     * If you don't want to open window other than the launcher then please pass false to [useGui]
     *
     * If you are using [Environment.Server] and the server doesn't support graphical interface
     * then please consider pass false to [useGui]
     *
     * unless you need this, it's better to always it in the java launch arguments, by default gui will be used
     * pass [Constants.DISABLE_GUI_ARG_NAME] and it should disable the GUI
     *
     * */
    val useGui: Boolean? = null,
    /**
     * When the script doesn't successfully finish for some reason or the user closed the script
     * should we exit with error so the launcher you are using will stop launching the game to indicate there is error
     * or continue and launch it which can result in unexpected behavior or un-synced content or not in a correct way
     * can be fixed by launching the script again
     * */
    val launchOnError: Boolean = Constants.LAUNCH_ON_ERROR,
    /**
     * The script need to know it's syncing the content to a server or client
     * for example it will only install the server side mods if the environment is [Environment.Server]
     *
     * The accepted values are `client` or `server`
     *
     * If you are using [Environment.Server] and the server doesn't support graphical interface
     * then please consider pass false to [useGui]
     * */
    val environment: Environment = Environment.Client,
)
