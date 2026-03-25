package com.burnt.xiondemo.ui.screens.brale

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnt.xiondemo.data.model.BraleTransfer
import com.burnt.xiondemo.data.model.WalletState
import com.burnt.xiondemo.data.repository.BraleRepository
import com.burnt.xiondemo.data.repository.XionRepository
import com.burnt.xiondemo.security.SecureStorage
import com.burnt.xiondemo.util.Constants
import com.burnt.xiondemo.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnrampUiState(
    val amount: String = "",
    val amountError: String? = null,
    val bankLinked: Boolean = false,
    val bankAddressId: String? = null,
    val bankName: String? = null,
    val xionAddressId: String? = null,
    val plaidLinkToken: String? = null,
    val isLoading: Boolean = false,
    val transfer: BraleTransfer? = null,
    val tokensReceived: Boolean = false,
    val receivedAmount: String? = null,
    val error: String? = null,
    val step: OnrampStep = OnrampStep.FORM
)

enum class OnrampStep { FORM, PROCESSING, STATUS }

@HiltViewModel
class OnrampViewModel @Inject constructor(
    private val braleRepository: BraleRepository,
    private val xionRepository: XionRepository,
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnrampUiState())
    val uiState: StateFlow<OnrampUiState> = _uiState.asStateFlow()

    private var initialSbcBalance: Long = 0

    init {
        val bankId = secureStorage.getString(Constants.PREF_BRALE_BANK_ADDRESS_ID)
        val xionId = secureStorage.getString(Constants.PREF_BRALE_XION_ADDRESS_ID)
        _uiState.value = _uiState.value.copy(
            bankLinked = bankId != null,
            bankAddressId = bankId,
            xionAddressId = xionId
        )
        if (bankId == null) checkExistingBankAddress()
        if (xionId == null) checkExistingXionAddress()
    }

    private fun checkExistingXionAddress() {
        viewModelScope.launch {
            try {
                val walletAddress = (xionRepository.walletState.value as? WalletState.Connected)?.metaAccountAddress ?: return@launch
                val existing = braleRepository.findExistingXionAddress(walletAddress)
                if (existing != null) {
                    secureStorage.putString(Constants.PREF_BRALE_XION_ADDRESS_ID, existing.id)
                    _uiState.value = _uiState.value.copy(xionAddressId = existing.id)
                }
            } catch (_: Exception) {}
        }
    }

    private fun checkExistingBankAddress() {
        viewModelScope.launch {
            try {
                val existing = braleRepository.findExistingBankAddress()
                if (existing != null) {
                    secureStorage.putString(Constants.PREF_BRALE_BANK_ADDRESS_ID, existing.id)
                    _uiState.value = _uiState.value.copy(
                        bankLinked = true,
                        bankAddressId = existing.id,
                        bankName = existing.name
                    )
                }
            } catch (_: Exception) {}
        }
    }

    fun updateAmount(value: String) {
        val error = if (value.isNotEmpty()) {
            val num = value.toDoubleOrNull()
            when {
                num == null -> "Enter a valid amount"
                num <= 0 -> "Amount must be positive"
                num > 50000 -> "ACH limit is \$50,000"
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
        }

    fun requestPlaidLinkToken(name: String, email: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = braleRepository.createPlaidLinkToken(name = name, email = email)
                _uiState.value = _uiState.value.copy(
                    plaidLinkToken = response.linkToken,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to create Plaid link",
                    isLoading = false
                )
            }
        }
    }

    fun onPlaidSuccess(publicToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, plaidLinkToken = null)
            try {
                val addressId = braleRepository.registerBankAccount(publicToken)
                secureStorage.putString(Constants.PREF_BRALE_BANK_ADDRESS_ID, addressId)
                _uiState.value = _uiState.value.copy(
                    bankLinked = true,
                    bankAddressId = addressId,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to register bank account",
                    isLoading = false
                )
            }
        }
    }

    fun onPlaidCancelled() {
        _uiState.value = _uiState.value.copy(plaidLinkToken = null, isLoading = false)
    }

    fun submitOnramp() {
        viewModelScope.launch {
            val state = _uiState.value
            val bankId = state.bankAddressId ?: return@launch
            _uiState.value = state.copy(isLoading = true, error = null, step = OnrampStep.PROCESSING)

            try {
                // Capture initial SBC balance before transfer
                initialSbcBalance = getCurrentSbcBalance()

                // Ensure Xion address is registered with Brale
                val xionAddressId = state.xionAddressId ?: run {
                    val walletAddress = xionRepository.walletState.value.let { ws ->
                        (ws as? WalletState.Connected)?.metaAccountAddress
                    } ?: throw IllegalStateException("Wallet not connected")

                    val addr = braleRepository.registerXionAddress(walletAddress)
                    secureStorage.putString(Constants.PREF_BRALE_XION_ADDRESS_ID, addr.id)
                    _uiState.value = _uiState.value.copy(xionAddressId = addr.id)
                    addr.id
                }

                val transfer = braleRepository.createOnrampTransfer(
                    amount = state.amount,
                    bankAddressId = bankId,
                    xionAddressId = xionAddressId
                )
                _uiState.value = _uiState.value.copy(
                    transfer = transfer,
                    isLoading = false
                )
                // Poll on-chain balance for token arrival
                pollForTokenArrival()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Onramp failed",
                    isLoading = false,
                    step = OnrampStep.FORM
                )
            }
        }
    }

    private suspend fun getCurrentSbcBalance(): Long {
        return when (val result = xionRepository.getSbcBalance()) {
            is Result.Success -> result.data.amount.toLongOrNull() ?: 0
            else -> 0
        }
    }

    private fun pollForTokenArrival() {
        viewModelScope.launch {
            repeat(60) { // Poll for up to ~3 minutes
                delay(3000)
                try {
                    val currentBalance = getCurrentSbcBalance()
                    if (currentBalance > initialSbcBalance) {
                        val received = currentBalance - initialSbcBalance
                        _uiState.value = _uiState.value.copy(
                            tokensReceived = true,
                            receivedAmount = received.toString(),
                            step = OnrampStep.STATUS
                        )
                        // Add to transaction history
                        xionRepository.appendTransaction(
                            com.burnt.xiondemo.data.model.TransactionResult(
                                txHash = _uiState.value.transfer?.id ?: "",
                                success = true,
                                gasUsed = "0",
                                gasWanted = "0",
                                height = 0,
                                rawLog = "",
                                txType = "Buy SBC",
                                amount = received.toString(),
                                recipient = "",
                                fee = "0",
                                timestamp = _uiState.value.transfer?.createdAt ?: ""
                            )
                        )
                        return@launch
                    }
                } catch (_: Exception) {}
            }
            // Timeout — show status anyway with what we have
            _uiState.value = _uiState.value.copy(step = OnrampStep.STATUS)
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun reset() {
        _uiState.value = OnrampUiState(
            bankLinked = _uiState.value.bankLinked,
            bankAddressId = _uiState.value.bankAddressId,
            bankName = _uiState.value.bankName,
            xionAddressId = _uiState.value.xionAddressId
        )
    }
}
