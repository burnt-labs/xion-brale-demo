package com.burnt.xiondemo.data.model

sealed class WalletState {
    data object Disconnected : WalletState()

    data class Connecting(val step: ConnectionStep) : WalletState()

    data class Connected(
        val metaAccountAddress: String,
        val sessionKeyAddress: String,
        val treasuryAddress: String,
        val grantsActive: Boolean,
        val sessionExpiresAt: Long
    ) : WalletState()
}

enum class ConnectionStep {
    AUTHENTICATING,
    GENERATING_SESSION_KEY,
    SETTING_UP_GRANTS,
    VERIFYING_GRANTS
}
