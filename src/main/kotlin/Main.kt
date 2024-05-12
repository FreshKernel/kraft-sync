import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import syncInfo.data.RemoteSyncInfoDataSource
import syncInfo.data.SyncInfoDataSource
import syncInfo.models.SyncInfo
import syncService.SyncModsService
import syncService.SyncService
import util.gui.CreateSyncInfoDialog
import util.showErrorMessage
import java.time.Duration
import kotlin.system.exitProcess

val okHttpClient =
    OkHttpClient.Builder()
        .connectTimeout(Duration.ofMinutes(120))
        .callTimeout(Duration.ofMinutes(120))
        .readTimeout(Duration.ofMinutes(120))
        .build()

/**
 * By default, will load it from the program arguments by [Constants.DISABLE_GUI_ARG_NAME]
 * but the value can be override using [ScriptConfig.useGui] when the [ScriptConfig] is initialized
 * */
var useGui: Boolean = Constants.USE_GUI_DEFAULT

var scriptConfig: ScriptConfig? = null

fun getScriptConfigOrThrow() =
    scriptConfig
        ?: throw IllegalStateException(
            "The ${ScriptConfig::class.simpleName} is not initialized yet, should use the nullable access instead.",
        )

lateinit var syncInfo: SyncInfo

// TODO: The logging in the script need to be improved
// TODO: The emojis are completely broken
// TODO: Add localizations for the GUI

suspend fun main(args: Array<String>) {
    // 0. Detriment if the user want to use GUI or not

    useGui = !(args.isNotEmpty() && args[0] == Constants.DISABLE_GUI_ARG_NAME)

    // 1. Loading the script config file from json file

    val scriptConfigFile = Constants.MinecraftInstanceFiles.MinecraftSyncData.ScriptConfig.file
    if (!scriptConfigFile.exists()) {
        if (useGui) {
            println(
                "Configuration Missing! ⚠\uFE0F. Since you're in GUI mode, we'll launch a quick " +
                    "dialog to gather the necessary information",
            )
            CreateSyncInfoDialog().showDialog()
        } else {
            showErrorMessage(
                title = "Configuration Missing! ⚠\uFE0F",
                message =
                    """
                    The script configuration file, `${scriptConfigFile.path}`, couldn't be found. 

                    To get started, please create this file in the same directory where you're running the script or 
                    in the working directory. 

                    This file will contain important settings for the script to function properly. ⚙️
                    """.trimIndent(),
                useGuiOverride = false,
            )
        }
    }
    runCatching {
        Json.decodeFromString<ScriptConfig>(scriptConfigFile.readText()).also { scriptConfig = it }
    }.onFailure {
        showErrorMessage(
            title = "Configuration Error ⚠\uFE0F",
            message =
                """
                An error occurred while parsing your script configuration file (${scriptConfigFile.path}).

                Please ensure it's valid JSON format.
                """.trimIndent(),
        )
    }

    // Allow to override it and use the one from the script config instead of the launching args
    getScriptConfigOrThrow().useGui?.let {
        useGui = it
    }

    println("Script Configuration: $scriptConfig")
    if (useGui && getScriptConfigOrThrow().environment.isServer()) {
        println(
            """
      You're using the GUI in a server environment. 

      If your server doesn't have a graphical interface, consider disabling the GUI in the launch args `${Constants.DISABLE_GUI_ARG_NAME}` property ⚙️ 
      in the configuration file `${Constants.MinecraftInstanceFiles.MinecraftSyncData.ScriptConfig.file.name}` .
    """,
        )
    }

    // 2. Fetch the sync info data from the remote url

    val syncInfoDataSource: SyncInfoDataSource =
        RemoteSyncInfoDataSource(
            client = okHttpClient,
        )
    syncInfo =
        syncInfoDataSource.fetchSyncInfo(
            url = getScriptConfigOrThrow().syncInfoUrl,
        ).getOrElse {
            showErrorMessage(
                title = "Sync Info Unavailable \uD83D\uDD04",
                message = "We had little problem while trying to fetch sync info from the server: ${it.message}",
            )
            return
        }

    println("Sync Info: $syncInfo")

    // 3. Use sync services to sync the content

    val syncServices: List<SyncService> =
        listOf(
            SyncModsService(),
        )

    syncServices.forEach {
        it.syncContents(syncInfo)
    }

    // 4. Finally, finish the script

    // TODO: Workaround for fixing long/infinite code execution for okhttp library
    okHttpClient.dispatcher.executorService.shutdown()

    println("\uD83C\uDF89 The script has successfully completed! \uD83D\uDE80")
    exitProcess(0)
}
