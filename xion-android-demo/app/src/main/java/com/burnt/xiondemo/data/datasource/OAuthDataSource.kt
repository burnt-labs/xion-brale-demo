package com.burnt.xiondemo.data.datasource

import com.burnt.xiondemo.data.model.OAuthTokens
import com.burnt.xiondemo.data.model.OAuthUserInfo
import com.burnt.xiondemo.data.remote.XionOAuthApi
import com.burnt.xiondemo.util.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OAuthDataSource @Inject constructor(
    private val oAuthApi: XionOAuthApi
) {

    suspend fun exchangeCode(
        code: String,
        codeVerifier: String,
        clientId: String
    ): OAuthTokens = withContext(Dispatchers.IO) {
        oAuthApi.exchangeCode(
            code = code,
            redirectUri = Constants.OAUTH_REDIRECT_URI,
            codeVerifier = codeVerifier,
            clientId = clientId
        )
    }

    suspend fun refreshToken(
        refreshToken: String,
        clientId: String
    ): OAuthTokens = withContext(Dispatchers.IO) {
        oAuthApi.refreshToken(
            refreshToken = refreshToken,
            clientId = clientId
        )
    }

    suspend fun getUserInfo(accessToken: String): OAuthUserInfo = withContext(Dispatchers.IO) {
        oAuthApi.getUserInfo("Bearer $accessToken")
    }
}
