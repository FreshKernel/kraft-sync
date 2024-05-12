package syncInfo.models

import kotlinx.serialization.Serializable
import util.getSHA256Checksum
import util.getSHA512Checksum
import java.io.File

// TODO: I might add support for SHA1 and other types of hashing later

/**
 * A class that hold the data that is used for verifying a fle
 * will be used in [Mod] and other type of resources to verify them
 *
 * Used to verify the file's integrity
 * and trigger a re-download if the file is invalid or has been tampered with
 *
 * if all of them are null, [hasValidIntegrity] will return null, the file integrity will be unknown as
 * there is no way to validate it
 * */
@Serializable
data class FileIntegrityInfo(
    /**
     * The SHA-256 hash of the file.
     */
    val sha256: String? = null,
    /**
     * The SHA-512 hash of the file.
     */
    val sha512: String? = null,
    /**
     * The size/length of the file
     * */
    val size: Long? = null,
) {
    /**
     * Support for using one, some or all of the ways for validating a file
     * @return if none of them are specified, return `null` as unknown
     * otherwise `true` or `false`
     * */
    fun hasValidIntegrity(file: File): Boolean? {
        val validations = mutableListOf<Boolean>()
        if (size != null) {
            validations.add(size == file.length())
        }
        if (sha256 != null) {
            validations.add(sha256 == getSHA256Checksum(file.path))
        }
        if (sha256 != null) {
            validations.add(sha512 == getSHA512Checksum(file.path))
        }
        if (validations.isEmpty()) {
            // If none of the values specified, return null as unknown
            return null
        }
        return validations.all { it }
    }
}
