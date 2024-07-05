package utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.fileSize
import kotlin.io.path.name
import kotlin.streams.toList

fun Path.deleteRecursivelyWithLegacyJavaIo() {
    if (!toFile().deleteRecursively()) {
        throw FileSystemException("Could not delete the directory '$name' recursively.")
    }
}

suspend fun Path.listFilteredPaths(filter: (path: Path) -> Boolean): Result<List<Path>> =
    try {
        val paths =
            withContext(Dispatchers.IO) {
                Files
                    .list(this@listFilteredPaths)
                    .use { stream ->
                        stream
                            .filter { path ->
                                filter(path)
                            }.toList()
                    }
            }
        Result.success(paths)
    } catch (e: Exception) {
        Result.failure(e)
    }

fun Path.isFileEmpty(): Boolean = fileSize() == 0L
