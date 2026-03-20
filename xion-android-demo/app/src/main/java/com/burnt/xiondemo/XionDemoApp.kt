package com.burnt.xiondemo

import android.app.Application
import com.burnt.xiondemo.security.CertificateExporter
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class XionDemoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CertificateExporter.exportSystemCertsForRustTls(this)
    }
}
