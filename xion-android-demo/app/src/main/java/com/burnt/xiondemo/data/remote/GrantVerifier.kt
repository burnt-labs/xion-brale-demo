package com.burnt.xiondemo.data.remote

import com.burnt.xiondemo.util.Constants
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GrantVerifier @Inject constructor(
    private val httpClient: OkHttpClient
) {
    private val grantClient: OkHttpClient by lazy {
        httpClient.newBuilder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .build()
    }

    suspend fun pollForGrants(
        granter: String,
        grantee: String,
        maxAttempts: Int = 15
    ): Boolean {
        repeat(maxAttempts) { attempt ->
            try {
                val url = "${Constants.REST_URL}cosmos/authz/v1beta1/grants?granter=$granter&grantee=$grantee"
                val request = Request.Builder().url(url).get().build()
                val response = grantClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val grants = json.optJSONArray("grants")
                    if (grants != null && grants.length() > 0) {
                        return true
                    }
                }
                response.close()
            } catch (_: Exception) {
                // Network error, retry
            }
            if (attempt < maxAttempts - 1) {
                delay(2000L)
            }
        }
        return false
    }
}
