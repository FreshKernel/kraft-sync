package preferences

import config.models.ScriptConfig

/**
 * The data that is stored on the system preferences and not by the config file
 * this data intended to be modified by the script
 * */
interface ScriptPreferencesDataSource {
    /**
     * Checks if this is the user's first time launching the script from the specified source [ScriptConfig.syncInfoUrl].
     */
    suspend fun doesUserTrustSource(sourceUrl: String): Result<Boolean>

    /**
     * Updates the information regarding whether this is the user's first time launching the script
     * from the specified source [ScriptConfig.syncInfoUrl].
     */
    suspend fun updateDoesUserTrustSource(
        sourceUrl: String,
        newValue: Boolean,
    ): Result<Unit>
}
