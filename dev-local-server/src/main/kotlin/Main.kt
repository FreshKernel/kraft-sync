import com.sun.net.httpserver.HttpServer
import java.io.File
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.NetworkInterface
import kotlin.concurrent.thread

fun main() {
    println("📂 Current Directory: ${System.getProperty("user.dir")}")

    val fileName = "file.json"
    val file = File(fileName)
    println("ℹ\uFE0F File Path: ${file.absolutePath}")

    if (!file.exists()) {
        println("❌ The file to host in localhost does not exist: $fileName")
        return
    }

    val serverPort = System.getenv("PORT")?.toInt() ?: 8080
    val server = HttpServer.create(InetSocketAddress(serverPort), 0)

    server.createContext("/") { exchange ->
        if (exchange.requestMethod.equals("GET", ignoreCase = true)) {
            val responseText = file.readText()
            exchange.sendResponseHeaders(200, responseText.toByteArray().size.toLong())
            val outputStream: OutputStream = exchange.responseBody
            outputStream.use { it.write(responseText.toByteArray()) }
        } else {
            exchange.sendResponseHeaders(405, -1)
        }
    }

    server.executor = null
    server.start()

    println("\n🚀 Server started at http://localhost:$serverPort/")
    println("\uD83C\uDFE0 The server can be accessed locally at http://${getLocalIpAddress()}:$serverPort/")

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
