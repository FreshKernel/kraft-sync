package utils

import kotlinx.serialization.json.Json
import java.net.URL
import java.net.URLDecoder
import java.nio.file.Path
import kotlin.io.path.toPath

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
fun getFileNameFromUrl(url: String): Result<String?> {
    return try {
        if (url.isBlank()) {
            return Result.failure(IllegalArgumentException("Provided URL cannot be empty or blank."))
        }
        val parts = url.split("/")
        val fileName = if (parts.isNotEmpty()) URLDecoder.decode(parts.last(), "UTF-8") else null
        Result.success(fileName)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}

fun getClassLoader(): ClassLoader = object {}.javaClass.classLoader

fun getResourceAsURLOrThrow(resourceName: String): URL =
    getClassLoader().getResource(resourceName) ?: throw IllegalStateException(
        "Could not get the resource with name $resourceName from the resources, double check if it exist.",
    )

fun getResourceAsPathOrThrow(resourceName: String): Path = getResourceAsURLOrThrow(resourceName).toURI().toPath()

fun String.isValidUrl(): Boolean {
    val urlPattern = Regex("""^(https?://)?([\w\-.]+)+(:\d+)?(/[-._~/\w:@?=%&$+,()!*]*)?""")
    return (this.startsWith("http://") || this.startsWith("https://")) && urlPattern.matches(this)
}

fun String.baseUrl(): String {
    val url = URL(this)
    val port = if (url.port != -1) ":${url.port}" else ""
    return "${url.protocol}://${url.host}$port/"
}

// TODO: Use @JsonIgnoreUnknownKeys annotation
//  in https://github.com/Kotlin/kotlinx.serialization/releases/tag/v1.8.0 instead?
val JsonIgnoreUnknownKeys = Json { ignoreUnknownKeys = true }

val JsonPrettyPrint = Json { prettyPrint = true }
