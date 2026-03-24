package com.burnt.xiondemo.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.burnt.xiondemo.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyStoreManager: KeyStoreManager
) {

    private val sharedPreferences by lazy {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        EncryptedSharedPreferences.create(
            Constants.PREFS_NAME,
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveSessionData(
        sessionMnemonic: String,
        metaAccountAddress: String,
        sessionKeyAddress: String,
        treasuryAddress: String
    ) {
        // Encrypt the session mnemonic with KeyStore-backed AES-256-GCM
        val encrypted = keyStoreManager.encrypt(sessionMnemonic)
        val encoded = Base64.encodeToString(encrypted, Base64.NO_WRAP)

        sharedPreferences.edit()
            .putString(Constants.PREF_SESSION_MNEMONIC, encoded)
            .putString(Constants.PREF_META_ACCOUNT_ADDRESS, metaAccountAddress)
            .putString(Constants.PREF_SESSION_KEY_ADDRESS, sessionKeyAddress)
            .putString(Constants.PREF_TREASURY_ADDRESS, treasuryAddress)
            .apply()
    }

    fun getSessionMnemonic(): String? {
        val encoded = sharedPreferences.getString(Constants.PREF_SESSION_MNEMONIC, null) ?: return null
        return try {
            val encrypted = Base64.decode(encoded, Base64.NO_WRAP)
            keyStoreManager.decrypt(encrypted)
        } catch (e: Exception) {
            null
        }
    }

    fun getMetaAccountAddress(): String? {
        return sharedPreferences.getString(Constants.PREF_META_ACCOUNT_ADDRESS, null)
    }

    fun getSessionKeyAddress(): String? {
        return sharedPreferences.getString(Constants.PREF_SESSION_KEY_ADDRESS, null)
    }

    fun getTreasuryAddress(): String? {
        return sharedPreferences.getString(Constants.PREF_TREASURY_ADDRESS, null)
    }

    fun saveSessionExpiry(expiresAt: Long) {
        sharedPreferences.edit()
            .putLong(Constants.PREF_SESSION_EXPIRES_AT, expiresAt)
            .apply()
    }

    fun getSessionExpiry(): Long {
        return sharedPreferences.getLong(Constants.PREF_SESSION_EXPIRES_AT, 0L)
    }

    fun saveOAuthTokens(accessToken: String, refreshToken: String?) {
        sharedPreferences.edit()
            .putString(Constants.PREF_OAUTH_TOKEN, accessToken)
            .apply {
                if (refreshToken != null) {
                    putString(Constants.PREF_OAUTH_REFRESH, refreshToken)
                }
            }
            .apply()
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}
