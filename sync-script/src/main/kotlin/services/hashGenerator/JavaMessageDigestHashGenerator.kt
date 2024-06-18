package services.hashGenerator

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

class JavaMessageDigestHashGenerator : HashGenerator {
    override suspend fun generateMD5(text: String): Result<String> {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val hashBytes = digest.digest(text.toByteArray())
            return Result.success(hashBytes.joinToString("") { "%02x".format(it) })
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun generateMD5(file: File): Result<String> {
        return try {
            val buffer = ByteArray(1024)
            val md = MessageDigest.getInstance("MD5")

            file.inputStream().use { fileInputStream ->
                var bytesRead: Int
                while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                    md.update(buffer, 0, bytesRead)
                }
            }

            return Result.success(md.digest().joinToString("") { "%02x".format(it) })
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * @author https://www.baeldung.com/sha-256-hashing-java
     * */
    override suspend fun generateSHA1(file: File): Result<String> {
        return try {
            file.inputStream().use { inputStream ->
                val digest = MessageDigest.getInstance("SHA-1")
                val buffer = ByteArray(1024)
                var read: Int
                do {
                    read = inputStream.read(buffer)
                    if (read > 0) {
                        digest.update(buffer, 0, read)
                    }
                } while (read != -1)
                val hash = digest.digest()
                val formatter = StringBuilder(hash.size * 2)
                for (b in hash) {
                    val hex = String.format("%02x", b)
                    formatter.append(hex)
                }
                Result.success(formatter.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * @author https://www.baeldung.com/sha-256-hashing-java
     * */
    override suspend fun generateSHA256(file: File): Result<String> {
        return try {
            file.inputStream().use { inputStream ->
                val digest = MessageDigest.getInstance("SHA-256")
                val buffer = ByteArray(1024)
                var read: Int
                do {
                    read = inputStream.read(buffer)
                    if (read > 0) {
                        digest.update(buffer, 0, read)
                    }
                } while (read != -1)
                val hash = digest.digest()
                val formatter = StringBuilder(hash.size * 2)
                for (b in hash) {
                    val hex = String.format("%02x", b)
                    formatter.append(hex)
                }
                Result.success(formatter.toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * @author https://www.baeldung.com/sha-256-hashing-java
     * */
    override suspend fun generateSHA512(file: File): Result<String> {
        return try {
            withContext(Dispatchers.IO) {
                val digest = MessageDigest.getInstance("SHA-512")
                file.inputStream().use { inputStream ->
                    val byteBuffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(byteBuffer).also { bytesRead = it } != -1) {
                        digest.update(byteBuffer, 0, bytesRead)
                    }
                }
                val hashedBytes = digest.digest()
                Result.success(hashedBytes.fold("") { acc, byte -> acc + "%02x".format(byte) })
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
