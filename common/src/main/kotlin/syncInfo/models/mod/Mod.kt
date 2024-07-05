package syncInfo.models.mod

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import syncInfo.models.Environment
import syncInfo.models.FileIntegrityInfo
import syncInfo.models.SyncInfo

// When adding a new property, consider converting/import the new
// data from other launchers data format if possible

/**
 * The mod info that will be used by the script to download and validate it
 * */
@Serializable
data class Mod(
    /**
     * The public download url of the mod file
     * */
    val downloadUrl: String,
    /**
     * The [fileIntegrityInfo] contains different ways to validate a mod file integrity
     * */
    val fileIntegrityInfo: FileIntegrityInfo = FileIntegrityInfo(),
    /**
     * The mod name (optional) that might be displayed in GUI, if not present
     * will fall back to the file name from [downloadUrl]
     * */
    val name: String? = null,
    /**
     * The script needs this info to know if it should download the mod based on the [Environment]
     * if you're using a client side only and not needed in server,
     * pass [ModSupport.Required] to [clientSupport] and [ModSupport.Unsupported] to [serverSupport]
     * or if it's optional on the server, pass [ModSupport.Optional] to [serverSupport].
     *
     * If it's required on both sides, pass [ModSupport.Required] to both [clientSupport] and [serverSupport]
     * */
    val clientSupport: ModSupport = ModSupport.defaultValue(),
    /**
     * The script needs this info to know if it should download the mod based on the [Environment]
     * if you're using a server side only and not needed in a client,
     * pass [ModSupport.Required] to [serverSupport] and [ModSupport.Unsupported] to [clientSupport]
     * or if it's optional on the client, pass [ModSupport.Optional] to [clientSupport].
     *
     * If it's required on both sides, pass [ModSupport.Required] to both [clientSupport] and [serverSupport]
     * */
    val serverSupport: ModSupport = ModSupport.defaultValue(),
    /**
     * The description of the mod might be used in GUI or console
     * while downloading the mods, for example
     * */
    val description: String? = null,
    /**
     * Will override [ModSyncInfo.shouldVerifyFilesIntegrity] and [SyncInfo.shouldVerifyAssetFilesIntegrity]
     * for a specific mod
     * */
    val overrideShouldVerifyFileIntegrity: Boolean? = null,
) {
    @Serializable
    enum class ModSupport {
        @SerialName("required")
        Required,

        @SerialName("optional")
        Optional,

        @SerialName("unsupported")
        Unsupported,

        ;

        companion object {
            fun defaultValue() = Required
        }
    }
}
