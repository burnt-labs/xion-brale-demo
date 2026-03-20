package com.burnt.xiondemo.util;

import com.burnt.xiondemo.BuildConfig;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0018\n\u0002\u0010\t\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\u00020\u0004X\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\u00020\u0004X\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0007R\u0014\u0010\u0010\u001a\u00020\u0004X\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0007R\u000e\u0010\u0012\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0017\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001c\u001a\u00020\u0004X\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u0007R\u0014\u0010\u001e\u001a\u00020\u0004X\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0007R\u000e\u0010 \u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\"\u001a\u00020#X\u0086T\u00a2\u0006\u0002\n\u0000R\u0014\u0010$\u001a\u00020\u0004X\u0086D\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0007\u00a8\u0006&"}, d2 = {"Lcom/burnt/xiondemo/util/Constants;", "", "()V", "ADDRESS_PREFIX", "", "CHAIN_ID", "getCHAIN_ID", "()Ljava/lang/String;", "COIN_DENOM", "DECIMALS", "", "DISPLAY_DENOM", "GAS_PRICE", "KEYSTORE_ALIAS", "OAUTH_AUTHORIZATION_ENDPOINT", "getOAUTH_AUTHORIZATION_ENDPOINT", "OAUTH_CLIENT_ID", "getOAUTH_CLIENT_ID", "OAUTH_DISCOVERY_PATH", "OAUTH_REDIRECT_URI", "PREFS_NAME", "PREF_META_ACCOUNT_ADDRESS", "PREF_OAUTH_REFRESH", "PREF_OAUTH_TOKEN", "PREF_SESSION_EXPIRES_AT", "PREF_SESSION_KEY_ADDRESS", "PREF_SESSION_MNEMONIC", "PREF_TREASURY_ADDRESS", "REST_URL", "getREST_URL", "RPC_URL", "getRPC_URL", "SAMPLE_CONTRACT_ADDRESS", "SAMPLE_CONTRACT_MSG", "SESSION_GRANT_DURATION_SECONDS", "", "TREASURY_ADDRESS", "getTREASURY_ADDRESS", "app_debug"})
public final class Constants {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String RPC_URL = "https://rpc.xion-testnet-2.burnt.com:443";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String REST_URL = "https://api.xion-testnet-2.burnt.com/";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHAIN_ID = "xion-testnet-2";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String COIN_DENOM = "uxion";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String DISPLAY_DENOM = "XION";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String GAS_PRICE = "0.025";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ADDRESS_PREFIX = "xion";
    public static final int DECIMALS = 6;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TREASURY_ADDRESS = "xion1sm3qp7kdqkkqgq5sdze6fjvk02a9psqqht2s575kdw06y4prlqcqhqa0mj";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String OAUTH_CLIENT_ID = "";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String OAUTH_AUTHORIZATION_ENDPOINT = "https://auth.testnet.burnt.com/";
    public static final long SESSION_GRANT_DURATION_SECONDS = 86400L;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String OAUTH_REDIRECT_URI = "xiondemo://callback";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String OAUTH_DISCOVERY_PATH = "/.well-known/oauth-authorization-server";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SAMPLE_CONTRACT_ADDRESS = "";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String SAMPLE_CONTRACT_MSG = "{\"increment\": {}}";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEYSTORE_ALIAS = "xion_demo_session_key";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PREFS_NAME = "xion_demo_secure_prefs";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PREF_SESSION_MNEMONIC = "encrypted_session_mnemonic";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PREF_META_ACCOUNT_ADDRESS = "meta_account_address";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PREF_SESSION_KEY_ADDRESS = "session_key_address";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PREF_TREASURY_ADDRESS = "treasury_address";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PREF_SESSION_EXPIRES_AT = "session_expires_at";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PREF_OAUTH_TOKEN = "oauth_access_token";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String PREF_OAUTH_REFRESH = "oauth_refresh_token";
    @org.jetbrains.annotations.NotNull()
    public static final com.burnt.xiondemo.util.Constants INSTANCE = null;
    
    private Constants() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRPC_URL() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getREST_URL() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getCHAIN_ID() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTREASURY_ADDRESS() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getOAUTH_CLIENT_ID() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getOAUTH_AUTHORIZATION_ENDPOINT() {
        return null;
    }
}