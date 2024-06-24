package services.syncScriptInstaller

import constants.MinecraftInstanceNames
import constants.ProjectInfoConstants
import launchers.LauncherDataSource
import launchers.LauncherDataSourceFactory
import launchers.MinecraftLauncher
import java.io.File

class SyncScriptInstallerImpl : SyncScriptInstaller {
    override suspend fun configureInstallation(
        installationConfig: SyncScriptInstallationConfig,
        launcher: MinecraftLauncher,
        launcherInstanceDirectoryPath: String,
        confirmReplaceExistingPreLaunchCommand: Boolean,
    ): SyncScriptInstallationResult {
        return try {
            if (launcherInstanceDirectoryPath.isBlank()) {
                return SyncScriptInstallationResult.Failure(
                    error = SyncScriptInstallationError.EmptyLauncherInstanceDirectoryPath,
                )
            }
            val launcherInstanceDirectory = File(launcherInstanceDirectoryPath)
            if (!launcherInstanceDirectory.exists()) {
                return SyncScriptInstallationResult.Failure(
                    error = SyncScriptInstallationError.LauncherInstanceDirectoryNotFound,
                )
            }
            val launcherDataSource: LauncherDataSource = LauncherDataSourceFactory.getHandler(launcher)

            launcherDataSource
                .validateInstanceDirectory(
                    launcherInstanceDirectory = launcherInstanceDirectory,
                ).getOrElse {
                    return SyncScriptInstallationResult.Failure(
                        error =
                            SyncScriptInstallationError.InvalidLauncherInstanceDirectory(
                                message = it.message.toString(),
                                exception = it,
                            ),
                    )
                }

            val newSyncScriptJarFileName = "${ProjectInfoConstants.NORMALIZED_NAME}.jar"
            val syncScriptJarFile = launcherInstanceDirectory.resolve(newSyncScriptJarFileName)

            when (installationConfig) {
                is SyncScriptInstallationConfig.Install -> {
                    if (installationConfig.syncScriptJarFilePath.isBlank()) {
                        return SyncScriptInstallationResult.Failure(
                            error = SyncScriptInstallationError.EmptySyncScriptJarFilePath,
                        )
                    }
                    val providedSyncScriptJarFile = File(installationConfig.syncScriptJarFilePath)
                    if (!providedSyncScriptJarFile.exists()) {
                        return SyncScriptInstallationResult.Failure(
                            error = SyncScriptInstallationError.SyncScriptJarFileNotFound,
                        )
                    }
                    // TODO: Unrelated: also might rename hasUserConfirmedToReplaceExistingPreLaunchCommand and hasUserAcceptedCurseForgeForStudiosTermsOfUse
                    providedSyncScriptJarFile.copyTo(
                        syncScriptJarFile,
                        overwrite = true,
                    )
                }

                SyncScriptInstallationConfig.UnInstall -> {
                    val isSyncScriptJarFileDeleted = syncScriptJarFile.delete()
                    if (!isSyncScriptJarFileDeleted && syncScriptJarFile.exists()) {
                        return SyncScriptInstallationResult.Failure(
                            error = SyncScriptInstallationError.CouldNotDeleteSyncScriptJarFileWhileUninstall,
                        )
                    }
                    val isSyncScriptDataExist =
                        File(launcherInstanceDirectory, MinecraftInstanceNames.SYNC_SCRIPT_FOLDER).deleteRecursively()
                    if (!isSyncScriptDataExist) {
                        return SyncScriptInstallationResult.Failure(
                            error = SyncScriptInstallationError.CouldNotDeleteSyncScriptDataWhileUninstall,
                        )
                    }
                }
            }

            val newCommand =
                when (launcher) {
                    MinecraftLauncher.Official -> throw NotImplementedError()
                    MinecraftLauncher.MultiMc, MinecraftLauncher.PrismLauncher, MinecraftLauncher.ATLauncher ->
                        "\$INST_JAVA -jar \$INST_MC_DIR/$newSyncScriptJarFileName"

                    MinecraftLauncher.ModrinthApp -> TODO()
                    MinecraftLauncher.GDLauncher -> TODO()
                }

            val newCommandToSet = if (installationConfig is SyncScriptInstallationConfig.Install) newCommand else null

            val currentCommand =
                launcherDataSource
                    .getPreLaunchCommand(launcherInstanceDirectory = launcherInstanceDirectory)
                    .getOrThrow()

            if ((currentCommand != null && currentCommand != newCommand) && !confirmReplaceExistingPreLaunchCommand) {
                return SyncScriptInstallationResult.RequiresUserConfirmationToReplacePreLaunchCommand(
                    existingCommand = currentCommand,
                    newCommand = newCommandToSet.toString(),
                )
            }

            launcherDataSource
                .setPreLaunchCommand(
                    command = newCommandToSet,
                    launcherInstanceDirectory = launcherInstanceDirectory,
                ).getOrElse {
                    return SyncScriptInstallationResult.Failure(
                        error =
                            SyncScriptInstallationError.CouldNotSetPreLaunchCommand(
                                message = it.message.toString(),
                                exception = it,
                            ),
                    )
                }
            SyncScriptInstallationResult.Success
        } catch (e: Exception) {
            SyncScriptInstallationResult.Failure(
                error =
                    SyncScriptInstallationError.UnknownError(
                        message = e.message.toString(),
                        exception = e,
                    ),
            )
        }
    }
}
