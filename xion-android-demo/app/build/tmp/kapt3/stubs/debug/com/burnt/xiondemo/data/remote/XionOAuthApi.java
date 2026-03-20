package com.burnt.xiondemo.data.remote;

import com.burnt.xiondemo.data.model.OAuthTokens;
import com.burnt.xiondemo.data.model.OAuthUserInfo;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0005\bf\u0018\u00002\u00020\u0001J@\u0010\u0002\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u0006\u001a\u00020\u00052\b\b\u0001\u0010\u0007\u001a\u00020\u00052\b\b\u0001\u0010\b\u001a\u00020\u00052\b\b\u0001\u0010\t\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\nJ\u0018\u0010\u000b\u001a\u00020\f2\b\b\u0001\u0010\r\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u000eJ,\u0010\u000f\u001a\u00020\u00032\b\b\u0003\u0010\u0004\u001a\u00020\u00052\b\b\u0001\u0010\u000f\u001a\u00020\u00052\b\b\u0001\u0010\t\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0010\u00a8\u0006\u0011"}, d2 = {"Lcom/burnt/xiondemo/data/remote/XionOAuthApi;", "", "exchangeCode", "Lcom/burnt/xiondemo/data/model/OAuthTokens;", "grantType", "", "code", "redirectUri", "codeVerifier", "clientId", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getUserInfo", "Lcom/burnt/xiondemo/data/model/OAuthUserInfo;", "authorization", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refreshToken", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface XionOAuthApi {
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "oauth2/token")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object exchangeCode(@retrofit2.http.Field(value = "grant_type")
    @org.jetbrains.annotations.NotNull()
    java.lang.String grantType, @retrofit2.http.Field(value = "code")
    @org.jetbrains.annotations.NotNull()
    java.lang.String code, @retrofit2.http.Field(value = "redirect_uri")
    @org.jetbrains.annotations.NotNull()
    java.lang.String redirectUri, @retrofit2.http.Field(value = "code_verifier")
    @org.jetbrains.annotations.NotNull()
    java.lang.String codeVerifier, @retrofit2.http.Field(value = "client_id")
    @org.jetbrains.annotations.NotNull()
    java.lang.String clientId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.OAuthTokens> $completion);
    
    @retrofit2.http.FormUrlEncoded()
    @retrofit2.http.POST(value = "oauth2/token")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object refreshToken(@retrofit2.http.Field(value = "grant_type")
    @org.jetbrains.annotations.NotNull()
    java.lang.String grantType, @retrofit2.http.Field(value = "refresh_token")
    @org.jetbrains.annotations.NotNull()
    java.lang.String refreshToken, @retrofit2.http.Field(value = "client_id")
    @org.jetbrains.annotations.NotNull()
    java.lang.String clientId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.OAuthTokens> $completion);
    
    @retrofit2.http.GET(value = "api/v1/me")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getUserInfo(@retrofit2.http.Header(value = "Authorization")
    @org.jetbrains.annotations.NotNull()
    java.lang.String authorization, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.OAuthUserInfo> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}