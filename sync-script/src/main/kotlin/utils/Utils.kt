package utils

import config.models.ScriptConfig
import constants.Constants
import gui.GuiState
import gui.utils.GuiUtils
import java.awt.Component
import kotlin.system.exitProcess

/**
 * If [ScriptConfig.overrideIsGuiEnabled] is true, will use [GuiUtils.showErrorMessage]
 * otherwise will just print an error message to the log
 *
 * @param guiParentComponent The Java Swing parent component that created/showed this error message
 * the [terminateWithOrWithoutError] will decide to exit with error or as normal exit
 *
 *
 * Whatever if it should use GUI to show error or not, will use [ScriptConfig.overrideIsGuiEnabled],
 * we might call [showErrorMessageAndTerminate] when having error while loading the [ScriptConfig]
 * (for example), which is why we can't [ScriptConfig.getInstanceOrThrow] because we will get not initialized error
 * while showing an error message
 * */
fun showErrorMessageAndTerminate(
    title: String,
    message: String,
    guiParentComponent: Component? = null,
) {
    if (GuiState.isGuiEnabled) {
        GuiUtils.showErrorMessage(
            title = title,
            message = message,
            parentComponent = guiParentComponent,
        )
    } else {
        println("❌ $title: $message")
    }
    terminateWithOrWithoutError()
}

/**
 * Identical to [getFileNameFromUrl] but will show an error message in case of error
 * */
fun getFileNameFromUrlOrError(url: String): String {
    val fileName =
        getFileNameFromUrl(url).getOrElse {
            showErrorMessageAndTerminate(
                title = "File Name Retrieval Error",
                message = "Failed to retrieve the file name from the download URL: $url",
            )
            // Will never be reached as showErrorMessage usually will stop the application
            throw IllegalArgumentException("Couldn't not extract the file name from: $url")
        }
    if (fileName != null) {
        return fileName
    }
    showErrorMessageAndTerminate(
        title = "File Name Retrieval Error",
        message = "Unexpected error while retrieving file name from the download URL: $url",
    )
    // Will never be reached as showErrorMessage usually will stop the application
    throw IllegalArgumentException("Couldn't not extract the file name from: $url")
}

/**
 * Close the script as error, also based on the [ScriptConfig.launchOnError] allow to not closing as error
 * so the launcher will launch the game anyway, it's important to not use [ScriptConfig.getInstanceOrThrow] because
 * the [ScriptConfig] might still not initialized yet, and we are calling this function when we got error while initialize
 * the [ScriptConfig]
 * */
fun terminateWithOrWithoutError() {
    if (ScriptConfig.instance?.launchOnError ?: Constants.LAUNCH_ON_ERROR_DEFAULT) {
        exitProcess(0)
    }
    exitProcess(1)
}

// 1 Mebibyte (MiB) = 1024 * 1024 bytes
private const val BYTES_IN_MEGABYTE = 1024.0 * 1024.0

/**
 * Convert bytes to megabytes
 * */
fun Long.convertBytesToMegabytes(): Double {
    val bytes = this
    return bytes / BYTES_IN_MEGABYTE
}

/**
 * Convert bytes to megabytes to human-readable as [String]
 * @return if [this] is less than 0, will be in the following format "0.55"
 * otherwise return without the fractions "15"
 * */
fun Long.convertBytesToReadableMegabytesAsString(): String {
    val bytes = this
    val megabytes = bytes.convertBytesToMegabytes()
    if (megabytes < 1) {
        return String.format("%.2f", megabytes)
    }
    return megabytes.toLong().toString()
}

/**
 * Calculates the progress percentage based on the current index and the total number of items in the list.
 *
 * The list should not be empty.
 *
 * @param currentIndex The zero-based index of the current item.
 * @return The progress percentage from 0 to 100 as an integer.
 * @throws IllegalArgumentException if totalItems is less than or equal to zero.
 */
fun <T> List<T>.calculateProgressByIndex(currentIndex: Int): Int {
    require(isNotEmpty()) { "The list must not be empty" }
    require(currentIndex in indices) { "Current index must be within the range of list" }
    return ((currentIndex + 1) * 100) / this.size
}
