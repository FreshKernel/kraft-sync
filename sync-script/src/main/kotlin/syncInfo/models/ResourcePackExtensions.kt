package syncInfo.models

import syncInfo.models.resourcePack.ResourcePack
import utils.getFileNameFromUrlOrError
import utils.showErrorMessageAndTerminate
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.system.exitProcess

/**
 * The name that will be used to display in the console or GUI
 * will use the [ResourcePack.name] which is the resource-pack display name / title
 * will use [getFileNameFromUrlOrError] as an alternative if it's not available
 * */
fun ResourcePack.getDisplayName(): String = name ?: getFileNameFromUrlOrError(downloadUrl)

/**
 * Allow overriding the value for a specific resource-pack, or all the resource-packs, or use a global value for all the assets.
 * */
fun ResourcePack.shouldVerifyFileIntegrity(): Boolean =
    verifyFileIntegrity ?: SyncInfo.instance.resourcePackSyncInfo.verifyFilesIntegrity
        ?: SyncInfo.instance.verifyAssetFilesIntegrity

suspend fun ResourcePack.hasValidFileIntegrityOrError(resourcePackFilePath: Path): Boolean? =
    this.fileIntegrityInfo.hasValidIntegrity(filePath = resourcePackFilePath).getOrElse {
        showErrorMessageAndTerminate(
            title = "File Integrity Validation Error ⚠️",
            message =
                "An error occurred while validating the integrity of the resource-pack file " +
                    "(${resourcePackFilePath.name}) \uD83D\uDCC1.",
        )
        // This will never reach due to the previous statement stopping the application
        exitProcess(1)
    }
