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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.json.JSONObject
import uniffi.mob.Coin
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XionRepositoryImpl @Inject constructor(
    private val mobDataSource: MobDataSource,
    private val secureStorage: SecureStorage,
    private val httpClient: OkHttpClient
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
        val sessionKeyAddress = mobDataSource.createSigner(sessionMnemonic)

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

        val expiresAt = System.currentTimeMillis() / 1000 + Constants.SESSION_GRANT_DURATION_SECONDS

        // Upgrade client to session mode — granter/feeGranter are now handled internally
        mobDataSource.upgradeToSessionClient(
            metaAccountAddress = metaAccountAddress,
            treasuryAddress = Constants.TREASURY_ADDRESS,
            sessionExpiresAt = expiresAt
        )

        secureStorage.saveSessionData(
            sessionMnemonic = sessionMnemonic,
            metaAccountAddress = metaAccountAddress,
            sessionKeyAddress = sessionKeyAddress,
            treasuryAddress = Constants.TREASURY_ADDRESS
        )
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

        mobDataSource.createSigner(sessionMnemonic)

        // Upgrade to session client so granter/feeGranter are handled internally
        mobDataSource.upgradeToSessionClient(
            metaAccountAddress = metaAccountAddress,
            treasuryAddress = treasuryAddress,
            sessionExpiresAt = expiresAt
        )

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

    override suspend fun getSbcBalance(): Result<BalanceInfo> = Result.runCatching {
        val state = _walletState.value as? WalletState.Connected
            ?: throw IllegalStateException("Wallet not connected")
        mobDataSource.getBalance(state.metaAccountAddress, Constants.BRALE_SBC_ON_CHAIN_DENOM)
    }

    override suspend fun getBlockHeight(): Result<Long> = Result.runCatching {
        mobDataSource.getHeight()
    }

    override suspend fun send(
        toAddress: String,
        amount: String,
        memo: String,
        denom: String
    ): Result<TransactionResult> = withGrantRecovery {
        _walletState.value as? WalletState.Connected
            ?: throw IllegalStateException("Wallet not connected")
        val coins = listOf(Coin(denom = denom, amount = amount))
        val result = mobDataSource.send(
            toAddress = toAddress,
            coins = coins,
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
        _walletState.value as? WalletState.Connected
            ?: throw IllegalStateException("Wallet not connected")
        val fundsCoins = funds?.let { listOf(Coin(denom = Constants.COIN_DENOM, amount = it)) } ?: emptyList()
        val result = mobDataSource.executeContract(
            contractAddress = contractAddress,
            msg = msg.toByteArray(),
            funds = fundsCoins,
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

    override suspend fun getVaultBalance(): Result<BalanceInfo> = Result.runCatching {
        val state = _walletState.value as? WalletState.Connected
            ?: throw IllegalStateException("Wallet not connected")
        val queryMsg = """{"balance":{"address":"${state.metaAccountAddress}"}}"""
        val responseBytes = mobDataSource.queryContractSmart(
            Constants.VAULT_CONTRACT_ADDRESS,
            queryMsg.toByteArray()
        )
        val responseStr = String(responseBytes)
        val json = org.json.JSONObject(responseStr)
        val coins = json.getJSONArray("coins")
        if (coins.length() == 0) {
            BalanceInfo(amount = "0", denom = Constants.COIN_DENOM)
        } else {
            val firstCoin = coins.getJSONObject(0)
            BalanceInfo(amount = firstCoin.getString("amount"), denom = firstCoin.getString("denom"))
        }
    }

    override suspend fun vaultDeposit(amount: String, denom: String): Result<TransactionResult> = withGrantRecovery {
        _walletState.value as? WalletState.Connected
            ?: throw IllegalStateException("Wallet not connected")
        val msg = """{"deposit":{}}"""
        val fundsCoins = listOf(Coin(denom = denom, amount = amount))
        val result = mobDataSource.executeContract(
            contractAddress = Constants.VAULT_CONTRACT_ADDRESS,
            msg = msg.toByteArray(),
            funds = fundsCoins,
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

    override suspend fun vaultWithdraw(amount: String, denom: String): Result<TransactionResult> = withGrantRecovery {
        _walletState.value as? WalletState.Connected
            ?: throw IllegalStateException("Wallet not connected")
        val msg = """{"withdraw":{"coins":[{"denom":"$denom","amount":"$amount"}]}}"""
        val result = mobDataSource.executeContract(
            contractAddress = Constants.VAULT_CONTRACT_ADDRESS,
            msg = msg.toByteArray(),
            funds = emptyList(),
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

    override suspend fun vaultWithdrawAll(): Result<TransactionResult> = withGrantRecovery {
        _walletState.value as? WalletState.Connected
            ?: throw IllegalStateException("Wallet not connected")
        val msg = """{"withdraw_all":{}}"""
        val result = mobDataSource.executeContract(
            contractAddress = Constants.VAULT_CONTRACT_ADDRESS,
            msg = msg.toByteArray(),
            funds = emptyList(),
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

    override suspend fun getRecentTransactions(address: String): Result<List<TransactionResult>> = Result.runCatching {
        withContext(Dispatchers.IO) {
            val sent = fetchTxsByQuery(address, "transfer.sender%3D%27${address}%27")
            val received = fetchTxsByQuery(address, "transfer.recipient%3D%27${address}%27")
            val merged = (sent + received)
                .distinctBy { it.txHash }
                .sortedByDescending { it.height }
                .take(3)
            merged
        }
    }

    private fun fetchTxsByQuery(userAddress: String, queryValue: String): List<TransactionResult> {
        val url = "${Constants.REST_URL}cosmos/tx/v1beta1/txs" +
            "?query=${queryValue}" +
            "&order_by=ORDER_BY_DESC" +
            "&pagination.limit=3"
        val request = okhttp3.Request.Builder().url(url).get().build()
        val response = httpClient.newCall(request).execute()
        return response.use { resp ->
            if (!resp.isSuccessful) return@use emptyList()
            val body = resp.body?.string() ?: return@use emptyList()
            val json = JSONObject(body)
            val txResponses = json.optJSONArray("tx_responses") ?: return@use emptyList()
            val txs = json.optJSONArray("txs")
            (0 until txResponses.length()).map { i ->
                val tx = txResponses.getJSONObject(i)
                val txBody = txs?.optJSONObject(i)

                // Extract fee from tx body
                val feeAmount = txBody?.optJSONObject("auth_info")
                    ?.optJSONObject("fee")
                    ?.optJSONArray("amount")
                    ?.optJSONObject(0)
                    ?.optString("amount", "0") ?: "0"

                // Extract tx type from messages
                val messages = txBody?.optJSONObject("body")
                    ?.optJSONArray("messages")
                val txType = if (messages != null && messages.length() > 0) {
                    val typeUrl = messages.getJSONObject(0).optString("@type", "")
                    val shortType = typeUrl.substringAfterLast(".")
                    if (messages.length() > 1) "$shortType +${messages.length() - 1}" else shortType
                } else ""

                // Extract amount, denom, and recipient from transfer events
                var transferAmount = ""
                var transferDenom = ""
                var transferRecipient = ""
                var transferSender = ""
                val events = tx.optJSONArray("events")
                if (events != null) {
                    for (e in 0 until events.length()) {
                        val event = events.getJSONObject(e)
                        if (event.optString("type") == "transfer") {
                            val attrs = event.optJSONArray("attributes") ?: continue
                            val attrMap = mutableMapOf<String, String>()
                            for (a in 0 until attrs.length()) {
                                val attr = attrs.getJSONObject(a)
                                attrMap[attr.optString("key")] = attr.optString("value")
                            }
                            val sender = attrMap["sender"] ?: ""
                            val recipient = attrMap["recipient"] ?: ""
                            val rawAmount = attrMap["amount"] ?: ""
                            if (sender == userAddress || recipient == userAddress) {
                                // Parse "1200000factory/xion.../sbc" or "5000000uxion"
                                // Extract leading digits as amount, rest as denom
                                val match = Regex("^(\\d+)(.*)$").find(rawAmount)
                                transferAmount = match?.groupValues?.get(1) ?: rawAmount.replace(Regex("[^0-9]"), "")
                                transferDenom = match?.groupValues?.get(2) ?: ""
                                transferRecipient = recipient
                                transferSender = sender
                                break
                            }
                        }
                    }
                }

                // Determine display-friendly tx type
                val displayTxType = when {
                    transferRecipient.startsWith("xion1amma") || transferRecipient.startsWith("xion1yymx") -> "Cash Out"
                    transferSender.startsWith("xion1amma") || transferSender.startsWith("xion1yymx") -> "Buy SBC"
                    transferDenom.contains("sbc", ignoreCase = true) -> "SBC Transfer"
                    txType == "MsgExec" && transferAmount.isNotEmpty() -> "Send"
                    else -> txType
                }

                TransactionResult(
                    txHash = tx.optString("txhash", ""),
                    success = tx.optInt("code", -1) == 0,
                    gasUsed = tx.optString("gas_used", "0"),
                    gasWanted = tx.optString("gas_wanted", "0"),
                    height = tx.optLong("height", 0),
                    rawLog = tx.optString("raw_log", ""),
                    timestamp = tx.optString("timestamp", ""),
                    fee = feeAmount,
                    txType = displayTxType,
                    amount = transferAmount,
                    amountDenom = if (transferDenom.contains("sbc", ignoreCase = true)) "SBC" else "XION",
                    recipient = transferRecipient
                )
            }
        }
    }

    override fun appendTransaction(tx: TransactionResult) {
        _transactionHistory.value = _transactionHistory.value + tx
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
