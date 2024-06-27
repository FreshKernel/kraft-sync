package config.data

import config.models.ScriptConfig
import constants.SyncScriptDotMinecraftFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import utils.JsonPrettyPrint
import utils.createParentDirectoriesIfDoesNotExist
import kotlin.io.path.readText
import kotlin.io.path.writeText

class LocalJsonScriptConfigDataSource : ScriptConfigDataSource {
    override suspend fun getConfig(): Result<ScriptConfig> =
        try {
            val scriptConfig =
                withContext(Dispatchers.IO) {
                    Json.decodeFromString<ScriptConfig>(
                        SyncScriptDotMinecraftFiles.SyncScriptData.ScriptConfig.path
                            .readText(),
                    )
                }
            Result.success(scriptConfig)
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun replaceConfig(scriptConfig: ScriptConfig): Result<Unit> =
        try {
            withContext(Dispatchers.IO) {
                val configFile = SyncScriptDotMinecraftFiles.SyncScriptData.ScriptConfig.path
                configFile.createParentDirectoriesIfDoesNotExist()
                configFile.writeText(JsonPrettyPrint.encodeToString(ScriptConfig.serializer(), scriptConfig))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
