package services.syncScriptInstaller

import constants.DotMinecraftFileNames
import constants.ProjectInfoConstants
import constants.SharedConstants
import launchers.LauncherDataSource
import launchers.LauncherDataSourceFactory
import launchers.MinecraftLauncher
import utils.deleteRecursivelyWithLegacyJavaIo
import java.nio.file.Paths
import kotlin.io.path.copyTo
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists

class SyncScriptInstallerImpl : SyncScriptInstaller {
    override suspend fun configureInstallation(
        installationConfig: SyncScriptInstallationConfig,
        launcher: MinecraftLauncher,
        launcherInstanceDirectoryPathString: String,
        confirmReplaceExistingPreLaunchCommand: Boolean,
        shouldEnableGui: Boolean,
    ): SyncScriptInstallationResult {
        return try {
            if (launcherInstanceDirectoryPathString.isBlank()) {
                return SyncScriptInstallationResult.Failure(
                    error = SyncScriptInstallationError.EmptyLauncherInstanceDirectoryPath,
                )
            }
            val launcherInstanceDirectoryPath = Paths.get(launcherInstanceDirectoryPathString)
            if (!launcherInstanceDirectoryPath.exists()) {
                return SyncScriptInstallationResult.Failure(
                    error = SyncScriptInstallationError.LauncherInstanceDirectoryNotFound,
                )
            }
            val launcherDataSource: LauncherDataSource = LauncherDataSourceFactory.getHandler(launcher)

            launcherDataSource
                .validateInstanceDirectory(
                    launcherInstanceDirectoryPath = launcherInstanceDirectoryPath,
                ).getOrElse {
                    return SyncScriptInstallationResult.Failure(
                        error =
                            SyncScriptInstallationError.InvalidLauncherInstanceDirectory(
                                message = it.toString(),
                                exception = it,
                            ),
                    )
                }

            val newSyncScriptJarFileName = "${ProjectInfoConstants.NORMALIZED_NAME}.jar"
            val newSyncScriptJarFilePath = launcherInstanceDirectoryPath.resolve(newSyncScriptJarFileName)

            when (installationConfig) {
                is SyncScriptInstallationConfig.Install -> {
                    val syncScriptJarFilePathString =
                        installationConfig.getSyncScriptJarFilePathString()
                            ?: return SyncScriptInstallationResult.Cancelled
                    if (syncScriptJarFilePathString.isBlank()) {
                        return SyncScriptInstallationResult.Failure(
                            error = SyncScriptInstallationError.EmptySyncScriptJarFilePath,
                        )
                    }
                    val providedSyncScriptJarFilePath = Paths.get(syncScriptJarFilePathString)
                    if (!providedSyncScriptJarFilePath.exists()) {
                        return SyncScriptInstallationResult.Failure(
                            error = SyncScriptInstallationError.SyncScriptJarFileNotFound,
                        )
                    }
                    providedSyncScriptJarFilePath.copyTo(
                        newSyncScriptJarFilePath,
                        overwrite = true,
                    )
                }

                SyncScriptInstallationConfig.UnInstall -> {
                    try {
                        newSyncScriptJarFilePath.deleteIfExists()
                    } catch (e: Exception) {
                        return SyncScriptInstallationResult.Failure(
                            error =
                                SyncScriptInstallationError.CouldNotDeleteSyncScriptJarFileWhileUninstall(
                                    message = e.toString(),
                                    exception = e,
                                ),
                        )
                    }

                    try {
                        launcherInstanceDirectoryPath
                            .resolve(DotMinecraftFileNames.SYNC_SCRIPT_DIRECTORY)
                            .deleteRecursivelyWithLegacyJavaIo()
                    } catch (e: Exception) {
                        return SyncScriptInstallationResult.Failure(
                            error =
                                SyncScriptInstallationError.CouldNotDeleteSyncScriptDataWhileUninstall(
                                    message = e.toString(),
                                    exception = e,
                                ),
                        )
                    }
                }
            }

            // Without arguments
            val newLaunchCommand =
                when (launcher) {
                    MinecraftLauncher.Official -> throw NotImplementedError()
                    MinecraftLauncher.MultiMc, MinecraftLauncher.PrismLauncher, MinecraftLauncher.ATLauncher ->
                        "\$INST_JAVA -jar \$INST_MC_DIR/$newSyncScriptJarFileName"

                    MinecraftLauncher.ModrinthApp -> TODO() // TODO: https://github.com/modrinth/code/pull/1254
                    MinecraftLauncher.GDLauncher -> TODO()
                }

            // With arguments
            val newFullCommandWithArgs =
                buildString {
                    append(newLaunchCommand)
                    if (!shouldEnableGui) {
                        append(" ${SharedConstants.DISABLE_GUI_ARG_NAME}")
                    }
                }

            val newCommandToSet = if (installationConfig is SyncScriptInstallationConfig.Install) newFullCommandWithArgs else null

            val currentCommand =
                launcherDataSource
                    .getPreLaunchCommand(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath)
                    .getOrThrow()

            if (!confirmReplaceExistingPreLaunchCommand) {
                // If there is already a command exists, confirm if the user wants to replace the current
                // unless if the command is used to launch the script that the user trying to install
                if ((currentCommand != null && !currentCommand.startsWith(newLaunchCommand))) {
                    return SyncScriptInstallationResult.RequiresUserConfirmationToReplacePreLaunchCommand(
                        existingCommand = currentCommand,
                        newCommand = newCommandToSet.toString(),
                    )
                }
            }

            launcherDataSource
                .setPreLaunchCommand(
                    command = newCommandToSet,
                    launcherInstanceDirectoryPath = launcherInstanceDirectoryPath,
                ).getOrElse {
                    return SyncScriptInstallationResult.Failure(
                        error =
                            SyncScriptInstallationError.CouldNotSetPreLaunchCommand(
                                message = it.toString(),
                                exception = it,
                            ),
                    )
                }
            SyncScriptInstallationResult.Success
        } catch (e: Exception) {
            SyncScriptInstallationResult.Failure(
                error =
                    SyncScriptInstallationError.UnknownError(
                        message = e.toString(),
                        exception = e,
                    ),
            )
        }
    }
}
