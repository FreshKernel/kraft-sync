package config.data

import config.models.ScriptConfig
import constants.SyncScriptInstanceFiles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import utils.JsonPrettyPrint

class LocalJsonScriptConfigDataSource : ScriptConfigDataSource {
    override suspend fun getConfig(): Result<ScriptConfig> {
        return try {
            val scriptConfig =
                withContext(Dispatchers.IO) {
                    Json.decodeFromString<ScriptConfig>(SyncScriptInstanceFiles.SyncScriptData.ScriptConfig.file.readText())
                }
            Result.success(scriptConfig)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun replaceConfig(scriptConfig: ScriptConfig): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                val configFile = SyncScriptInstanceFiles.SyncScriptData.ScriptConfig.file
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
}
