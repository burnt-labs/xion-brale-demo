package com.burnt.xiondemo.data.datasource

import com.burnt.xiondemo.data.model.BalanceInfo
import com.burnt.xiondemo.data.model.TransactionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import uniffi.mob.ChainConfig
import uniffi.mob.Client
import uniffi.mob.Coin
import uniffi.mob.Signer
import javax.inject.Inject
import javax.inject.Singleton

interface MobDataSource {
    suspend fun createClientWithSigner(mnemonic: String): String
    suspend fun getHeight(): Long
    suspend fun getBalance(address: String, denom: String): BalanceInfo
    suspend fun send(toAddress: String, coins: List<Coin>, granter: String?, feeGranter: String?, memo: String?): TransactionResult
    suspend fun executeContract(
        contractAddress: String,
        msg: ByteArray,
        funds: List<Coin>,
        granter: String?,
        feeGranter: String?,
        memo: String?
    ): TransactionResult
    suspend fun getTx(txHash: String): TransactionResult
    fun getSignerAddress(): String?
    fun disconnect()
}

@Singleton
class RealMobDataSource @Inject constructor() : MobDataSource {

    private var client: Client? = null
    private var signer: Signer? = null

    override suspend fun createClientWithSigner(mnemonic: String): String = withContext(Dispatchers.IO) {
        val config = ChainConfig(
            chainId = "xion-testnet-2",
            rpcEndpoint = "https://rpc.xion-testnet-2.burnt.com:443",
            grpcEndpoint = null,
            addressPrefix = "xion",
            coinType = 118u,
            gasPrice = "0.025"
        )

        val newSigner = Signer.fromMnemonic(
            mnemonic = mnemonic,
            addressPrefix = "xion",
            derivationPath = "m/44'/118'/0'/0/0"
        )
        signer = newSigner

        val newClient = try {
            Client.newWithSigner(config, newSigner)
        } catch (e: Exception) {
            // Account info refresh fails for new/unfunded session keys.
            // Fall back to bare client + attachSigner.
            val c = Client(config)
            try { c.attachSigner(newSigner) } catch (_: Exception) {}
            c
        }
        client = newClient

        newSigner.address()
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
        granter: String?,
        feeGranter: String?,
        memo: String?
    ): TransactionResult = withContext(Dispatchers.IO) {
        val c = client ?: throw IllegalStateException("Client not initialized")
        val response = c.send(toAddress, coins, granter, feeGranter, memo)
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
        granter: String?,
        feeGranter: String?,
        memo: String?
    ): TransactionResult = withContext(Dispatchers.IO) {
        val c = client ?: throw IllegalStateException("Client not initialized")
        val response = c.executeContract(contractAddress, msg, funds, granter, feeGranter, memo)
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
        client?.close()
        signer?.close()
        client = null
        signer = null
    }
}
