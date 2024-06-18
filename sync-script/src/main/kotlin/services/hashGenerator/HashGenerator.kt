package services.hashGenerator

import java.io.File

interface HashGenerator {
    /**
     * Calculates the MD5 checksum of a text and returns it as a hexadecimal string.
     *
     *
     * @param text The text to calculate the SHA-1 checksum from.
     * @return The MD5 checksum of the file as a hexadecimal string
     * @throws SecurityException If there is a security error during the process.
     */
    suspend fun generateMD5(text: String): Result<String>

    /**
     * Calculates the MD5 checksum of a file and returns it as a hexadecimal string.
     *
     *
     * @param file The file to calculate the SHA-1 checksum from.
     * @return The MD5 checksum of the file as a hexadecimal string
     * @throws SecurityException If there is a security error during the process.
     */
    suspend fun generateMD5(file: File): Result<String>

    /**
     * Calculates the SHA-1 checksum of a file and returns it as a hexadecimal string.
     *
     * This function opens the specified file, calculates its SHA-1 hash using the
     * Java Cryptography Architecture (JCA), and returns the hash as a hexadecimal string.
     * If the file doesn't exist, the function returns null.
     *
     * @param file The file to calculate the SHA-1 checksum from.
     * @return The SHA-1 checksum of the file as a hexadecimal string
     * @throws SecurityException If there is a security error during the process.
     */
    suspend fun generateSHA1(file: File): Result<String>

    /**
     * Calculates the SHA-256 checksum of a file and returns it as a hexadecimal string.
     *
     * This function opens the specified file, calculates its SHA-256 hash using the
     * Java Cryptography Architecture (JCA), and returns the hash as a hexadecimal string.
     * If the file doesn't exist, the function returns null.
     *
     * @param file The file to calculate the SHA-256 checksum from.
     * @return The SHA-256 checksum of the file as a hexadecimal string
     * @throws SecurityException If there is a security error during the process.
     *
     */
    suspend fun generateSHA256(file: File): Result<String>

    /**
     * Calculates the SHA-512 checksum of a file and returns it as a hexadecimal string.
     *
     * This function opens the specified file, calculates its SHA-512 hash using the
     * Java Cryptography Architecture (JCA), and returns the hash as a hexadecimal string.
     * If the file doesn't exist, the function returns null.
     *
     * @param file The file to calculate the SHA-512 checksum from.
     * @return The SHA-512 checksum of the file as a hexadecimal string
     * @throws SecurityException If there is a security error during the process.
     *
     * @author https://www.baeldung.com/sha-256-hashing-java
     */
    suspend fun generateSHA512(file: File): Result<String>
}
