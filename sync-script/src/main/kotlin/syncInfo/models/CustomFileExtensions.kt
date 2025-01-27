package syncInfo.models

import syncInfo.models.customFile.CustomFile
import utils.showErrorMessageAndTerminate
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.system.exitProcess

/**
 * Whether to validate the file integrity. Can set globally to all files or all custom files or a specific custom file.
 * */
fun CustomFile.shouldVerifyFileIntegrity(): Boolean =
    verifyFileIntegrity ?: SyncInfo.instance.customFileSyncInfo?.verifyFilesIntegrity
    ?: SyncInfo.instance.verifyAssetFilesIntegrity

suspend fun CustomFile.hasValidFileIntegrityOrError(filePath: Path): Boolean? =
    this.fileIntegrityInfo.hasValidIntegrity(filePath = filePath).getOrElse {
        showErrorMessageAndTerminate(
            title = "File Integrity Validation Error ⚠️",
            message = "An error occurred while validating the integrity of the custom-file file (${filePath.name}) \uD83D\uDCC1.",
        )
        // This will never reach due to the previous statement stopping the application
        exitProcess(1)
    }
