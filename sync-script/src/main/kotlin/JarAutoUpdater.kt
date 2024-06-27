import constants.ProjectInfoConstants
import constants.SyncScriptDotMinecraftFiles
import generated.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import utils.FileDownloader
import utils.HttpService
import utils.SystemInfoProvider
import utils.deleteExistingOrTerminate
import utils.executeAsync
import utils.executeBatchScriptInSeparateWindow
import utils.getBodyOrThrow
import utils.getRunningJarFilePath
import utils.moveToOrTerminate
import utils.os.OperatingSystem
import utils.terminateWithOrWithoutError
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

object JarAutoUpdater {
    private suspend fun downloadLatestJarFile(): Result<Path> =
        try {
            // TODO: Avoid converting to Path and use the Path instead
            val newJarFile =
                SyncScriptDotMinecraftFiles.SyncScriptData.Temp.file
                    .resolve("${ProjectInfoConstants.NORMALIZED_NAME}-new.jar")
                    .toPath()
            if (newJarFile.exists()) {
                newJarFile.deleteExistingOrTerminate(
                    fileEntityType = "JAR",
                    reasonOfDelete = "the script is downloading the new update",
                )
            }
            FileDownloader(
                downloadUrl = ProjectInfoConstants.LATEST_SYNC_SCRIPT_JAR_FILE_URL,
                targetFilePath = newJarFile,
                progressListener = { _, _, _ -> },
            ).downloadFile()
            Result.success(newJarFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    private suspend fun getLatestProjectVersion(): Result<String?> =
        try {
            val url = ProjectInfoConstants.LIBS_VERSIONS_TOML_FILE_URL
            println("\uD83D\uDCE5 Sending GET request to: $url")
            val request =
                Request
                    .Builder()
                    .url(url)
                    .get()
                    .build()
            val response = HttpService.client.newCall(request).executeAsync()
            val responseBody: String = response.getBodyOrThrow().string()

            val projectVersionRegex = Regex("""project\s*=\s*"(.+?)"""")

            val projectVersion =
                projectVersionRegex
                    .find(responseBody)
                    ?.groups
                    ?.get(1)
                    ?.value
            Result.success(projectVersion)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    suspend fun updateIfAvailable() {
        val currentRunningJarFilePath =
            getRunningJarFilePath()
                .getOrElse {
                    println("⚠\uFE0F Auto update feature is only supported when running using JAR.")
                    return
                }
        val latestProjectVersion =
            getLatestProjectVersion().getOrElse {
                println("❌ We couldn't get the latest project version: ${it.message}")
                return
            }
        if (latestProjectVersion == null) {
            println(
                "⚠\uFE0F It seems that the project version is missing, it could have been moved somewhere else. " +
                    "Consider updating manually.",
            )
            return
        }
        if (latestProjectVersion == BuildConfig.PROJECT_VERSION) {
            println("✨ You're using the latest version of the project.")
            return
        }
        val newJarFile =
            downloadLatestJarFile().getOrElse {
                println("❌ An error occurred while downloading the latest version: ${it.message}")
                return
            }
        println("ℹ\uFE0F The new update has been downloaded, will close the application.")
        updateApplication(
            currentRunningJarFilePath = currentRunningJarFilePath,
            newJarFilePath = newJarFile,
        )
    }

    private suspend fun updateApplication(
        currentRunningJarFilePath: Path,
        newJarFilePath: Path,
    ) {
        when (OperatingSystem.current) {
            OperatingSystem.Linux, OperatingSystem.MacOS -> {
                Runtime.getRuntime().addShutdownHook(
                    Thread {
                        currentRunningJarFilePath.deleteExistingOrTerminate(
                            fileEntityType = "JAR",
                            reasonOfDelete = "the script is deleting is deleting the current JAR file to use the updated one",
                        )
                        newJarFilePath.moveToOrTerminate(
                            target = currentRunningJarFilePath,
                            overwrite = true,
                            fileEntityType = "JAR",
                        )
                    },
                )
            }

            OperatingSystem.Windows -> {
                // On Windows, we can't rename, delete or modify the current running JAR file due to file locking
                // TODO: Migrate to Java NIO later
                val updateBatScriptFile =
                    SyncScriptDotMinecraftFiles.SyncScriptData.Temp.file
                        .resolve("update.bat")
                withContext(Dispatchers.IO) {
                    updateBatScriptFile.parentFile.mkdirs()
                    updateBatScriptFile.createNewFile()
                }
                updateBatScriptFile.writeText(
                    """
                    @echo off
                    echo Waiting for 2 seconds to ensure application closure...
                    timeout /t 2 > nul
                    del "${currentRunningJarFilePath.absolutePathString()}"
                    move "${newJarFilePath.absolutePathString()}" "${currentRunningJarFilePath.absolutePathString()}"
                    exit
                    """.trimIndent(),
                )
                executeBatchScriptInSeparateWindow(
                    batScriptFile = updateBatScriptFile,
                )
            }

            OperatingSystem.Unknown -> {
                println("⚠\uFE0F Auto update feature is not supported on ${SystemInfoProvider.getOperatingSystemName()}.")
            }
        }
        // Will require the user to launch once again after the update.
        terminateWithOrWithoutError()
    }
}
