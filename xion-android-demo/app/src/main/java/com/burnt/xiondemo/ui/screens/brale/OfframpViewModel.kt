package com.burnt.xiondemo.ui.screens.brale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnt.xiondemo.data.model.BraleAddress
import com.burnt.xiondemo.data.model.BraleTransfer
import com.burnt.xiondemo.data.model.WalletState
import com.burnt.xiondemo.data.repository.BraleRepository
import com.burnt.xiondemo.data.repository.XionRepository
import com.burnt.xiondemo.security.SecureStorage
import com.burnt.xiondemo.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OfframpUiState(
    val amount: String = "",
    val amountError: String? = null,
    val bankLinked: Boolean = false,
    val bankAddressId: String? = null,
    val custodialAddress: BraleAddress? = null,
    val isLoading: Boolean = false,
    val depositTxHash: String? = null,
    val transfer: BraleTransfer? = null,
    val error: String? = null,
    val step: OfframpStep = OfframpStep.FORM
)

enum class OfframpStep { FORM, DEPOSITING, PROCESSING, STATUS }

@HiltViewModel
class OfframpViewModel @Inject constructor(
    private val braleRepository: BraleRepository,
    private val xionRepository: XionRepository,
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(OfframpUiState())
    val uiState: StateFlow<OfframpUiState> = _uiState.asStateFlow()

    init {
        val bankId = secureStorage.getString(Constants.PREF_BRALE_BANK_ADDRESS_ID)
        _uiState.value = _uiState.value.copy(
            bankLinked = bankId != null,
            bankAddressId = bankId
        )
        loadCustodialAddress()
    }

    private fun loadCustodialAddress() {
        viewModelScope.launch {
            try {
                val internals = braleRepository.getInternalAddresses()
                val custodial = internals.firstOrNull()
                _uiState.value = _uiState.value.copy(custodialAddress = custodial)
            } catch (_: Exception) {}
        }
    }

    fun updateAmount(value: String) {
        val error = if (value.isNotEmpty()) {
            val num = value.toDoubleOrNull()
            when {
                num == null -> "Enter a valid amount"
                num <= 0 -> "Amount must be positive"
                else -> null
            }
        } else null
        _uiState.value = _uiState.value.copy(amount = value, amountError = error)
    }

    val isFormValid: Boolean
        get() {
            val state = _uiState.value
            return state.amount.toDoubleOrNull()?.let { it > 0 } == true
                && state.amountError == null
                && state.bankLinked
                && state.custodialAddress != null
        }

    fun submitOfframp() {
        viewModelScope.launch {
            val state = _uiState.value
            val bankId = state.bankAddressId ?: return@launch
            val custodial = state.custodialAddress ?: return@launch
            val custodialWallet = custodial.address ?: run {
                _uiState.value = state.copy(error = "No custodial deposit address found")
                return@launch
            }

            _uiState.value = state.copy(isLoading = true, error = null, step = OfframpStep.DEPOSITING)

            try {
                // Step 1: Send stablecoins to Brale custodial address on-chain
                val microAmount = com.burnt.xiondemo.util.CoinFormatter.displayToMicro(state.amount)
                val sendResult = xionRepository.send(
                    toAddress = custodialWallet,
                    amount = microAmount,
                    memo = "Brale offramp deposit"
                )
                if (sendResult is com.burnt.xiondemo.util.Result.Error) {
                    throw Exception(sendResult.message)
                }
                val txHash = (sendResult as com.burnt.xiondemo.util.Result.Success).data.txHash
                _uiState.value = _uiState.value.copy(
                    depositTxHash = txHash,
                    step = OfframpStep.PROCESSING
                )

                // Step 2: Create offramp transfer via Brale
                val transfer = braleRepository.createOfframpTransfer(
                    amount = state.amount,
                    custodialAddressId = custodial.id,
                    bankAddressId = bankId
                )
                _uiState.value = _uiState.value.copy(
                    transfer = transfer,
                    isLoading = false,
                    step = OfframpStep.STATUS
                )
                pollTransferStatus(transfer.id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Offramp failed",
                    isLoading = false,
                    step = OfframpStep.FORM
                )
            }
        }
    }

    private fun pollTransferStatus(transferId: String) {
        viewModelScope.launch {
            var delayMs = 3000L
            repeat(20) {
                delay(delayMs)
                try {
                    val updated = braleRepository.getTransfer(transferId)
                    _uiState.value = _uiState.value.copy(transfer = updated)
                    if (updated.status in listOf("complete", "failed", "canceled")) return@launch
                } catch (_: Exception) {}
                delayMs = minOf(delayMs * 2, 30000L)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        _uiState.value = OfframpUiState(
            bankLinked = _uiState.value.bankLinked,
            bankAddressId = _uiState.value.bankAddressId,
            custodialAddress = _uiState.value.custodialAddress
        )
    }
}
