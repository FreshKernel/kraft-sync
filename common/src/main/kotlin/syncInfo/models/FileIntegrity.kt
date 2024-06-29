package syncInfo.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * A class that holds the data that is used for verifying a file
 *
 * Used to verify the file's integrity
 * and trigger a re-download if the file is invalid or has been tampered with
 *
 * If all of them are null, the file integrity will be unknown as
 * there is no way to validate it.
 *
 * By default, the script only validates the file name and usually the file name contains
 * the mod and minecraft version, so when you update some mods, the old versions will be deleted and be
 * downloaded once again, if you also want the script to verify each file and if it matches the file from the source
 * if not, then it will be deleted and re-downloaded again
 *
 * Also, if some files got corrupted because of killing the process, then this would be helpful to make sure
 * you have healthy files
 *
 * **Notice**: This option will only take effect for the files that have at least one non-null value in the [FileIntegrityInfo]
 * for example if [FileIntegrityInfo.sha256] or [FileIntegrityInfo.sizeInBytes] is not null, you can use one, some or all
 * of them, it's up to you, in short if you want to verify a file to be matched on the one the server, you have
 * to assign a value to at least one, use [FileIntegrityInfo.sha256] or [FileIntegrityInfo.sha512]
 * as it's validating the content to make sure it's valid and secure.
 *
 * If you want to verify all the files, then all the files need to have at least one value for one
 * of those discussed above
 *
 * */
@Serializable
data class FileIntegrityInfo(
    /**
     * The size/length of the file in bytes
     * */
    val sizeInBytes: Long? = null,
    /**
     * The MD5 hash of the file.
     * It'd highly recommend to use [sha256]
     */
    val md5: String? = null,
    /**
     * The SHA1 hash of the file.
     * Consider using [sha256] when possible.
     */
    val sha1: String? = null,
    /**
     * The SHA-256 hash of the file.
     */
    val sha256: String? = null,
    /**
     * The SHA-512 hash of the file.
     */
    val sha512: String? = null,
)

/**
 * An enum class represent the option that will be used to validate the file
 * using a specific method or option instead of using them all
 *
 * @property Strong Prefer the method that's the more secure and strong if available (sha512 > sha256 > sha1 > md5 > file size)
 * @property Medium Prefer the method that's a mix between [Strong] and [Unsecure] (sha256 > sha512 > sha1 > md5 > file size)
 * @property Unsecure Prefer the method that's fastest and less secure than [Strong] when possible (md5 > sha1 > sha256 > sha512 > file size)
 * @property LeastUnsecure Prefer the method that's the fastest and less secure when possible (file size > md5 > sha1 > sha256 > sha512)
 * @property FileSize Will prefer [FileIntegrityInfo.sizeInBytes] over the other methods (file size > sha512 > sha256 > sha1 > md5)
 * @property Md5 Will prefer [FileIntegrityInfo.md5] over the other methods (md5 > sha512 > sha256 > sha1 > file size)
 * @property Sha1 Will prefer [FileIntegrityInfo.sha1] over the other methods (sha1 > sha512 > sha256 > md5 > file size)
 * @property Sha256 Will prefer [FileIntegrityInfo.sha256] over the other methods (sha256 > sha512 > sha1 > md5 > file size)
 * @property Sha512 Will prefer [FileIntegrityInfo.sha512] over the other methods (sha512 > sha256 > sha1 > md5 > file size)
 *
 * */
@Serializable
enum class PreferredFileVerificationOption {
    @SerialName("secure")
    Strong,

    @SerialName("medium")
    Medium,

    @SerialName("unsecure")
    Unsecure,

    @SerialName("leastUnsecure")
    LeastUnsecure,

    @SerialName("fileSize")
    FileSize,

    @SerialName("md5")
    Md5,

    @SerialName("sha1")
    Sha1,

    @SerialName("sha256")
    Sha256,

    @SerialName("sha512")
    Sha512,
}
