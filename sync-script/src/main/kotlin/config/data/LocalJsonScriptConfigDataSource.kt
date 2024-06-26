package config.data

import config.models.ScriptConfig
import constants.SyncScriptDotMinecraftFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import utils.JsonPrettyPrint

class LocalJsonScriptConfigDataSource : ScriptConfigDataSource {
    override suspend fun getConfig(): Result<ScriptConfig> =
        try {
            val scriptConfig =
                withContext(Dispatchers.IO) {
                    Json.decodeFromString<ScriptConfig>(
                        SyncScriptDotMinecraftFiles.SyncScriptData.ScriptConfig.file
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
                val configFile = SyncScriptDotMinecraftFiles.SyncScriptData.ScriptConfig.file
                if (!configFile.parentFile.exists()) {
                    configFile.parentFile.mkdirs()
                }
                configFile.writeText(JsonPrettyPrint.encodeToString(ScriptConfig.serializer(), scriptConfig))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
}
