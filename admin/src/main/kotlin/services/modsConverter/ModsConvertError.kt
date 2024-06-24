package services.modsConverter

sealed class ModsConvertError {
    data object EmptyLauncherInstanceDirectoryPath : ModsConvertError()

    data object LauncherInstanceDirectoryNotFound : ModsConvertError()

    data class InvalidLauncherInstanceDirectory(
        val message: String,
        val exception: Throwable,
    ) : ModsConvertError()

    data class CurseForgeApiCheckError(
        val message: String,
        val exception: Throwable,
    ) : ModsConvertError()

    data class ModsAvailabilityCheckError(
        val message: String,
        val exception: Throwable,
    ) : ModsConvertError()

    data class CouldNotConvertMods(
        val message: String,
        val exception: Throwable,
    ) : ModsConvertError()

    data class ModsUnavailable(
        val happenedWhileConvertingMods: Boolean,
    ) : ModsConvertError()

    data class UnknownError(
        val message: String,
        val exception: Exception,
    ) : ModsConvertError()
}
