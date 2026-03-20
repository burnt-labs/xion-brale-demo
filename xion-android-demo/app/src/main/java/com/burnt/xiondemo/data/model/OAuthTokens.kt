package com.burnt.xiondemo.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OAuthTokens(
    @SerialName("access_token") val accessToken: String,
    @SerialName("token_type") val tokenType: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("refresh_token") val refreshToken: String? = null
)

@Serializable
data class OAuthUserInfo(
    val address: String,
    @SerialName("chain_id") val chainId: String? = null
)
