package com.burnt.xiondemo.data.datasource

import com.burnt.mob.NativeHttpTransport
import com.burnt.xiondemo.data.model.BalanceInfo
import com.burnt.xiondemo.data.model.TransactionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uniffi.mob.ChainConfig
import uniffi.mob.Client
import uniffi.mob.Coin
import uniffi.mob.RustSigner
import uniffi.mob.SessionMetadata
import javax.inject.Inject
import javax.inject.Singleton

interface MobDataSource {
    suspend fun createSigner(mnemonic: String): String
    suspend fun upgradeToSessionClient(
        metaAccountAddress: String,
        treasuryAddress: String,
        sessionExpiresAt: Long
    )
    suspend fun getHeight(): Long
    suspend fun getBalance(address: String, denom: String): BalanceInfo
    suspend fun send(toAddress: String, coins: List<Coin>, memo: String?): TransactionResult
    suspend fun executeContract(
        contractAddress: String,
        msg: ByteArray,
        funds: List<Coin>,
        memo: String?
    ): TransactionResult
    suspend fun getTx(txHash: String): TransactionResult
    fun getSignerAddress(): String?
    fun disconnect()
}

@Singleton
class RealMobDataSource @Inject constructor() : MobDataSource {

    private var client: Client? = null
    private var signer: RustSigner? = null
    private val cleanupScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val transport = NativeHttpTransport()

    private fun buildConfig(): ChainConfig = ChainConfig(
        chainId = "xion-testnet-2",
        rpcEndpoint = "https://rpc.xion-testnet-2.burnt.com:443",
        grpcEndpoint = null,
        addressPrefix = "xion",
        coinType = 118u,
        gasPrice = "0.025",
        feeGranter = null
    )

    override suspend fun createSigner(mnemonic: String): String = withContext(Dispatchers.IO) {
        val oldClient = client
        val oldSigner = signer
        client = null
        signer = null

        val newSigner = RustSigner.fromMnemonic(
            mnemonic = mnemonic,
            addressPrefix = "xion",
            derivationPath = "m/44'/118'/0'/0/0"
        )

        signer = newSigner

        // Deferred cleanup of old resources
        if (oldClient != null || oldSigner != null) {
            cleanupScope.launch {
                delay(2000)
                oldClient?.close()
                oldSigner?.close()
            }
        }

        newSigner.address()
    }

    override suspend fun upgradeToSessionClient(
        metaAccountAddress: String,
        treasuryAddress: String,
        sessionExpiresAt: Long
    ) = withContext(Dispatchers.IO) {
        val currentSigner = signer ?: throw IllegalStateException("Signer not initialized")

        val config = buildConfig()
        val now = System.currentTimeMillis() / 1000

        val metadata = SessionMetadata(
            granter = metaAccountAddress,
            grantee = currentSigner.address(),
            feeGranter = treasuryAddress,
            feePayer = null,
            createdAt = now.toULong(),
            expiresAt = sessionExpiresAt.toULong(),
            description = null
        )

        val oldClient = client
        client = null

        val sessionClient = Client.newWithSessionSigner(config, currentSigner, metadata, transport)
        client = sessionClient

        // Deferred cleanup of old client
        if (oldClient != null) {
            cleanupScope.launch {
                delay(2000)
                oldClient.close()
            }
        }
    }

    override suspend fun getHeight(): Long = withContext(Dispatchers.IO) {
        val c = client ?: throw IllegalStateException("Client not initialized")
        c.getHeight().toLong()
    }

    override suspend fun getBalance(address: String, denom: String): BalanceInfo = withContext(Dispatchers.IO) {
        val c = client ?: throw IllegalStateException("Client not initialized")
        val coin = c.getBalance(address, denom)
        BalanceInfo(amount = coin.amount, denom = coin.denom)
    }

    override suspend fun send(
        toAddress: String,
        coins: List<Coin>,
        memo: String?
    ): TransactionResult = withContext(Dispatchers.IO) {
        val c = client ?: throw IllegalStateException("Client not initialized")
        val response = c.send(toAddress, coins, memo)
        TransactionResult(
            txHash = response.txhash,
            success = response.code == 0u,
            gasUsed = response.gasUsed.toString(),
            gasWanted = response.gasWanted.toString(),
            height = response.height,
            rawLog = response.rawLog
        )
    }

    override suspend fun executeContract(
        contractAddress: String,
        msg: ByteArray,
        funds: List<Coin>,
        memo: String?
    ): TransactionResult = withContext(Dispatchers.IO) {
        val c = client ?: throw IllegalStateException("Client not initialized")
        val response = c.executeContract(contractAddress, msg, funds, memo, null)
        TransactionResult(
            txHash = response.txhash,
            success = response.code == 0u,
            gasUsed = response.gasUsed.toString(),
            gasWanted = response.gasWanted.toString(),
            height = response.height,
            rawLog = response.rawLog
        )
    }

    override suspend fun getTx(txHash: String): TransactionResult = withContext(Dispatchers.IO) {
        val c = client ?: throw IllegalStateException("Client not initialized")
        val response = c.getTx(txHash)
        TransactionResult(
            txHash = response.txhash,
            success = response.code == 0u,
            gasUsed = response.gasUsed.toString(),
            gasWanted = response.gasWanted.toString(),
            height = response.height,
            rawLog = response.rawLog
        )
    }

    override fun getSignerAddress(): String? = signer?.address()

    override fun disconnect() {
        val oldClient = client
        val oldSigner = signer
        client = null
        signer = null
        // Defer close so in-flight RPCs on the old runtime can finish
        cleanupScope.launch {
            delay(2000)
            oldClient?.close()
            oldSigner?.close()
        }
    }
}
