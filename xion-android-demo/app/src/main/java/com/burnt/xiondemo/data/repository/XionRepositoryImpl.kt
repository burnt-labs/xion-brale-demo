package com.burnt.xiondemo.data.repository

import com.burnt.xiondemo.data.datasource.MobDataSource
import com.burnt.xiondemo.data.model.BalanceInfo
import com.burnt.xiondemo.data.model.ConnectionStep
import com.burnt.xiondemo.data.model.GrantExpiredException
import com.burnt.xiondemo.data.model.TransactionResult
import com.burnt.xiondemo.data.model.WalletState
import com.burnt.xiondemo.security.Bip39
import com.burnt.xiondemo.security.SecureStorage
import com.burnt.xiondemo.util.Constants
import com.burnt.xiondemo.util.Result
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import uniffi.mob.Coin
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XionRepositoryImpl @Inject constructor(
    private val mobDataSource: MobDataSource,
    private val secureStorage: SecureStorage
) : XionRepository {

    private val _walletState = MutableStateFlow<WalletState>(WalletState.Disconnected)
    override val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    private val _transactionHistory = MutableStateFlow<List<TransactionResult>>(emptyList())
    override val transactionHistory: StateFlow<List<TransactionResult>> = _transactionHistory.asStateFlow()

    // Temporarily holds the session mnemonic between prepareSessionKey and completeConnection
    private var pendingSessionMnemonic: String? = null
    private var pendingSessionKeyAddress: String? = null

    override suspend fun prepareSessionKey(): Result<String> = Result.runCatching {
        _walletState.value = WalletState.Connecting(ConnectionStep.GENERATING_SESSION_KEY)

        val sessionMnemonic = Bip39.generateMnemonic()
        val sessionKeyAddress = mobDataSource.createClientWithSigner(sessionMnemonic)

        pendingSessionMnemonic = sessionMnemonic
        pendingSessionKeyAddress = sessionKeyAddress

        sessionKeyAddress
    }.also { result ->
        if (result.isError) {
            _walletState.value = WalletState.Disconnected
            pendingSessionMnemonic = null
            pendingSessionKeyAddress = null
        }
    }

    override suspend fun completeConnection(metaAccountAddress: String): Result<String> = Result.runCatching {
        val sessionMnemonic = pendingSessionMnemonic
            ?: throw IllegalStateException("No pending session key. Call prepareSessionKey() first.")
        val sessionKeyAddress = pendingSessionKeyAddress
            ?: throw IllegalStateException("No pending session key address.")

        _walletState.value = WalletState.Connecting(ConnectionStep.SETTING_UP_GRANTS)

        secureStorage.saveSessionData(
            sessionMnemonic = sessionMnemonic,
            metaAccountAddress = metaAccountAddress,
            sessionKeyAddress = sessionKeyAddress,
            treasuryAddress = Constants.TREASURY_ADDRESS
        )

        val expiresAt = System.currentTimeMillis() / 1000 + Constants.SESSION_GRANT_DURATION_SECONDS
        secureStorage.saveSessionExpiry(expiresAt)

        _walletState.value = WalletState.Connected(
            metaAccountAddress = metaAccountAddress,
            sessionKeyAddress = sessionKeyAddress,
            treasuryAddress = Constants.TREASURY_ADDRESS,
            grantsActive = true,
            sessionExpiresAt = expiresAt
        )

        // Clear pending state
        pendingSessionMnemonic = null
        pendingSessionKeyAddress = null

        metaAccountAddress
    }.also { result ->
        if (result.isError) {
            _walletState.value = WalletState.Disconnected
            pendingSessionMnemonic = null
            pendingSessionKeyAddress = null
        }
    }

    override suspend fun restoreSession(): Result<Boolean> = Result.runCatching {
        val sessionMnemonic = secureStorage.getSessionMnemonic() ?: return@runCatching false
        val metaAccountAddress = secureStorage.getMetaAccountAddress() ?: return@runCatching false
        val sessionKeyAddress = secureStorage.getSessionKeyAddress() ?: return@runCatching false
        val treasuryAddress = secureStorage.getTreasuryAddress() ?: return@runCatching false
        val expiresAt = secureStorage.getSessionExpiry()

        val now = System.currentTimeMillis() / 1000
        if (expiresAt > 0 && now >= expiresAt) {
            secureStorage.clearAll()
            return@runCatching false
        }

        _walletState.value = WalletState.Connecting(ConnectionStep.GENERATING_SESSION_KEY)

        mobDataSource.createClientWithSigner(sessionMnemonic)

        _walletState.value = WalletState.Connected(
            metaAccountAddress = metaAccountAddress,
            sessionKeyAddress = sessionKeyAddress,
            treasuryAddress = treasuryAddress,
            grantsActive = true,
            sessionExpiresAt = expiresAt
        )

        true
    }.also { result ->
        if (result.isError) {
            _walletState.value = WalletState.Disconnected
        }
    }

    override suspend fun getBalance(): Result<BalanceInfo> = Result.runCatching {
        val state = _walletState.value as? WalletState.Connected
            ?: throw IllegalStateException("Wallet not connected")
        mobDataSource.getBalance(state.metaAccountAddress, Constants.COIN_DENOM)
    }

    override suspend fun getBlockHeight(): Result<Long> = Result.runCatching {
        mobDataSource.getHeight()
    }

    override suspend fun send(
        toAddress: String,
        amount: String,
        memo: String
    ): Result<TransactionResult> = withGrantRecovery {
        val state = _walletState.value as? WalletState.Connected
            ?: throw IllegalStateException("Wallet not connected")
        val coins = listOf(Coin(denom = Constants.COIN_DENOM, amount = amount))
        val result = mobDataSource.send(
            toAddress = toAddress,
            coins = coins,
            granter = state.metaAccountAddress,
            feeGranter = state.treasuryAddress,
            memo = memo.ifBlank { null }
        )
        val confirmed = if (result.success) {
            awaitTxConfirmation(result.txHash) ?: result
        } else {
            result
        }
        _transactionHistory.value = _transactionHistory.value + confirmed
        confirmed
    }

    override suspend fun executeContract(
        contractAddress: String,
        msg: String,
        funds: String?
    ): Result<TransactionResult> = withGrantRecovery {
        val state = _walletState.value as? WalletState.Connected
            ?: throw IllegalStateException("Wallet not connected")
        val fundsCoins = funds?.let { listOf(Coin(denom = Constants.COIN_DENOM, amount = it)) } ?: emptyList()
        val result = mobDataSource.executeContract(
            contractAddress = contractAddress,
            msg = msg.toByteArray(),
            funds = fundsCoins,
            granter = state.metaAccountAddress,
            feeGranter = state.treasuryAddress,
            memo = null
        )
        val confirmed = if (result.success) {
            awaitTxConfirmation(result.txHash) ?: result
        } else {
            result
        }
        _transactionHistory.value = _transactionHistory.value + confirmed
        confirmed
    }

    override suspend fun getTx(txHash: String): Result<TransactionResult> = Result.runCatching {
        mobDataSource.getTx(txHash)
    }

    override fun disconnect() {
        mobDataSource.disconnect()
        secureStorage.clearAll()
        _walletState.value = WalletState.Disconnected
        _transactionHistory.value = emptyList()
        pendingSessionMnemonic = null
        pendingSessionKeyAddress = null
    }

    private suspend fun awaitTxConfirmation(
        txHash: String,
        maxAttempts: Int = 10,
        delayMs: Long = 1500
    ): TransactionResult? {
        repeat(maxAttempts) {
            delay(delayMs)
            try {
                return mobDataSource.getTx(txHash)
            } catch (_: Exception) {
                // tx not yet included in a block, retry
            }
        }
        return null
    }

    private suspend fun <T> withGrantRecovery(block: suspend () -> T): Result<T> = Result.runCatching {
        try {
            block()
        } catch (e: Exception) {
            val message = e.message?.lowercase() ?: ""
            if (message.contains("authorization not found") || message.contains("fee allowance not found")) {
                val state = _walletState.value
                if (state is WalletState.Connected) {
                    _walletState.value = state.copy(grantsActive = false)
                }
                throw GrantExpiredException("Session grants have expired. Please reconnect.")
            }
            throw e
        }
    }
}
