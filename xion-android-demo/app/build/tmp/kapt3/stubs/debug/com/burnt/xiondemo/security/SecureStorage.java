package com.burnt.xiondemo.security;

import android.content.Context;
import android.util.Base64;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;
import com.burnt.xiondemo.util.Constants;
import dagger.hilt.android.qualifiers.ApplicationContext;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0002\b\u000e\b\u0007\u0018\u00002\u00020\u0001B\u0019\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\r\u001a\u00020\u000eJ\b\u0010\u000f\u001a\u0004\u0018\u00010\u0010J\u0006\u0010\u0011\u001a\u00020\u0012J\b\u0010\u0013\u001a\u0004\u0018\u00010\u0010J\b\u0010\u0014\u001a\u0004\u0018\u00010\u0010J\b\u0010\u0015\u001a\u0004\u0018\u00010\u0010J\u0018\u0010\u0016\u001a\u00020\u000e2\u0006\u0010\u0017\u001a\u00020\u00102\b\u0010\u0018\u001a\u0004\u0018\u00010\u0010J&\u0010\u0019\u001a\u00020\u000e2\u0006\u0010\u001a\u001a\u00020\u00102\u0006\u0010\u001b\u001a\u00020\u00102\u0006\u0010\u001c\u001a\u00020\u00102\u0006\u0010\u001d\u001a\u00020\u0010J\u000e\u0010\u001e\u001a\u00020\u000e2\u0006\u0010\u001f\u001a\u00020\u0012R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0007\u001a\u00020\b8BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u000b\u0010\f\u001a\u0004\b\t\u0010\n\u00a8\u0006 "}, d2 = {"Lcom/burnt/xiondemo/security/SecureStorage;", "", "context", "Landroid/content/Context;", "keyStoreManager", "Lcom/burnt/xiondemo/security/KeyStoreManager;", "(Landroid/content/Context;Lcom/burnt/xiondemo/security/KeyStoreManager;)V", "sharedPreferences", "Landroid/content/SharedPreferences;", "getSharedPreferences", "()Landroid/content/SharedPreferences;", "sharedPreferences$delegate", "Lkotlin/Lazy;", "clearAll", "", "getMetaAccountAddress", "", "getSessionExpiry", "", "getSessionKeyAddress", "getSessionMnemonic", "getTreasuryAddress", "saveOAuthTokens", "accessToken", "refreshToken", "saveSessionData", "sessionMnemonic", "metaAccountAddress", "sessionKeyAddress", "treasuryAddress", "saveSessionExpiry", "expiresAt", "app_debug"})
public final class SecureStorage {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.burnt.xiondemo.security.KeyStoreManager keyStoreManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy sharedPreferences$delegate = null;
    
    @javax.inject.Inject()
    public SecureStorage(@dagger.hilt.android.qualifiers.ApplicationContext()
    @org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.burnt.xiondemo.security.KeyStoreManager keyStoreManager) {
        super();
    }
    
    private final android.content.SharedPreferences getSharedPreferences() {
        return null;
    }
    
    public final void saveSessionData(@org.jetbrains.annotations.NotNull()
    java.lang.String sessionMnemonic, @org.jetbrains.annotations.NotNull()
    java.lang.String metaAccountAddress, @org.jetbrains.annotations.NotNull()
    java.lang.String sessionKeyAddress, @org.jetbrains.annotations.NotNull()
    java.lang.String treasuryAddress) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSessionMnemonic() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getMetaAccountAddress() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSessionKeyAddress() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getTreasuryAddress() {
        return null;
    }
    
    public final void saveSessionExpiry(long expiresAt) {
    }
    
    public final long getSessionExpiry() {
        return 0L;
    }
    
    public final void saveOAuthTokens(@org.jetbrains.annotations.NotNull()
    java.lang.String accessToken, @org.jetbrains.annotations.Nullable()
    java.lang.String refreshToken) {
    }
    
    public final void clearAll() {
    }
}