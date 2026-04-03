package com.burnt.xiondemo.ui.screens.vault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnt.xiondemo.data.repository.XionRepository
import com.burnt.xiondemo.util.CoinFormatter
import com.burnt.xiondemo.util.Constants
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
        loadVaultBalance()
    }

    fun updateAmount(value: String) {
        _uiState.update { it.copy(amount = value, error = null) }
    }

    fun loadVaultBalance() {
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

    fun deposit() {
        val amount = _uiState.value.amount
        if (amount.isBlank()) return
        val microAmount = CoinFormatter.displayToMicro(amount, Constants.DECIMALS)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, txHash = null) }
            when (val result = repository.vaultDeposit(microAmount)) {
                is Result.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, txHash = result.data.txHash, amount = "") }
                        loadVaultBalance()
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
        val amount = _uiState.value.amount
        if (amount.isBlank()) return
        val microAmount = CoinFormatter.displayToMicro(amount, Constants.DECIMALS)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, txHash = null) }
            when (val result = repository.vaultWithdraw(microAmount)) {
                is Result.Success -> {
                    if (result.data.success) {
                        _uiState.update { it.copy(isLoading = false, txHash = result.data.txHash, amount = "") }
                        loadVaultBalance()
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
                        loadVaultBalance()
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
