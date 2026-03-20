package com.burnt.xiondemo.data.remote

import com.burnt.xiondemo.data.model.OAuthTokens
import com.burnt.xiondemo.data.model.OAuthUserInfo
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface XionOAuthApi {

    @FormUrlEncoded
    @POST("oauth2/token")
    suspend fun exchangeCode(
        @Field("grant_type") grantType: String = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("code_verifier") codeVerifier: String,
        @Field("client_id") clientId: String
    ): OAuthTokens

    @FormUrlEncoded
    @POST("oauth2/token")
    suspend fun refreshToken(
        @Field("grant_type") grantType: String = "refresh_token",
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String
    ): OAuthTokens

    @GET("api/v1/me")
    suspend fun getUserInfo(
        @Header("Authorization") authorization: String
    ): OAuthUserInfo
}
