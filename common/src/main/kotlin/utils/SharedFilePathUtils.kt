package utils

import java.nio.file.FileSystemException
import java.nio.file.Path
import kotlin.io.path.name

fun Path.deleteRecursivelyWithLegacyJavaIo() {
    if (!toFile().deleteRecursively()) {
        throw FileSystemException("Could not delete the directory '$name' recursively.")
    }
}
