package utils

import java.net.URL

/**
 * @return The current running JAR file, or the JAR URI used to run the application from using `java -jar app.jar`
 * @throws IllegalStateException if the application is not being run from a JAR file
 * */
fun getRunningJarFileAsUrl(): Result<URL> {
    val codeSource = object {}.javaClass.enclosingClass.protectionDomain?.codeSource
    codeSource?.location?.let {
        if (!it.file.endsWith(".jar", ignoreCase = true)) {
            return Result.failure(
                IllegalStateException(
                    "The application is not being running from a JAR file and it try to access the information about the JAR file",
                ),
            )
        }
        return Result.success(it)
    }
    return Result.failure(
        IllegalStateException("The running JAR file or the code source location is null"),
    )
}
