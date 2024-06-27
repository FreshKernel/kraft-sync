import com.sun.net.httpserver.HttpServer
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.concurrent.thread
import kotlin.io.path.exists
import kotlin.io.path.isRegularFile
import kotlin.io.path.readText
import kotlin.streams.toList

fun main() {
    println("📂 Current Directory: ${System.getProperty("user.dir")}")

    val serverPort = System.getenv("PORT")?.toInt() ?: 8080
    val server = HttpServer.create(InetSocketAddress(serverPort), 0)

    server.createContext("/") { exchange ->
        when {
            exchange.requestMethod.equals("GET", ignoreCase = true) -> {
                val path = exchange.requestURI.path
                val requestedFile = Paths.get(path.substring(1))

                if (requestedFile.exists() && requestedFile.isRegularFile()) {
                    val responseText = requestedFile.readText()
                    exchange.sendResponseHeaders(200, responseText.toByteArray().size.toLong())
                    val outputStream: OutputStream = exchange.responseBody
                    outputStream.use { it.write(responseText.toByteArray()) }
                } else {
                    exchange.sendResponseHeaders(404, -1)
                }
            }

            else -> {
                exchange.sendResponseHeaders(405, -1)
            }
        }
    }

    server.executor = null
    server.start()

    println("\n🚀 Server started at http://localhost:$serverPort/")
    println("\uD83C\uDFE0 The server can be accessed locally at http://${getLocalIpAddress()}:$serverPort/")

    println("\n\uD83D\uDCC1 The files:")

    Files
        .walk(Paths.get(""))
        .use { paths ->
            paths.filter { it.isRegularFile() }.toList()
        }.forEach {
            println("\uD83C\uDF10 http://localhost:$serverPort/$it")
        }

    listenForQToStop(server = server)
}

private fun listenForQToStop(server: HttpServer) {
    // Start a new thread to listen for "q" key press
    thread {
        println("\n🛑 Press 'q' and then press enter to stop the server.")
        while (true) {
            if (readlnOrNull()?.trim()?.equals("q", ignoreCase = true) == true) {
                server.stop(0)
                println("✅ Server stopped successfully.")
                break
            }
            println("⚠️ Invalid input. Press 'q' and then press enter to stop the server.")
        }
    }
}

fun getLocalIpAddress(): String? =
    try {
        NetworkInterface
            .getNetworkInterfaces()
            .asSequence()
            .toList()
            .flatMap { it.inetAddresses.asSequence().toList() }
            .firstOrNull { !it.isLoopbackAddress && it.isSiteLocalAddress }
            ?.hostAddress
    } catch (ex: Exception) {
        ex.printStackTrace()
        null
    }
