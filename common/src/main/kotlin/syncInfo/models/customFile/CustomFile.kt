package syncInfo.models.customFile

import kotlinx.serialization.Serializable
import syncInfo.models.FileIntegrityInfo
import syncInfo.models.SyncInfo

@Serializable
data class CustomFile(
    /**
     * Specifies the file path where the file will be stored relative to the current working directory,
     * which corresponds to the `.minecraft` directory (located alongside the `mods` directory).
     *
     * For example,
     * If the specified path is `config/mod-config.json` and the `.minecraft` directory is
     * `C:\Users\Username\AppData\Roaming\.minecraft`, the resulting file will be created at:
     * `C:\Users\Username\AppData\Roaming\.minecraft\config\mod-config.json`.
     *
     * The content of this file will be sourced from [downloadUrl].
     *
     * The file and all necessary parent directories will be created recursively.
     * */
    val filePath: String,
    val downloadUrl: String,
    val fileIntegrityInfo: FileIntegrityInfo = FileIntegrityInfo(),
    /**
     * Overrides [CustomFileSyncInfo.verifyFilesIntegrity] and [SyncInfo.verifyAssetFilesIntegrity]
     * for a file.
     * */
    val verifyFileIntegrity: Boolean? = null,
)
