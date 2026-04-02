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
import com.burnt.xiondemo.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val bankName: String? = null,
    val custodialAddress: BraleAddress? = null,
    val isLoading: Boolean = false,
    val depositTxHash: String? = null,
    val depositConfirmed: Boolean = false,
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
        // Load the xion_testnet custodial address specifically
        loadCustodialAddress()
    }

    private fun loadCustodialAddress() {
        viewModelScope.launch {
            try {
                val internals = braleRepository.getInternalAddresses()
                // Find the custodial address that supports xion_testnet specifically
                val custodial = internals.firstOrNull { addr ->
                    addr.transferTypes.contains(Constants.BRALE_TRANSFER_TYPE)
                } ?: internals.firstOrNull()
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
                // Step 1: Send SBC stablecoins to Brale custodial address on-chain
                val microAmount = com.burnt.xiondemo.util.CoinFormatter.displayToMicro(state.amount)
                val sendResult = xionRepository.send(
                    toAddress = custodialWallet,
                    amount = microAmount,
                    memo = "Brale offramp deposit",
                    denom = Constants.BRALE_SBC_ON_CHAIN_DENOM
                )
                if (sendResult is Result.Error) {
                    throw Exception(sendResult.message)
                }
                val txHash = (sendResult as Result.Success).data.txHash
                _uiState.value = _uiState.value.copy(
                    depositTxHash = txHash,
                    depositConfirmed = true,
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
                // Add to transaction history
                xionRepository.appendTransaction(
                    com.burnt.xiondemo.data.model.TransactionResult(
                        txHash = txHash,
                        success = true,
                        gasUsed = "0",
                        gasWanted = "0",
                        height = 0,
                        rawLog = "",
                        txType = "Cash Out",
                        amount = microAmount,
                        recipient = custodialWallet,
                        fee = "0",
                        timestamp = transfer.createdAt ?: ""
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Offramp failed",
                    isLoading = false,
                    step = if (_uiState.value.depositConfirmed) OfframpStep.STATUS else OfframpStep.FORM
                )
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
            bankName = _uiState.value.bankName,
            custodialAddress = _uiState.value.custodialAddress
        )
    }
}
