package com.burnt.xiondemo.ui.screens.send

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnt.xiondemo.data.model.TransactionResult
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

enum class SendToken(val displayName: String, val denom: String) {
    XION("XION", Constants.COIN_DENOM),
    SBC("SBC", Constants.BRALE_SBC_ON_CHAIN_DENOM);
}

data class SendUiState(
    val recipient: String = "",
    val amount: String = "",
    val memo: String = "",
    val selectedToken: SendToken = SendToken.XION,
    val recipientError: String? = null,
    val amountError: String? = null,
    val isFormValid: Boolean = false,
    val isLoading: Boolean = false,
    val txResult: TransactionResult? = null,
    val error: String? = null
)

@HiltViewModel
class SendViewModel @Inject constructor(
    private val repository: XionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SendUiState())
    val uiState: StateFlow<SendUiState> = _uiState.asStateFlow()

    fun updateRecipient(value: String) {
        val error = when {
            value.isBlank() -> null
            !value.startsWith(Constants.ADDRESS_PREFIX) -> "Address must start with '${Constants.ADDRESS_PREFIX}'"
            value.length < 39 -> "Address too short"
            else -> null
        }
        _uiState.update {
            it.copy(recipient = value, recipientError = error, isFormValid = validateForm(value, it.amount))
        }
    }

    fun updateAmount(value: String) {
        val error = when {
            value.isBlank() -> null
            !CoinFormatter.isValidAmount(value) -> "Enter a valid amount"
            else -> null
        }
        _uiState.update {
            it.copy(amount = value, amountError = error, isFormValid = validateForm(it.recipient, value))
        }
    }

    fun updateMemo(value: String) {
        _uiState.update { it.copy(memo = value) }
    }

    fun selectToken(token: SendToken) {
        _uiState.update { it.copy(selectedToken = token) }
    }

    private fun validateForm(recipient: String, amount: String): Boolean {
        return recipient.startsWith(Constants.ADDRESS_PREFIX) &&
            recipient.length >= 39 &&
            CoinFormatter.isValidAmount(amount)
    }

    fun send() {
        val state = _uiState.value
        val microAmount = CoinFormatter.displayToMicro(state.amount)

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.send(
                toAddress = state.recipient,
                amount = microAmount,
                memo = state.memo,
                denom = state.selectedToken.denom
            )) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isLoading = false, txResult = result.data)
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
        _uiState.value = SendUiState()
    }
}
