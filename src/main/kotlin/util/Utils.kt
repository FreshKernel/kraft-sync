package util

import Constants
import ScriptConfig
import getScriptConfigOrThrow
import scriptConfig
import useGui
import util.gui.GuiUtils
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.net.URLDecoder
import java.security.MessageDigest
import kotlin.math.roundToLong
import kotlin.system.exitProcess

@Deprecated("No longer needed, initially we wanted to put the jar file along with the config etc... all in one folder")
fun getJarFile(): Result<File> {
    val codeSource = object {}.javaClass.enclosingClass.protectionDomain?.codeSource
    codeSource?.location?.let {
        return Result.success(File(it.toURI()))
    }
    return Result.failure(
        IllegalStateException("The running script jar file is null"),
    )
}

/**
 * // TODO: Make sure usages of [ScriptConfig.useGui] is correct
 * If [ScriptConfig.useGui] is true, will use [GuiUtils.showErrorMessage]
 * otherwise will just print error message to the log
 *
 * @param useGuiOverride if you would like to override [ScriptConfig.useGui] value
 * @param shouldTerminate Since this a script that do one task, by default any error will cause the script to fail and terminated
 *
 *
 * Whatever if it should use GUI to show error or not, will prefer [useGuiOverride], then [ScriptConfig.useGui],
 * we might call [showErrorMessage] when having error while loading the [ScriptConfig]
 * for example, which is why we can't [getScriptConfigOrThrow]
 * */
fun showErrorMessage(
    title: String,
    message: String,
    useGuiOverride: Boolean? = null,
    shouldTerminate: Boolean = true,
) {
    if (useGuiOverride ?: useGui) {
        GuiUtils.showErrorMessage(
            title = title,
            message = message,
        )
    } else {
        println("$title: $message")
    }
    if (shouldTerminate) {
        terminateWithError()
    }
}

/**
 * Print a list in a simple way
 * */
fun <T> prettyPrintList(
    list: List<T>,
    message: String,
) {
    println("\n$message")
    list.forEach {
        println(" - $it")
    }
    println("\n")
}

/**
 * Extracts the file name from a public download URL.
 *
 * This function parses the URL path and returns the last segment, which is typically the file name.
 * It also decodes any percent-encoded characters (like `%2B`) in the filename portion to ensure you get the
 * original un-encoded name.
 *
 * Let's say you pass `https://cdn.modrinth.com/data/AANobbMI/versions/IZskON6d/sodium-fabric-0.5.8%2Bmc1.20.6.jar`
 * to [url] then you will get `sodium-fabric-0.5.8+mc1.20.6.jar` as a return value
 *
 * @param url The public download URL from which to extract the file name.
 * @return The extracted file name, or null if the URL is invalid or doesn't have a clear file segment.
 *
 * @throws [IllegalArgumentException] if the provided URL is not a valid string.
 * */
fun getFileNameFromUrl(url: String): String? {
    if (url.isBlank()) {
        throw IllegalArgumentException("Provided URL cannot be empty or blank.")
    }
    val parts = url.split("/")
    return if (parts.isNotEmpty()) URLDecoder.decode(parts.last(), "UTF-8") else null
}

fun getResourceURL(resourceName: String): URL {
    return object {}.javaClass.classLoader.getResource(resourceName) ?: throw IllegalStateException(
        "Could not get the resource with name $resourceName from the resources, please make sure it exist",
    )
}

/**
 * Calculates the SHA-256 checksum of a file and returns it as a hexadecimal string.
 *
 * This function opens the specified file, calculates its SHA-256 hash using the
 * Java Cryptography Architecture (JCA), and returns the hash as a hexadecimal string.
 * If the file doesn't exist, the function returns null.
 *
 * @param filePath The path to the file for which to calculate the SHA-256 checksum.
 * @return The SHA-256 checksum of the file as a hexadecimal string, or null if the file doesn't exist.
 * @throws SecurityException If there is a security error accessing the JCA provider.
 *
 *
 * @author https://www.baeldung.com/sha-256-hashing-java
 */
fun getSHA256Checksum(filePath: String): String? {
    val file = File(filePath)
    if (!file.exists()) {
        return null
    }
    val inputStream = file.inputStream()
    inputStream.use {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(1024)
        var read: Int
        do {
            read = it.read(buffer)
            if (read > 0) {
                digest.update(buffer, 0, read)
            }
        } while (read != -1)
        val hash = digest.digest()
        val formatter = StringBuilder(hash.size * 2)
        for (b in hash) {
            val hex = String.format("%02x", b)
            formatter.append(hex)
        }
        return formatter.toString()
    }
}

/**
 * Calculates the SHA-512 checksum of a file and returns it as a hexadecimal string.
 *
 * This function opens the specified file, calculates its SHA-512 hash using the
 * Java Cryptography Architecture (JCA), and returns the hash as a hexadecimal string.
 * If the file doesn't exist, the function returns null.
 *
 * @param filePath The path to the file for which to calculate the SHA-256 checksum.
 * @return The SHA-512 checksum of the file as a hexadecimal string, or null if the file doesn't exist.
 * @throws SecurityException If there is a security error accessing the JCA provider.
 *
 * @author https://www.baeldung.com/sha-256-hashing-java
 */
fun getSHA512Checksum(filePath: String): String? {
    val file = File(filePath)
    if (!file.exists()) {
        return null
    }
    val digest = MessageDigest.getInstance("SHA-512")
    val inputStream = file.inputStream()
    val byteBuffer = ByteArray(1024)
    var bytesRead: Int
    while (inputStream.read(byteBuffer).also { bytesRead = it } != -1) {
        digest.update(byteBuffer, 0, bytesRead)
    }
    inputStream.close()
    val hashedBytes = digest.digest()
    return hashedBytes.fold("") { acc, byte -> acc + "%02x".format(byte) }
}

/**
 * Close the script as error, also based on the [ScriptConfig.launchOnError] allow to not closing as error
 * so the launcher will launch the game anyway, it's important to use [getScriptConfigOrThrow] because // TODO: Make sure usages of getScriptConfigOrThrow is correct
 * the [ScriptConfig] might still not initialized, and we are calling this function when we got error while initialize
 * the [ScriptConfig]
 * */
fun terminateWithError() {
    if (scriptConfig?.launchOnError ?: Constants.LAUNCH_ON_ERROR) {
        exitProcess(0)
    }
    exitProcess(1)
}

fun Long.convertBytesToMb(): Long {
    return (this / (1024f * 1024f)).roundToLong() // Convert bytes to MB
}
