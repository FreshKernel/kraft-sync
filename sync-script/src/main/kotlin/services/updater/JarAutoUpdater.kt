package services.updater

import constants.ProjectInfoConstants
import constants.SyncScriptDotMinecraftFiles
import generated.BuildConfig
import gui.GuiState
import gui.dialogs.LoadingIndicatorDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import services.HttpClient
import services.HttpResponse
import utils.FileDownloader
import utils.Logger
import utils.SystemInfoProvider
import utils.buildHtml
import utils.commandLineNonBlocking
import utils.convertBytesToReadableMegabytesAsString
import utils.createFileWithParentDirectoriesOrTerminate
import utils.deleteExistingOrTerminate
import utils.executeBatchScriptInSeparateWindow
import utils.getRunningJarFilePath
import utils.moveToOrTerminate
import utils.os.LinuxDesktopEnvironment
import utils.os.OperatingSystem
import utils.terminateWithOrWithoutError
import utils.version.SemanticVersion
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.writeText

object JarAutoUpdater {
    private suspend fun downloadLatestJarFile(): Result<Path> =
        try {
            val newJarFile =
                SyncScriptDotMinecraftFiles.SyncScriptData.Temp.path
                    .resolve("${ProjectInfoConstants.NORMALIZED_NAME}-new.jar")
            if (newJarFile.exists()) {
                newJarFile.deleteExistingOrTerminate(
                    fileEntityType = "JAR",
                    reasonOfDelete = "the script is downloading the new update",
                )
            }
            val latestJarFileDownloadUrl = ProjectInfoConstants.LATEST_SYNC_SCRIPT_JAR_FILE_URL
            Logger.info { "\uD83D\uDD3D Downloading the new JAR file from: $latestJarFileDownloadUrl" }

            LoadingIndicatorDialog.instance?.updateComponentProperties(
                title = "Updating...",
                infoText =
                    "Sending HTTP request...",
                progress = 0,
                detailsText =
                    "Initiating network request for the update.",
            )
            FileDownloader(
                downloadUrl = ProjectInfoConstants.LATEST_SYNC_SCRIPT_JAR_FILE_URL,
                targetFilePath = newJarFile,
                progressListener = { downloadedBytes, downloadedProgress, bytesToDownload ->
                    LoadingIndicatorDialog.instance?.updateComponentProperties(
                        title = "Updating...",
                        infoText =
                            buildHtml {
                                text("Downloading ")
                                boldText(ProjectInfoConstants.DISPLAY_NAME)
                                text(" Update")
                            }.buildBodyAsText(),
                        progress = downloadedProgress.toInt(),
                        // TODO: This is duplicated twice
                        detailsText =
                            "${downloadedBytes.convertBytesToReadableMegabytesAsString()} MB /" +
                                " ${bytesToDownload.convertBytesToReadableMegabytesAsString()} MB",
                    )
                },
            ).downloadFile(fileEntityType = "JAR")
            Result.success(newJarFile)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    private suspend fun getLatestProjectVersion(): Result<String?> {
        val url = ProjectInfoConstants.LIBS_VERSIONS_TOML_FILE_URL

        Logger.info { "\uD83D\uDCE5 Sending GET request to: $url" }

        return when (val response = HttpClient.get(url = url)) {
            is HttpResponse.Success -> {
                val projectVersionRegex = Regex("""project\s*=\s*"(.+?)"""")

                val projectVersion =
                    projectVersionRegex
                        .find(response.body)
                        ?.groups
                        ?.get(1)
                        ?.value
                Result.success(projectVersion)
            }

            is HttpResponse.HttpFailure -> Result.failure(response.exception())
            is HttpResponse.UnknownError -> Result.failure(response.exception)
        }
    }

    private suspend fun shouldUpdate(): Boolean {
        LoadingIndicatorDialog.instance?.updateComponentProperties(
            title = "Checking for Update...",
            infoText = "Checking for a new update...",
            progress = 0,
            detailsText = "Determining if a new version is available.",
        )
        val latestProjectVersionString =
            getLatestProjectVersion().getOrElse {
                Logger.error { "❌ We couldn't get the latest project version: ${it.message}" }
                return false
            }
        if (latestProjectVersionString == null) {
            Logger.error {
                "⚠\uFE0F It seems that the project version is missing, it could have been moved somewhere else. " +
                    "Consider updating manually."
            }
            return false
        }

        val currentVersionString = BuildConfig.PROJECT_VERSION

        val latestProjectSemanticVersion: SemanticVersion =
            SemanticVersion.parse(latestProjectVersionString).getOrElse {
                Logger.error { "❌ Failed to parse the latest project version to SemanticVersion: ${it.message}" }
                return false
            }
        val currentSemanticVersion: SemanticVersion =
            SemanticVersion.parse(currentVersionString).getOrElse {
                Logger.error { "❌ Failed to parse the current application version to SemanticVersion: ${it.message}" }
                return false
            }

        return when {
            currentSemanticVersion == latestProjectSemanticVersion -> {
                Logger.info { "✨ You're using the latest version of the project." }
                false
            }

            currentSemanticVersion > latestProjectSemanticVersion -> {
                Logger.info { "✨ You're using a version that's newer than the latest." }
                false
            }

            else -> true
        }
    }

    suspend fun updateIfAvailable() {
        val currentRunningJarFilePath =
            getRunningJarFilePath()
                .getOrElse {
                    Logger.warning { "⚠\uFE0F Auto update feature is only supported when running using JAR." }
                    return
                }

        LoadingIndicatorDialog.instance?.isVisible = true

        val shouldUpdate = shouldUpdate()
        if (!shouldUpdate) {
            return
        }
        val newJarFile =
            downloadLatestJarFile().getOrElse {
                Logger.error { "❌ An error occurred while downloading the latest version: ${it.message}" }
                return
            }
        Logger.info { "ℹ\uFE0F The new update has been downloaded, will close the application." }
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

                        if (!shouldShowSuccessMessage()) {
                            return@Thread
                        }

                        // The Application has been updated without automatically relaunching it, showing a message
                        val (windowTitle, message) = buildUpdateSuccessMessage()
                        val commandArgs: Array<String> =
                            when (OperatingSystem.current) {
                                OperatingSystem.Linux ->
                                    when (LinuxDesktopEnvironment.current) {
                                        LinuxDesktopEnvironment.KdePlasma ->
                                            arrayOf(
                                                "kdialog",
                                                "--title",
                                                windowTitle,
                                                "--msgbox",
                                                message,
                                            )

                                        else ->
                                            arrayOf(
                                                "zenity",
                                                "--info",
                                                "--title",
                                                windowTitle,
                                                "--text",
                                                message,
                                            )
                                    }

                                OperatingSystem.MacOS ->
                                    arrayOf(
                                        "osascript",
                                        "-e",
                                        "display dialog \"$message\" with title \"$windowTitle\" buttons {\"OK\"} default button \"OK\"",
                                    )

                                else -> error("The operating system is expected to be either Linux or macOS in the current check.")
                            }
                        commandLineNonBlocking(
                            *commandArgs,
                        ).getOrThrow()
                    },
                )
            }

            OperatingSystem.Windows -> {
                // On Windows, we can't rename, delete or modify the current running JAR file due to file locking.
                // Will create a batch script, execute it in a different windows process
                // and close the application immediately; the batch script expects
                // the application to be closed after a short delay.
                // The batch script will handle the update process

                val updateBatScriptFile =
                    SyncScriptDotMinecraftFiles.SyncScriptData.Temp.path
                        .resolve("update.bat")
                withContext(Dispatchers.IO) {
                    updateBatScriptFile.createFileWithParentDirectoriesOrTerminate()
                }
                val secondsToWait = 1

                val (windowTitle, message) = buildUpdateSuccessMessage()

                val messageVbsFilePath =
                    SyncScriptDotMinecraftFiles.SyncScriptData.Temp.path
                        .resolve("updateMessage.vbs")

                updateBatScriptFile.writeText(
                    """
                    @echo off
                    
                    echo Waiting for $secondsToWait second to ensure application closure...
                    timeout /t $secondsToWait > nul
                    del "${currentRunningJarFilePath.absolutePathString()}"
                    move "${newJarFilePath.absolutePathString()}" "${currentRunningJarFilePath.absolutePathString()}"
                    
                    ${
                        if (shouldShowSuccessMessage()) {
                            """
                            echo MsgBox "$message", 64, "$windowTitle" > "${messageVbsFilePath.absolutePathString()}"
                            cscript //nologo "${messageVbsFilePath.absolutePathString()}"
                            del "${messageVbsFilePath.absolutePathString()}"
                            """.trimIndent()
                        } else {
                            ""
                        }
                    }

                    exit
                    """.trimIndent(),
                )
                executeBatchScriptInSeparateWindow(
                    batScriptFilePath = updateBatScriptFile,
                )
            }

            OperatingSystem.Unknown -> {
                Logger.error { "⚠\uFE0F Auto update feature is not supported on ${SystemInfoProvider.getOperatingSystemName()}." }
            }
        }
        // Will require the user to launch once again after the update.
        terminateWithOrWithoutError()
    }

    private fun buildUpdateSuccessMessage(): Pair<String, String> =
        Pair(
            "Update Complete",
            "${ProjectInfoConstants.DISPLAY_NAME} has been updated. Relaunch to use the new version.",
        )

    private fun shouldShowSuccessMessage(): Boolean = GuiState.isGuiEnabled
}
