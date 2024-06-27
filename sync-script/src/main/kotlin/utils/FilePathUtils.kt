package utils

import java.nio.file.CopyOption
import java.nio.file.Path
import kotlin.io.path.deleteExisting
import kotlin.io.path.moveTo
import kotlin.io.path.name
import kotlin.io.path.pathString

fun Path.deleteExistingOrTerminate(
    fileEntityType: String,
    reasonOfDelete: String,
) {
    try {
        this.deleteExisting()
    } catch (e: Exception) {
        showErrorMessageAndTerminate(
            title = "File Deletion Error ⚠️",
            message = "❌ Failed to delete the $fileEntityType file '${this.name}' as $reasonOfDelete: ${e.message}",
        )
    }
}

fun Path.moveToOrTerminate(
    target: Path,
    overwrite: Boolean = false,
    fileEntityType: String,
) {
    try {
        this.moveTo(target = target, overwrite = overwrite)
    } catch (e: Exception) {
        showErrorMessageAndTerminate(
            title = "File Moving Error ⚠\uFE0F",
            message = "❌ Failed to move the $fileEntityType file '${this.pathString}' to '${target.pathString}': ${e.message}",
        )
    }
}

fun Path.moveToOrTerminate(
    target: Path,
    vararg options: CopyOption,
    fileEntityType: String,
) {
    try {
        this.moveTo(target = target, *options)
    } catch (e: Exception) {
        showErrorMessageAndTerminate(
            title = "File Moving Error ⚠\uFE0F",
            message = "❌ Failed to move the $fileEntityType file '${this.pathString}' to '${target.pathString}': ${e.message}",
        )
    }
}
