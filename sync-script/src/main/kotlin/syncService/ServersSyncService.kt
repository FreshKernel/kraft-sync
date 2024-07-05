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
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

class ServersSyncService : SyncService {
    private val serversFilePath = SyncScriptDotMinecraftFiles.ServersDat.path
    private val serversSyncService = SyncInfo.instance.serverSyncInfo

    companion object {
        const val SERVER_NBT_MAIN_COMPOUND_KEY = ""
    }

    override suspend fun syncData() {
        val isServersFileExist = serversFilePath.exists()
        val nbt =
            Nbt {
                variant = NbtVariant.Java
                compression = NbtCompression.None
            }

        val currentRootCompound =
            if (isServersFileExist) {
                serversFilePath
                    .inputStream()
                    .use { inputStream ->
                        // TODO: If the file exist though empty or invalid this will cause exception
                        //  improve error handling
                        nbt.decodeFromStream<NbtTag>(inputStream)
                    } as NbtCompound
            } else {
                NbtCompound(mapOf(SERVER_NBT_MAIN_COMPOUND_KEY to NbtCompound(emptyMap())))
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

        serversFilePath
            .outputStream()
            .use { outputStream ->
                nbt.encodeToStream(newRootCompound, outputStream)
            }
    }
}
