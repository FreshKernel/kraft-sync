package syncService

import constants.SyncScriptDotMinecraftFiles
import net.benwoodworth.knbt.Nbt
import net.benwoodworth.knbt.NbtCompound
import net.benwoodworth.knbt.NbtCompression
import net.benwoodworth.knbt.NbtList
import net.benwoodworth.knbt.NbtTag
import net.benwoodworth.knbt.NbtVariant
import net.benwoodworth.knbt.add
import net.benwoodworth.knbt.buildNbtCompound
import net.benwoodworth.knbt.buildNbtList
import net.benwoodworth.knbt.decodeFromStream
import net.benwoodworth.knbt.encodeToStream
import net.benwoodworth.knbt.nbtCompound
import net.benwoodworth.knbt.put
import syncInfo.models.SyncInfo
import syncInfo.models.instance
import utils.ExecutionTimer
import utils.buildHtml
import utils.isFileEmpty
import utils.showErrorMessageAndTerminate
import java.io.EOFException
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile
import kotlin.io.path.name
import kotlin.io.path.outputStream
import kotlin.io.path.pathString
import kotlin.system.exitProcess

class ServersSyncService : SyncService {
    private val serversDatFilePath = SyncScriptDotMinecraftFiles.ServersDat.path
    private val serversSyncService = SyncInfo.instance.serverSyncInfo

    companion object {
        const val SERVER_NBT_MAIN_COMPOUND_KEY = ""
    }

    private val nbt =
        Nbt {
            variant = NbtVariant.Java
            compression = NbtCompression.None
        }

    override suspend fun syncData() {
        val executionTimer = ExecutionTimer()
        executionTimer.setStartTime()

        println("\n\uD83D\uDD04 Syncing server list...")

        val currentRootCompound =
            loadServersDatFile().getOrElse {
                showErrorMessageAndTerminate(
                    title = "📁 File Loading Error",
                    message =
                        buildHtml {
                            text("⚠ Unable to read the server list from the file '${serversDatFilePath.pathString}': ")
                            newLine()
                            text("$it")
                            newLines(2)
                            if (it is EOFException) {
                                text("This issue might occur if the file is corrupt or incomplete.")
                                newLine()
                                text("As a potential workaround, consider deleting the file '${serversDatFilePath.pathString}'.")
                            } else {
                                text("Deleting the file '${serversDatFilePath.pathString}' could resolve the issue.")
                            }
                            newLine()
                            boldText("Note: Deleting this file will reset the server list in the game.")
                        }.buildBodyAsText(),
                )
                return
            }

        val newServerListCompound: NbtList<NbtCompound> =
            buildNbtList {
                serversSyncService.servers.map {
                    val serverCompound =
                        buildNbtCompound {
                            put("ip", it.address)
                            put("name", it.name)
                        }
                    add(serverCompound)
                }
            }

        val newRootCompound =
            currentRootCompound.let {
                val mutableRootMap = it.toMutableMap()

                val mutableMainMap = it[SERVER_NBT_MAIN_COMPOUND_KEY]?.nbtCompound?.toMutableMap() ?: mutableMapOf()
                mutableMainMap["servers"] = newServerListCompound

                mutableRootMap[SERVER_NBT_MAIN_COMPOUND_KEY] = NbtCompound(mutableMainMap)

                NbtCompound(mutableRootMap)
            }

        updateServersDatFile(newRootCompound = newRootCompound).getOrElse {
            showErrorMessageAndTerminate(
                title = "🚨 File Update Error",
                message = "⚠️ Unable to update the '${serversDatFilePath.name}' file: $it",
            )
            return
        }

        println("\uD83D\uDD52 Finished syncing the server list in ${executionTimer.getRunningUntilNowDuration().inWholeMilliseconds}ms.")
    }

    private fun loadServersDatFile(): Result<NbtCompound> {
        return try {
            if (serversDatFilePath.exists()) {
                if (!serversDatFilePath.isRegularFile()) {
                    showErrorMessageAndTerminate(
                        title = "❌ Invalid '${serversDatFilePath.name}' File",
                        message =
                            "\uD83D\uDEE0 '${serversDatFilePath.name}' must be a file \uD83D\uDCC2, a directory/folder was found instead.",
                    )
                    // This will never reach due to the previous statement stopping the application
                    exitProcess(1)
                }
                if (serversDatFilePath.isFileEmpty()) {
                    println("ℹ️ The file '${serversDatFilePath.name}' exists and is currently empty.")
                    return Result.success(createEmptyServerCompound())
                }
                return Result.success(
                    serversDatFilePath
                        .inputStream()
                        .use { inputStream ->
                            nbt.decodeFromStream<NbtTag>(inputStream)
                        } as NbtCompound,
                )
            }
            Result.success(createEmptyServerCompound())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun updateServersDatFile(newRootCompound: NbtCompound): Result<Unit> =
        runCatching {
            serversDatFilePath
                .outputStream()
                .use { outputStream ->
                    nbt.encodeToStream(newRootCompound, outputStream)
                }
        }

    private fun createEmptyServerCompound(): NbtCompound = NbtCompound(mapOf(SERVER_NBT_MAIN_COMPOUND_KEY to NbtCompound(emptyMap())))
}
