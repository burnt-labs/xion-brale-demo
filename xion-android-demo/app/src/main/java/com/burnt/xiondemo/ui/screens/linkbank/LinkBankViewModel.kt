package com.burnt.xiondemo.ui.screens.linkbank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnt.xiondemo.data.repository.BraleRepository
import com.burnt.xiondemo.security.SecureStorage
import com.burnt.xiondemo.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PlaidDiagnostic(
    val id: String = java.util.UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val phone: String,
    val outcome: String,
    val tokenRequestId: String?,
    val linkSessionId: String?,
    val exitRequestId: String?,
    val errorMessage: String?
)

data class LinkBankUiState(
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val userDob: String = "",
    val userNameError: String? = null,
    val userEmailError: String? = null,
    val userPhoneError: String? = null,
    val userDobError: String? = null,
    val bankLinked: Boolean = false,
    val bankAddressId: String? = null,
    val bankName: String? = null,
    val plaidLinkToken: String? = null,
    val plaidTokenRequestId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val diagnostics: List<PlaidDiagnostic> = emptyList()
)

@HiltViewModel
class LinkBankViewModel @Inject constructor(
    private val braleRepository: BraleRepository,
    private val secureStorage: SecureStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(LinkBankUiState())
    val uiState: StateFlow<LinkBankUiState> = _uiState.asStateFlow()

    init {
        val bankId = secureStorage.getString(Constants.PREF_BRALE_BANK_ADDRESS_ID)
        val savedName = secureStorage.getString(Constants.PREF_BRALE_USER_NAME) ?: ""
        val savedEmail = secureStorage.getString(Constants.PREF_BRALE_USER_EMAIL) ?: ""
        val savedPhone = secureStorage.getString(Constants.PREF_BRALE_USER_PHONE) ?: ""
        val savedDob = secureStorage.getString(Constants.PREF_BRALE_USER_DOB) ?: ""
        _uiState.value = _uiState.value.copy(
            bankLinked = bankId != null,
            bankAddressId = bankId,
            userName = savedName,
            userEmail = savedEmail,
            userPhone = savedPhone,
            userDob = savedDob
        )
    }

    fun updateUserName(value: String) {
        val error = if (value.isBlank()) "Name is required" else null
        _uiState.value = _uiState.value.copy(userName = value, userNameError = error)
    }

    fun updateUserEmail(value: String) {
        val error = when {
            value.isBlank() -> "Email is required"
            !android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches() -> "Enter a valid email address"
            else -> null
        }
        _uiState.value = _uiState.value.copy(userEmail = value, userEmailError = error)
    }

    fun updateUserPhone(value: String) {
        val error = when {
            value.isBlank() -> "Phone number is required"
            !value.startsWith("+") -> "Must start with + (e.g. +15551234567)"
            !value.drop(1).all { it.isDigit() } -> "Only digits after +"
            value.length < 11 -> "Enter full number with country code"
            else -> null
        }
        _uiState.value = _uiState.value.copy(userPhone = value, userPhoneError = error)
    }

    fun updateUserDob(value: String) {
        val error = when {
            value.isBlank() -> "Date of birth is required"
            !value.matches(Regex("""\d{4}-\d{2}-\d{2}""")) -> "Use YYYY-MM-DD format"
            else -> null
        }
        _uiState.value = _uiState.value.copy(userDob = value, userDobError = error)
    }

    val isLinkFormValid: Boolean
        get() {
            val state = _uiState.value
            return state.userName.isNotBlank()
                && state.userNameError == null
                && state.userEmail.isNotBlank()
                && state.userEmailError == null
                && state.userPhone.isNotBlank()
                && state.userPhoneError == null
                && state.userDob.isNotBlank()
                && state.userDobError == null
        }

    fun requestPlaidLinkToken() {
        val state = _uiState.value
        val name = state.userName.trim()
        val email = state.userEmail.trim()
        val phone = state.userPhone.trim()
        val dob = state.userDob.trim()
        secureStorage.putString(Constants.PREF_BRALE_USER_NAME, name)
        secureStorage.putString(Constants.PREF_BRALE_USER_EMAIL, email)
        secureStorage.putString(Constants.PREF_BRALE_USER_PHONE, phone)
        secureStorage.putString(Constants.PREF_BRALE_USER_DOB, dob)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = braleRepository.createPlaidLinkToken(name = name, email = email, phone = phone, dob = dob)
                _uiState.value = _uiState.value.copy(
                    plaidLinkToken = response.linkToken,
                    plaidTokenRequestId = response.requestId,
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

    fun onPlaidSuccess(publicToken: String, linkSessionId: String) {
        val tokenReqId = _uiState.value.plaidTokenRequestId
        val phone = _uiState.value.userPhone
        recordDiagnostic(
            phone = phone,
            outcome = "Linked",
            tokenRequestId = tokenReqId,
            linkSessionId = linkSessionId,
            exitRequestId = null,
            errorMessage = null
        )
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                plaidLinkToken = null,
                plaidTokenRequestId = null
            )
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

    fun onPlaidCancelled(linkSessionId: String?, exitRequestId: String?) {
        val tokenReqId = _uiState.value.plaidTokenRequestId
        val phone = _uiState.value.userPhone
        recordDiagnostic(
            phone = phone,
            outcome = "Cancelled",
            tokenRequestId = tokenReqId,
            linkSessionId = linkSessionId,
            exitRequestId = exitRequestId,
            errorMessage = null
        )
        _uiState.value = _uiState.value.copy(
            plaidLinkToken = null,
            plaidTokenRequestId = null,
            isLoading = false
        )
    }

    fun onPlaidExit(linkSessionId: String?, exitRequestId: String?, errorMessage: String) {
        val tokenReqId = _uiState.value.plaidTokenRequestId
        val phone = _uiState.value.userPhone
        recordDiagnostic(
            phone = phone,
            outcome = "Error",
            tokenRequestId = tokenReqId,
            linkSessionId = linkSessionId,
            exitRequestId = exitRequestId,
            errorMessage = errorMessage
        )
        _uiState.value = _uiState.value.copy(
            plaidLinkToken = null,
            plaidTokenRequestId = null,
            isLoading = false,
            error = errorMessage
        )
    }

    private fun recordDiagnostic(
        phone: String,
        outcome: String,
        tokenRequestId: String?,
        linkSessionId: String?,
        exitRequestId: String?,
        errorMessage: String?
    ) {
        val entry = PlaidDiagnostic(
            phone = phone,
            outcome = outcome,
            tokenRequestId = tokenRequestId,
            linkSessionId = linkSessionId,
            exitRequestId = exitRequestId,
            errorMessage = errorMessage
        )
        val updated = (listOf(entry) + _uiState.value.diagnostics).take(10)
        _uiState.value = _uiState.value.copy(diagnostics = updated)
        android.util.Log.d(
            "PlaidLink",
            "diag phone=$phone outcome=$outcome tokenReqId=${tokenRequestId ?: "nil"} sessionId=${linkSessionId ?: "nil"} exitReqId=${exitRequestId ?: "nil"}"
        )
    }

    fun clearDiagnostics() {
        _uiState.value = _uiState.value.copy(diagnostics = emptyList())
    }

    fun unlinkBank() {
        secureStorage.remove(Constants.PREF_BRALE_BANK_ADDRESS_ID)
        _uiState.value = _uiState.value.copy(
            bankLinked = false,
            bankAddressId = null,
            bankName = null
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
