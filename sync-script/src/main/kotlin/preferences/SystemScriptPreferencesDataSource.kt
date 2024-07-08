package preferences

import constants.Constants
import services.hashGenerator.HashGeneratorInstance
import java.util.prefs.Preferences

class SystemScriptPreferencesDataSource : ScriptPreferencesDataSource {
    private suspend fun getDoesUserTrustSourceOfUrl(url: String): String {
        // Java Preferences won't accept too long keys, a workaround to make the URL shorter without losing the uniqueness
        return "${Constants.SYSTEM_PREFERENCES_PREFIX}doesUserTrustSource:${HashGeneratorInstance.generateMD5(text = url).getOrThrow()}"
    }

    private val preferences = Preferences.userRoot()

    override suspend fun doesUserTrustSource(sourceUrl: String): Result<Boolean> =
        try {
            val result = preferences.getBoolean(getDoesUserTrustSourceOfUrl(sourceUrl), false)
            Result.success(result)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }

    override suspend fun updateDoesUserTrustSource(
        sourceUrl: String,
        newValue: Boolean,
    ): Result<Unit> =
        try {
            val result = preferences.putBoolean(getDoesUserTrustSourceOfUrl(sourceUrl), newValue)
            Result.success(result)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
}
