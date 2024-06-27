package services.modsConverter

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import launchers.LauncherDataSource
import launchers.LauncherDataSourceFactory
import launchers.MinecraftLauncher
import services.modsConverter.models.ModsConvertMode
import syncInfo.models.SyncInfo
import utils.JsonPrettyPrint
import java.nio.file.Paths
import kotlin.io.path.exists

class ModsConverterImpl : ModsConverter {
    override suspend fun convertMods(
        launcher: MinecraftLauncher,
        launcherInstanceDirectoryPathString: String,
        convertMode: ModsConvertMode,
        prettyFormat: Boolean,
        overrideCurseForgeApiKey: String?,
        isCurseForgeForStudiosTermsOfServiceAccepted: Boolean,
    ): ModsConvertResult {
        return try {
            if (launcherInstanceDirectoryPathString.isBlank()) {
                return ModsConvertResult.Failure(
                    error = ModsConvertError.EmptyLauncherInstanceDirectoryPath,
                )
            }
            val launcherInstanceDirectoryPath = Paths.get(launcherInstanceDirectoryPathString)
            if (!launcherInstanceDirectoryPath.exists()) {
                return ModsConvertResult.Failure(
                    error = ModsConvertError.LauncherInstanceDirectoryNotFound,
                )
            }
            val launcherDataSource: LauncherDataSource = LauncherDataSourceFactory.getHandler(launcher)

            launcherDataSource
                .validateInstanceDirectory(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath)
                .getOrElse {
                    return ModsConvertResult.Failure(
                        error =
                            ModsConvertError.InvalidLauncherInstanceDirectory(
                                message = it.message.toString(),
                                exception = it,
                            ),
                    )
                }

            val hasMods =
                launcherDataSource.hasMods(launcherInstanceDirectoryPath = launcherInstanceDirectoryPath).getOrElse {
                    return ModsConvertResult.Failure(
                        error =
                            ModsConvertError.ModsAvailabilityCheckError(
                                message = it.message.toString(),
                                exception = it,
                            ),
                    )
                }

            if (!hasMods) {
                return ModsConvertResult.Failure(
                    error =
                        ModsConvertError.ModsUnavailable(
                            happenedWhileConvertingMods = false,
                        ),
                )
            }

            val isCurseForgeApiRequestNeeded =
                launcherDataSource
                    .isCurseForgeApiRequestNeededForConvertingMods(launcherInstanceDirectoryPath)
                    .getOrElse {
                        return ModsConvertResult.Failure(
                            error =
                                ModsConvertError.CurseForgeApiCheckError(
                                    message = it.message.toString(),
                                    exception = it,
                                ),
                        )
                    }
            if (isCurseForgeApiRequestNeeded && !isCurseForgeForStudiosTermsOfServiceAccepted) {
                return ModsConvertResult.RequiresAcceptanceOfCurseForgeForStudiosTermsOfUse
            }
            val mods =
                launcherDataSource
                    .getLauncherInstanceMods(
                        launcherInstanceDirectoryPath = launcherInstanceDirectoryPath,
                        overrideCurseForgeApiKey = overrideCurseForgeApiKey?.ifBlank { null },
                    ).getOrElse {
                        return ModsConvertResult.Failure(
                            error =
                                ModsConvertError.CouldNotConvertMods(
                                    message = it.message.toString(),
                                    exception = it,
                                ),
                        )
                    }

            if (mods.isEmpty()) {
                return ModsConvertResult.Failure(
                    error =
                        ModsConvertError.ModsUnavailable(
                            happenedWhileConvertingMods = false,
                        ),
                )
            }

            val json = if (prettyFormat) JsonPrettyPrint else Json
            val modsOutputText: String =
                when (convertMode) {
                    ModsConvertMode.InsideSyncInfo -> json.encodeToString(SyncInfo(mods = mods))
                    ModsConvertMode.AsModsList -> json.encodeToString(mods)
                }

            ModsConvertResult.Success(
                mods = mods,
                modsOutputText = modsOutputText,
            )
        } catch (e: Exception) {
            ModsConvertResult.Failure(error = ModsConvertError.UnknownError(e.message.toString(), e))
        }
    }
}
