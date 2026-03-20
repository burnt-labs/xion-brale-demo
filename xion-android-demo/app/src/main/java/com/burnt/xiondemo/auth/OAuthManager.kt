package com.burnt.xiondemo.auth

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.burnt.xiondemo.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OAuthManager @Inject constructor(
    @ApplicationContext private val context: android.content.Context
) {
    private val _callbackFlow = MutableSharedFlow<AuthCallback>(extraBufferCapacity = 1)
    val callbackFlow: SharedFlow<AuthCallback> = _callbackFlow.asSharedFlow()

    data class AuthCallback(
        val metaAccountAddress: String
    )

    fun launchAuth(activity: android.app.Activity, granteeAddress: String) {
        val authUri = Uri.parse(Constants.OAUTH_AUTHORIZATION_ENDPOINT).buildUpon()
            .appendQueryParameter("treasury", Constants.TREASURY_ADDRESS)
            .appendQueryParameter("grantee", granteeAddress)
            .appendQueryParameter("redirect_uri", Constants.OAUTH_REDIRECT_URI)
            .build()

        val customTabsIntent = CustomTabsIntent.Builder()
            .setShowTitle(true)
            .build()

        customTabsIntent.launchUrl(activity, authUri)
    }

    fun handleCallback(uri: Uri) {
        if (uri.scheme != "xiondemo" || uri.host != "callback") return
        val address = uri.getQueryParameter("granter") ?: return

        _callbackFlow.tryEmit(AuthCallback(metaAccountAddress = address))
    }
}
