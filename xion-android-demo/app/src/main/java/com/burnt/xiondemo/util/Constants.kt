package com.burnt.xiondemo.util

import com.burnt.xiondemo.BuildConfig

object Constants {
    // Chain configuration
    val RPC_URL = BuildConfig.XION_RPC_URL
    val REST_URL = BuildConfig.XION_REST_URL
    val CHAIN_ID = BuildConfig.XION_CHAIN_ID
    const val COIN_DENOM = "uxion"
    const val DISPLAY_DENOM = "XION"
    const val GAS_PRICE = "0.025"
    const val ADDRESS_PREFIX = "xion"
    const val DECIMALS = 6

    // Session / Abstraxion
    val TREASURY_ADDRESS = BuildConfig.XION_TREASURY_ADDRESS
    val OAUTH_CLIENT_ID = BuildConfig.XION_OAUTH_CLIENT_ID
    val OAUTH_AUTHORIZATION_ENDPOINT = BuildConfig.XION_OAUTH_AUTHORIZATION_ENDPOINT
    const val SESSION_GRANT_DURATION_SECONDS = 86400L

    // OAuth2
    const val OAUTH_REDIRECT_URI = "xiondemo://callback"
    const val OAUTH_DISCOVERY_PATH = "/.well-known/oauth-authorization-server"

    // Sample contract for demo
    const val SAMPLE_CONTRACT_ADDRESS = ""
    const val SAMPLE_CONTRACT_MSG = """{"increment": {}}"""

    // Keystore
    const val KEYSTORE_ALIAS = "xion_demo_session_key"
    const val PREFS_NAME = "xion_demo_secure_prefs"

    // Session storage pref keys
    const val PREF_SESSION_MNEMONIC = "encrypted_session_mnemonic"
    const val PREF_META_ACCOUNT_ADDRESS = "meta_account_address"
    const val PREF_SESSION_KEY_ADDRESS = "session_key_address"
    const val PREF_TREASURY_ADDRESS = "treasury_address"
    const val PREF_SESSION_EXPIRES_AT = "session_expires_at"
    const val PREF_OAUTH_TOKEN = "oauth_access_token"
    const val PREF_OAUTH_REFRESH = "oauth_refresh_token"
}
