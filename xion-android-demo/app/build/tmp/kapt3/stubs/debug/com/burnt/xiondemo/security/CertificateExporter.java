package com.burnt.xiondemo.security;

import android.content.Context;
import android.system.Os;
import android.util.Base64;
import java.io.File;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Lcom/burnt/xiondemo/security/CertificateExporter;", "", "()V", "PEM_FILE_NAME", "", "exportSystemCertsForRustTls", "", "context", "Landroid/content/Context;", "app_debug"})
public final class CertificateExporter {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PEM_FILE_NAME = "system_cacerts.pem";
    @org.jetbrains.annotations.NotNull()
    public static final com.burnt.xiondemo.security.CertificateExporter INSTANCE = null;
    
    private CertificateExporter() {
        super();
    }
    
    public final void exportSystemCertsForRustTls(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}