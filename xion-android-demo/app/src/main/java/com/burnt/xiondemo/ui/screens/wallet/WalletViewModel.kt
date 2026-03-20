package com.burnt.xiondemo.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnt.xiondemo.data.model.WalletState
import com.burnt.xiondemo.data.repository.XionRepository
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
    val isBalanceLoading: Boolean = false,
    val blockHeight: Long? = null,
    val chainId: String = Constants.CHAIN_ID,
    val error: String? = null,
    val sessionExpiryWarning: Boolean = false,
    val isDisconnected: Boolean = false
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val repository: XionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.walletState.collect { state ->
                when (state) {
                    is WalletState.Connected -> {
                        _uiState.update {
                            it.copy(
                                address = state.metaAccountAddress,
                                sessionAddress = state.sessionKeyAddress,
                                connectionType = "Meta Account",
                                grantsActive = state.grantsActive
                            )
                        }
                    }
                    is WalletState.Disconnected -> {
                        _uiState.update { it.copy(isDisconnected = true) }
                    }
                    is WalletState.Connecting -> {}
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
        loadBlockHeight()
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
