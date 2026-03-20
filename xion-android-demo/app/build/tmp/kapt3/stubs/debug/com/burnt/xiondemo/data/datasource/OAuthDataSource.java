package com.burnt.xiondemo.data.datasource;

import com.burnt.xiondemo.data.model.OAuthTokens;
import com.burnt.xiondemo.data.model.OAuthUserInfo;
import com.burnt.xiondemo.data.remote.XionOAuthApi;
import com.burnt.xiondemo.util.Constants;
import kotlinx.coroutines.Dispatchers;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J&\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u000bJ\u0016\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u000fJ\u001e\u0010\u0010\u001a\u00020\u00062\u0006\u0010\u0010\u001a\u00020\b2\u0006\u0010\n\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u0010\u0011R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/burnt/xiondemo/data/datasource/OAuthDataSource;", "", "oAuthApi", "Lcom/burnt/xiondemo/data/remote/XionOAuthApi;", "(Lcom/burnt/xiondemo/data/remote/XionOAuthApi;)V", "exchangeCode", "Lcom/burnt/xiondemo/data/model/OAuthTokens;", "code", "", "codeVerifier", "clientId", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getUserInfo", "Lcom/burnt/xiondemo/data/model/OAuthUserInfo;", "accessToken", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refreshToken", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class OAuthDataSource {
    @org.jetbrains.annotations.NotNull()
    private final com.burnt.xiondemo.data.remote.XionOAuthApi oAuthApi = null;
    
    @javax.inject.Inject()
    public OAuthDataSource(@org.jetbrains.annotations.NotNull()
    com.burnt.xiondemo.data.remote.XionOAuthApi oAuthApi) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object exchangeCode(@org.jetbrains.annotations.NotNull()
    java.lang.String code, @org.jetbrains.annotations.NotNull()
    java.lang.String codeVerifier, @org.jetbrains.annotations.NotNull()
    java.lang.String clientId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.OAuthTokens> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object refreshToken(@org.jetbrains.annotations.NotNull()
    java.lang.String refreshToken, @org.jetbrains.annotations.NotNull()
    java.lang.String clientId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.OAuthTokens> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getUserInfo(@org.jetbrains.annotations.NotNull()
    java.lang.String accessToken, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.OAuthUserInfo> $completion) {
        return null;
    }
}