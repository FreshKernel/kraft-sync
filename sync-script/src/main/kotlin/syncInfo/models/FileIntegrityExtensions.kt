package syncInfo.models

import services.hashGenerator.HashGeneratorInstance
import java.io.File

/**
 * Support for using one, some or all of the ways for validating a file
 * @return if none of them are specified, return `null` as unknown
 * otherwise `true` or `false`
 * */
private suspend fun FileIntegrityInfo.validateAll(file: File): Result<Boolean?> {
    return try {
        val validations = mutableListOf<Boolean>()

        if (sizeInBytes != null) {
            validations.add(sizeInBytes == file.length())
        }
        if (md5 != null) {
            validations.add(md5 == HashGeneratorInstance.generateMD5(file).getOrThrow())
        }
        if (sha1 != null) {
            validations.add(sha1 == HashGeneratorInstance.generateSHA1(file).getOrThrow())
        }
        if (sha256 != null) {
            validations.add(sha256 == HashGeneratorInstance.generateSHA256(file).getOrThrow())
        }
        if (sha512 != null) {
            validations.add(sha512 == HashGeneratorInstance.generateSHA512(file).getOrThrow())
        }
        if (validations.isEmpty()) {
            // If none of the values specified, return null as unknown
            return Result.success(null)
        }
        Result.success(validations.all { it })
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}

/**
 * Only validate the file using the preferred file integrity method/option
 * */
private suspend fun FileIntegrityInfo.validateOnlyPreferredOption(
    file: File,
    preferredFileVerificationOption: PreferredFileVerificationOption,
): Result<Boolean?> {
    return try {
        val isValidFileIntegrity =
            when (preferredFileVerificationOption) {
                PreferredFileVerificationOption.Strong ->
                    sha512?.let { it == HashGeneratorInstance.generateSHA512(file).getOrThrow() }
                        ?: sha256?.let { it == HashGeneratorInstance.generateSHA256(file).getOrThrow() }
                        ?: sha1?.let { it == HashGeneratorInstance.generateSHA1(file).getOrThrow() }
                        ?: md5?.let { it == HashGeneratorInstance.generateMD5(file).getOrThrow() }
                        ?: sizeInBytes?.let { it == file.length() }

                PreferredFileVerificationOption.Medium ->
                    sha256?.let { it == HashGeneratorInstance.generateSHA256(file).getOrThrow() }
                        ?: sha512?.let { it == HashGeneratorInstance.generateSHA512(file).getOrThrow() }
                        ?: sha1?.let { it == HashGeneratorInstance.generateSHA1(file).getOrThrow() }
                        ?: md5?.let { it == HashGeneratorInstance.generateMD5(file).getOrThrow() }
                        ?: sizeInBytes?.let { it == file.length() }

                PreferredFileVerificationOption.Unsecure ->
                    md5?.let { it == HashGeneratorInstance.generateMD5(file).getOrThrow() }
                        ?: sha1?.let { it == HashGeneratorInstance.generateSHA1(file).getOrThrow() }
                        ?: sha256?.let { it == HashGeneratorInstance.generateSHA256(file).getOrThrow() }
                        ?: sha512?.let { it == HashGeneratorInstance.generateSHA512(file).getOrThrow() }
                        ?: sizeInBytes?.let { it == file.length() }

                PreferredFileVerificationOption.LeastUnsecure ->
                    sizeInBytes?.let { it == file.length() }
                        ?: md5?.let { it == HashGeneratorInstance.generateMD5(file).getOrThrow() }
                        ?: sha1?.let { it == HashGeneratorInstance.generateSHA1(file).getOrThrow() }
                        ?: sha256?.let { it == HashGeneratorInstance.generateSHA256(file).getOrThrow() }
                        ?: sha512?.let { it == HashGeneratorInstance.generateSHA512(file).getOrThrow() }

                PreferredFileVerificationOption.FileSize ->
                    sizeInBytes?.let { it == file.length() }
                        ?: sha512?.let { it == HashGeneratorInstance.generateSHA512(file).getOrThrow() }
                        ?: sha256?.let { it == HashGeneratorInstance.generateSHA256(file).getOrThrow() }
                        ?: sha1?.let { it == HashGeneratorInstance.generateSHA1(file).getOrThrow() }
                        ?: md5?.let { it == HashGeneratorInstance.generateMD5(file).getOrThrow() }

                PreferredFileVerificationOption.Md5 ->
                    md5?.let { it == HashGeneratorInstance.generateMD5(file).getOrThrow() }
                        ?: sha512?.let { it == HashGeneratorInstance.generateSHA512(file).getOrThrow() }
                        ?: sha256?.let { it == HashGeneratorInstance.generateSHA256(file).getOrThrow() }
                        ?: sha1?.let { it == HashGeneratorInstance.generateSHA1(file).getOrThrow() }
                        ?: sizeInBytes?.let { it == file.length() }

                PreferredFileVerificationOption.Sha1 ->
                    sha1?.let { it == HashGeneratorInstance.generateSHA1(file).getOrThrow() }
                        ?: sha512?.let { it == HashGeneratorInstance.generateSHA512(file).getOrThrow() }
                        ?: sha256?.let { it == HashGeneratorInstance.generateSHA256(file).getOrThrow() }
                        ?: md5?.let { it == HashGeneratorInstance.generateMD5(file).getOrThrow() }
                        ?: sizeInBytes?.let { it == file.length() }

                PreferredFileVerificationOption.Sha256 ->
                    sha256?.let { it == HashGeneratorInstance.generateSHA256(file).getOrThrow() }
                        ?: sha512?.let { it == HashGeneratorInstance.generateSHA512(file).getOrThrow() }
                        ?: sha1?.let { it == HashGeneratorInstance.generateSHA1(file).getOrThrow() }
                        ?: md5?.let { it == HashGeneratorInstance.generateMD5(file).getOrThrow() }
                        ?: sizeInBytes?.let { it == file.length() }

                PreferredFileVerificationOption.Sha512 ->
                    sha512?.let { it == HashGeneratorInstance.generateSHA512(file).getOrThrow() }
                        ?: sha256?.let { it == HashGeneratorInstance.generateSHA256(file).getOrThrow() }
                        ?: sha1?.let { it == HashGeneratorInstance.generateSHA1(file).getOrThrow() }
                        ?: md5?.let { it == HashGeneratorInstance.generateMD5(file).getOrThrow() }
                        ?: sizeInBytes?.let { it == file.length() }
            }
        return Result.success(isValidFileIntegrity)
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}

/**
 * If the current file has valid file integrity, depending on [SyncInfo.preferredAssetFileVerification]
 * will either validate the file using all the data in [FileIntegrityInfo] or only one of them.
 * @return true or false if the file integrity is known, null otherwise
 * */
suspend fun FileIntegrityInfo.hasValidIntegrity(file: File): Result<Boolean?> {
    return try {
        val isValidFileIntegrityResult =
            SyncInfo.instance.preferredAssetFileVerification?.let {
                this.validateOnlyPreferredOption(file = file, preferredFileVerificationOption = it)
            } ?: this.validateAll(
                file = file,
            )
        return isValidFileIntegrityResult
    } catch (e: Exception) {
        e.printStackTrace()
        Result.failure(e)
    }
}
