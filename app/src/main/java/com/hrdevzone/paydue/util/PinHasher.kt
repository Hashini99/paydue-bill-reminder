package com.hrdevzone.paydue.util

import java.security.MessageDigest
import java.security.SecureRandom

object PinHasher {

    fun randomSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hash(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest((salt + pin).toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun matches(pin: String, salt: String, expectedHash: String): Boolean {
        return hash(pin, salt) == expectedHash
    }
}
