package com.burnt.mob

import uniffi.mob.HttpTransport
import uniffi.mob.TransportException
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI

/**
 * HttpURLConnection-based implementation of the Rust `HttpTransport` trait.
 *
 * Uses the JVM/Android platform's native TLS stack, avoiding the
 * `UnknownIssuer` errors that occur with rustls-native-certs on Android.
 */
class NativeHttpTransport : HttpTransport {
    override fun post(url: String, body: ByteArray): ByteArray {
        val connection = try {
            URI(url).toURL().openConnection() as HttpURLConnection
        } catch (e: Exception) {
            throw TransportException.RequestFailed("Invalid URL: $url")
        }

        try {
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            connection.outputStream.use { it.write(body) }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                val errorBody = try {
                    connection.errorStream?.readBytes()?.decodeToString() ?: ""
                } catch (_: Exception) { "" }
                throw TransportException.RequestFailed(
                    "HTTP $responseCode: $errorBody"
                )
            }

            return connection.inputStream.use { it.readBytes() }
        } catch (e: TransportException) {
            throw e
        } catch (e: IOException) {
            throw TransportException.NetworkException(e.message ?: "Unknown network error")
        } catch (e: Exception) {
            throw TransportException.RequestFailed(e.message ?: "Unknown error")
        } finally {
            connection.disconnect()
        }
    }
}
