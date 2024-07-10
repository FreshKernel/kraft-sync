package utils

import generated.BuildConfig

/**
 * A simple logger that doesn't require additional dependencies and is not compatible
 * with known logging solutions. Most of the messages will be shown to the user
 * in case they run the application in CLI mode either in command-line or using the launcher,
 * which is why don't use something like `[main] INFO Main - The log message` in production mode.
 * */
object Logger {
    fun error(lazyMessage: () -> String) {
        logMessage(lazyMessage = lazyMessage, logLevel = "Error")
    }

    fun debug(lazyMessage: () -> String) {
        if (!BuildConfig.DEVELOPMENT_MODE) return
        logMessage(lazyMessage = lazyMessage, logLevel = "Debug")
    }

    fun info(lazyMessage: () -> String) {
        logMessage(lazyMessage = lazyMessage, logLevel = "Info")
    }

    fun warning(lazyMessage: () -> String) {
        logMessage(lazyMessage = lazyMessage, logLevel = "Warning")
    }

    fun trace(lazyMessage: () -> String) {
        logMessage(lazyMessage = lazyMessage, logLevel = "Trace")
    }

    private fun logMessage(
        lazyMessage: () -> String,
        logLevel: String,
    ) {
        val message =
            buildString {
                if (BuildConfig.DEVELOPMENT_MODE) {
                    append("$logLevel - ")
                }
                append(lazyMessage())
            }
        println(message)
    }
}
