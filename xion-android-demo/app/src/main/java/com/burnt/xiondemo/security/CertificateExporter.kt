package com.burnt.xiondemo.security

import android.content.Context
import android.system.Os
import android.util.Base64
import java.io.File
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

object CertificateExporter {
    private const val PEM_FILE_NAME = "system_cacerts.pem"

    fun exportSystemCertsForRustTls(context: Context) {
        val pemFile = File(context.filesDir, PEM_FILE_NAME)

        if (!pemFile.exists()) {
            val trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            )
            trustManagerFactory.init(null as java.security.KeyStore?)

            val x509TrustManager = trustManagerFactory.trustManagers
                .filterIsInstance<X509TrustManager>()
                .first()

            pemFile.bufferedWriter().use { writer ->
                for (cert in x509TrustManager.acceptedIssuers) {
                    writer.write("-----BEGIN CERTIFICATE-----\n")
                    writer.write(Base64.encodeToString(cert.encoded, Base64.NO_WRAP))
                    writer.write("\n-----END CERTIFICATE-----\n")
                }
            }
        }

        Os.setenv("SSL_CERT_FILE", pemFile.absolutePath, true)
    }
}
