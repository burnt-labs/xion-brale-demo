package com.burnt.xiondemo.ui.screens.contract

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnt.xiondemo.data.repository.XionRepository
import com.burnt.xiondemo.util.Constants
import com.burnt.xiondemo.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContractUiState(
    val contractAddress: String = Constants.SAMPLE_CONTRACT_ADDRESS,
    val message: String = Constants.SAMPLE_CONTRACT_MSG,
    val funds: String = "",
    val isFormValid: Boolean = false,
    val isLoading: Boolean = false,
    val txHash: String? = null,
    val error: String? = null
)

@HiltViewModel
class ContractViewModel @Inject constructor(
    private val repository: XionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContractUiState())
    val uiState: StateFlow<ContractUiState> = _uiState.asStateFlow()

    init {
        validateForm()
    }

    fun updateContractAddress(value: String) {
        _uiState.update { it.copy(contractAddress = value) }
        validateForm()
    }

    fun updateMessage(value: String) {
        _uiState.update { it.copy(message = value) }
        validateForm()
    }

    fun updateFunds(value: String) {
        _uiState.update { it.copy(funds = value) }
    }

    private fun validateForm() {
        val state = _uiState.value
        val valid = state.contractAddress.startsWith(Constants.ADDRESS_PREFIX) &&
            state.contractAddress.length >= 39 &&
            state.message.isNotBlank() &&
            isValidJson(state.message)
        _uiState.update { it.copy(isFormValid = valid) }
    }

    private fun isValidJson(json: String): Boolean {
        return try {
            // Basic check: starts with { and ends with }
            val trimmed = json.trim()
            trimmed.startsWith("{") && trimmed.endsWith("}")
        } catch (e: Exception) {
            false
        }
    }

    fun execute() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val funds = state.funds.ifBlank { null }
            when (val result = repository.executeContract(state.contractAddress, state.message, funds)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, txHash = result.data.txHash) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    fun reset() {
        _uiState.update {
            ContractUiState()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
