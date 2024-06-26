package utils

import java.nio.file.Path
import kotlin.io.path.deleteExisting
import kotlin.io.path.name

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
