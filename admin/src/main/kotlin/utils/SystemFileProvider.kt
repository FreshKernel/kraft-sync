package utils

import utils.os.OperatingSystem
import java.io.File
import java.nio.file.Paths

object SystemFileProvider {
    /**
     * @return Get the directory where the applications store their data for the current [OperatingSystem]
     * */
    private fun getUserApplicationDataRootDirectory(): Result<File?> {
        val directory =
            when (OperatingSystem.current) {
                OperatingSystem.Linux ->
                    Paths
                        .get(
                            SystemInfoProvider.getUserHomeDirectoryPath(),
                            ".local",
                            "share",
                        ).toFile()

                OperatingSystem.MacOS ->
                    Paths
                        .get(
                            SystemInfoProvider.getUserHomeDirectoryPath(),
                            "Library",
                            "Application Support",
                        ).toFile()

                OperatingSystem.Windows ->
                    SystemInfoProvider.getWindowsAppDataDirectory()?.let {
                        Paths
                            .get(
                                it,
                            ).toFile()
                    }

                OperatingSystem.Unknown -> {
                    return Result.failure(
                        UnsupportedOperationException(
                            "The operating system ${SystemInfoProvider.getOperatingSystemName()} is not supported to " +
                                "retrieve the application data directory.",
                        ),
                    )
                }
            }
        return Result.success(directory)
    }

    /**
     * @return The directory where a specific application stores its data for the current [OperatingSystem]
     * @param applicationDirectoryName The name of the application directory, it depends on the application
     * and might not be the application name, or it could with a slightly different name
     * */
    fun getUserApplicationDataDirectory(applicationDirectoryName: String): Result<File?> =
        try {
            val directory = getUserApplicationDataRootDirectory().getOrThrow()?.resolve(applicationDirectoryName)
            Result.success(directory)
        } catch (e: Exception) {
            Result.failure(e)
        }

    fun getFlatpakApplicationDataDirectory(flatpakApplicationId: String): Result<File> {
        if (!OperatingSystem.current.isLinux()) {
            return Result.failure(
                UnsupportedOperationException(
                    "The application attempting to retrieve the application data directory of ($flatpakApplicationId) and" +
                        " Flatpak is supported on Linux.",
                ),
            )
        }
        return Result.success(
            Paths
                .get(SystemInfoProvider.getUserHomeDirectoryPath(), ".var", "app", flatpakApplicationId, "data")
                .toFile(),
        )
    }

    /**
     * Same as [getUserApplicationDataDirectory] with Flatpak support for [OperatingSystem.Linux]
     * @return [Pair.first] is the directory and [Pair.second] is if this Flatpak specific directory
     * */
    fun getUserApplicationDataDirectoryWithFlatpakSupport(
        applicationDirectoryName: String,
        flatpakApplicationId: String,
    ): Result<Pair<File?, Boolean>> {
        return try {
            val directory =
                getUserApplicationDataDirectory(applicationDirectoryName = applicationDirectoryName).getOrThrow()
            if (OperatingSystem.current.isLinux() && (directory == null || !directory.exists())) {
                return Result.success(
                    Pair(
                        getFlatpakApplicationDataDirectory(flatpakApplicationId = flatpakApplicationId).getOrThrow(),
                        true,
                    ),
                )
            }
            Result.success(Pair(directory, false))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}