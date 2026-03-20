package com.burnt.xiondemo.auth;

import android.util.Base64;
import java.security.MessageDigest;
import java.security.SecureRandom;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004J\u0006\u0010\u0006\u001a\u00020\u0004\u00a8\u0006\u0007"}, d2 = {"Lcom/burnt/xiondemo/auth/PkceUtil;", "", "()V", "generateCodeChallenge", "", "verifier", "generateCodeVerifier", "app_debug"})
public final class PkceUtil {
    @org.jetbrains.annotations.NotNull()
    public static final com.burnt.xiondemo.auth.PkceUtil INSTANCE = null;
    
    private PkceUtil() {
        super();
    }
    
    /**
     * Generate a cryptographically random code verifier (43-128 chars, unreserved chars).
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String generateCodeVerifier() {
        return null;
    }
    
    /**
     * Generate code challenge from verifier using S256 method.
     * challenge = BASE64URL(SHA256(verifier))
     */
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String generateCodeChallenge(@org.jetbrains.annotations.NotNull()
    java.lang.String verifier) {
        return null;
    }
}