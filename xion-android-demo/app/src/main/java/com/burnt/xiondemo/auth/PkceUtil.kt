package com.burnt.xiondemo.auth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

object PkceUtil {

    /**
     * Generate a cryptographically random code verifier (43-128 chars, unreserved chars).
     */
    fun generateCodeVerifier(): String {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }

    /**
     * Generate code challenge from verifier using S256 method.
     * challenge = BASE64URL(SHA256(verifier))
     */
    fun generateCodeChallenge(verifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(verifier.toByteArray(Charsets.US_ASCII))
        return Base64.encodeToString(hash, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
    }
}
