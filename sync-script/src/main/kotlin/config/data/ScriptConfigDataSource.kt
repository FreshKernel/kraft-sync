package config.data

import config.models.ScriptConfig

interface ScriptConfigDataSource {
    suspend fun getConfig(): Result<ScriptConfig>

    /**
     * Create the config if it doesn't exist, and replace the data
     * */
    suspend fun replaceConfig(scriptConfig: ScriptConfig): Result<Unit>
}
