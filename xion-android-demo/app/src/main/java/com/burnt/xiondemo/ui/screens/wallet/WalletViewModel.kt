package com.burnt.xiondemo.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnt.xiondemo.data.model.TransactionResult
import com.burnt.xiondemo.data.model.WalletState
import com.burnt.xiondemo.data.repository.XionRepository
import com.burnt.xiondemo.security.SecureStorage
import com.burnt.xiondemo.util.Constants
import com.burnt.xiondemo.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletUiState(
    val address: String? = null,
    val sessionAddress: String? = null,
    val connectionType: String = "Unknown",
    val grantsActive: Boolean = true,
    val balance: String? = null,
    val sbcBalance: String? = null,
    val vaultBalance: String? = null,
    val isBalanceLoading: Boolean = false,
    val blockHeight: Long? = null,
    val chainId: String = Constants.CHAIN_ID,
    val error: String? = null,
    val sessionExpiryWarning: Boolean = false,
    val isDisconnected: Boolean = false,
    val transactions: List<TransactionResult> = emptyList(),
    val bankLinked: Boolean = false
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: XionRepository,
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.walletState.collect { state ->
                when (state) {
                    is WalletState.Connected -> {
                        val previousAddress = _uiState.value.address
                        _uiState.update {
                            it.copy(
                                address = state.metaAccountAddress,
                                sessionAddress = state.sessionKeyAddress,
                                connectionType = "Meta Account",
                                grantsActive = state.grantsActive
                            )
                        }
                        if (previousAddress == null) {
                            loadRecentTransactions()
                        }
                    }
                    is WalletState.Disconnected -> {
                        _uiState.update { it.copy(isDisconnected = true) }
                    }
                    is WalletState.Connecting -> {}
                }
            }
        }

        viewModelScope.launch {
            repository.transactionHistory.collect { inMemoryTxs ->
                if (inMemoryTxs.isNotEmpty()) {
                    // A new tx was just sent — refresh from chain to get full details
                    loadRecentTransactions()
                }
            }
        }

        // Periodic session expiry check (every 60s)
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                checkSessionExpiry()
            }
        }

        refresh()
    }

    fun refresh() {
        loadBalance()
        loadSbcBalance()
        loadVaultBalance()
        loadBlockHeight()
        loadRecentTransactions()
        checkBankLinked()
    }

    private fun checkBankLinked() {
        val bankAddressId = secureStorage.getString(Constants.PREF_BRALE_BANK_ADDRESS_ID)
        _uiState.update { it.copy(bankLinked = !bankAddressId.isNullOrBlank()) }
    }

    private fun loadBalance() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBalanceLoading = true) }
            when (val result = repository.getBalance()) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(balance = result.data.amount, isBalanceLoading = false)
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(isBalanceLoading = false, error = result.message)
                    }
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun loadSbcBalance() {
        viewModelScope.launch {
            when (val result = repository.getSbcBalance()) {
                is Result.Success -> {
                    _uiState.update { it.copy(sbcBalance = result.data.amount) }
                }
                is Result.Error -> {} // Non-critical
                is Result.Loading -> {}
            }
        }
    }

    private fun loadVaultBalance() {
        viewModelScope.launch {
            when (val result = repository.getVaultBalance()) {
                is Result.Success -> {
                    _uiState.update { it.copy(vaultBalance = result.data.amount) }
                }
                is Result.Error -> {} // Non-critical
                is Result.Loading -> {}
            }
        }
    }

    private fun loadBlockHeight() {
        viewModelScope.launch {
            when (val result = repository.getBlockHeight()) {
                is Result.Success -> {
                    _uiState.update { it.copy(blockHeight = result.data) }
                }
                is Result.Error -> {}
                is Result.Loading -> {}
            }
        }
    }

    private fun loadRecentTransactions() {
        viewModelScope.launch {
            val address = _uiState.value.address ?: return@launch
            val onChainTxs = when (val result = repository.getRecentTransactions(address)) {
                is Result.Success -> result.data
                else -> emptyList()
            }
            // Merge on-chain transactions with in-memory Brale transactions (Buy SBC, Cash Out)
            val inMemoryTxs = repository.transactionHistory.value
            val seen = mutableMapOf<String, TransactionResult>()
            // On-chain first (has enriched data)
            for (tx in onChainTxs) { seen[tx.txHash] = tx }
            // In-memory adds new entries (Brale txs have unique IDs)
            for (tx in inMemoryTxs) { if (tx.txHash !in seen) seen[tx.txHash] = tx }
            val merged = seen.values.sortedByDescending {
                if (it.height > 0) it.height else Long.MAX_VALUE // Brale entries (height=0) sort to top
            }
            _uiState.update { it.copy(transactions = merged) }
        }
    }

    private fun checkSessionExpiry() {
        val state = repository.walletState.value
        if (state is WalletState.Connected) {
            val now = System.currentTimeMillis() / 1000
            val remaining = state.sessionExpiresAt - now
            when {
                remaining <= 0 -> {
                    repository.disconnect()
                }
                remaining < 300 -> {
                    _uiState.update { it.copy(sessionExpiryWarning = true) }
                }
            }
        }
    }

    fun disconnect() {
        repository.disconnect()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
