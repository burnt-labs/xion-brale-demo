package com.burnt.xiondemo.ui.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnt.xiondemo.data.repository.XionRepository
import com.burnt.xiondemo.ui.screens.send.SendToken
import com.burnt.xiondemo.util.CoinFormatter
import com.burnt.xiondemo.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VaultUiState(
    val vaultBalance: String? = null,
    val walletBalance: String? = null,
    val walletSbcBalance: String? = null,
    val selectedToken: SendToken = SendToken.XION,
    val amount: String = "",
    val isLoading: Boolean = false,
    val isBalanceLoading: Boolean = false,
    val txHash: String? = null,
    val error: String? = null
)

@HiltViewModel
class VaultViewModel @Inject constructor(
    private val repository: XionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    init {
        loadAllBalances()
    }

    fun updateAmount(value: String) {
        _uiState.update { it.copy(amount = value, error = null) }
    }

    fun selectToken(token: SendToken) {
        _uiState.update { it.copy(selectedToken = token, amount = "", error = null) }
    }

    fun loadAllBalances() {
        loadVaultBalance()
        loadWalletBalances()
    }

    private fun loadVaultBalance() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBalanceLoading = true) }
            when (val result = repository.getVaultBalance()) {
                is Result.Success -> {
                    _uiState.update { it.copy(vaultBalance = result.data.amount, isBalanceLoading = false) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isBalanceLoading = false) }
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun loadWalletBalances() {
        viewModelScope.launch {
            when (val result = repository.getBalance()) {
                is Result.Success -> {
                    _uiState.update { it.copy(walletBalance = result.data.amount) }
                }
                is Result.Error -> {}
                is Result.Loading -> {}
            }
        }
        viewModelScope.launch {
            when (val result = repository.getSbcBalance()) {
                is Result.Success -> {
                    _uiState.update { it.copy(walletSbcBalance = result.data.amount) }
                }
                is Result.Error -> {}
                is Result.Loading -> {}
            }
        }
    }

    fun deposit() {
        val state = _uiState.value
        if (state.amount.isBlank()) return
        val microAmount = CoinFormatter.displayToMicro(state.amount)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, txHash = null) }
            when (val result = repository.vaultDeposit(microAmount, state.selectedToken.denom)) {
                is Result.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, txHash = result.data.txHash, amount = "") }
                        loadAllBalances()
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = result.data.rawLog) }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun withdraw() {
        val state = _uiState.value
        if (state.amount.isBlank()) return
        val microAmount = CoinFormatter.displayToMicro(state.amount)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, txHash = null) }
            when (val result = repository.vaultWithdraw(microAmount, state.selectedToken.denom)) {
                is Result.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, txHash = result.data.txHash, amount = "") }
                        loadAllBalances()
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = result.data.rawLog) }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun withdrawAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, txHash = null) }
            when (val result = repository.vaultWithdrawAll()) {
                is Result.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, txHash = result.data.txHash, amount = "") }
                        loadAllBalances()
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = result.data.rawLog) }
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun resetState() {
        _uiState.update { it.copy(txHash = null, error = null, amount = "") }
    }
}
