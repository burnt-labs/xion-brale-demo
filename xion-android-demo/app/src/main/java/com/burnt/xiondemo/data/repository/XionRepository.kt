package com.burnt.xiondemo.data.repository

import com.burnt.xiondemo.data.model.BalanceInfo
import com.burnt.xiondemo.data.model.TransactionResult
import com.burnt.xiondemo.data.model.WalletState
import com.burnt.xiondemo.util.Constants
import com.burnt.xiondemo.util.Result
import kotlinx.coroutines.flow.StateFlow

interface XionRepository {
    val walletState: StateFlow<WalletState>
    val transactionHistory: StateFlow<List<TransactionResult>>

    suspend fun prepareSessionKey(): Result<String>
    suspend fun completeConnection(metaAccountAddress: String): Result<String>
    suspend fun restoreSession(): Result<Boolean>

    suspend fun getBalance(): Result<BalanceInfo>
    suspend fun getSbcBalance(): Result<BalanceInfo>
    suspend fun getBlockHeight(): Result<Long>
    suspend fun send(toAddress: String, amount: String, memo: String, denom: String = Constants.COIN_DENOM): Result<TransactionResult>
    suspend fun executeContract(contractAddress: String, msg: String, funds: String?): Result<TransactionResult>
    suspend fun getTx(txHash: String): Result<TransactionResult>
    suspend fun getRecentTransactions(address: String): Result<List<TransactionResult>>

    fun appendTransaction(tx: TransactionResult)
    fun disconnect()
}
