package com.alexbui.nutritrack.data

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * PasswordUtils provides secure password hashing and verification
 *
 * Uses SHA-256 with a random salt to protect stored passwords.
 * Passwords are never stored in plaintext — only salt + hash.
 *
 * Format stored in DB: "salt:hash" (both Base64 encoded)
 *
 * Supports lazy migration — if a stored password has no ":" it is
 * treated as plaintext (pre-migration) and verified directly before
 * being transparently upgraded to a hash on next save
 */
object PasswordUtils {

    /**
     * Hashes a plaintext password with a random salt
     * @return "salt:hash" string for storage in Room
     */
    fun hashPassword(password: String): String {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = sha256(salt + password.toByteArray())
        val saltB64 = Base64.getEncoder().encodeToString(salt)
        val hashB64 = Base64.getEncoder().encodeToString(hash)
        return "$saltB64:$hashB64"
    }

    /**
     * Verifies a plaintext password against a stored hash
     * @param password the plaintext input to verify
     * @param stored the "salt:hash" string from Room
     * @return true if password matches
     */
    fun verifyPassword(password: String, stored: String): Boolean {
        return try {
            val parts = stored.split(":")
            if (parts.size != 2) return false
            val salt = Base64.getDecoder().decode(parts[0])
            val expectedHash = Base64.getDecoder().decode(parts[1])
            val actualHash = sha256(salt + password.toByteArray())
            expectedHash.contentEquals(actualHash)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Checks if a stored password is plaintext (pre-migration)
     * A hashed password always contains ":" separator
     */
    fun isPlaintext(stored: String): Boolean = !stored.contains(":")

    private fun sha256(input: ByteArray): ByteArray =
        MessageDigest.getInstance("SHA-256").digest(input)
}