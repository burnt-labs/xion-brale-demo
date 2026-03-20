package com.burnt.xiondemo.security;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import com.burnt.xiondemo.util.Constants;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0012\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u0000 \u000b2\u00020\u0001:\u0001\u000bB\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0007\u001a\u00020\u00062\u0006\u0010\b\u001a\u00020\u0004J\b\u0010\t\u001a\u00020\nH\u0002\u00a8\u0006\f"}, d2 = {"Lcom/burnt/xiondemo/security/KeyStoreManager;", "", "()V", "decrypt", "", "encryptedData", "", "encrypt", "data", "getOrCreateKey", "Ljavax/crypto/SecretKey;", "Companion", "app_debug"})
public final class KeyStoreManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;
    @org.jetbrains.annotations.NotNull()
    public static final com.burnt.xiondemo.security.KeyStoreManager.Companion Companion = null;
    
    @javax.inject.Inject()
    public KeyStoreManager() {
        super();
    }
    
    private final javax.crypto.SecretKey getOrCreateKey() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final byte[] encrypt(@org.jetbrains.annotations.NotNull()
    java.lang.String data) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String decrypt(@org.jetbrains.annotations.NotNull()
    byte[] encryptedData) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\b"}, d2 = {"Lcom/burnt/xiondemo/security/KeyStoreManager$Companion;", "", "()V", "ANDROID_KEYSTORE", "", "GCM_IV_LENGTH", "", "GCM_TAG_LENGTH", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}