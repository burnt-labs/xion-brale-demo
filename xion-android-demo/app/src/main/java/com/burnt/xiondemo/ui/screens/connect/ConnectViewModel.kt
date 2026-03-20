package com.burnt.xiondemo.ui.screens.connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.burnt.xiondemo.auth.OAuthManager
import com.burnt.xiondemo.data.model.ConnectionStep
import com.burnt.xiondemo.data.model.WalletState
import com.burnt.xiondemo.data.repository.XionRepository
import com.burnt.xiondemo.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ConnectUiState(
    val isLoading: Boolean = false,
    val loadingMessage: String = "Connecting...",
    val error: String? = null,
    val isConnected: Boolean = false
)

@HiltViewModel
class ConnectViewModel @Inject constructor(
    private val repository: XionRepository,
    private val oAuthManager: OAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectUiState())
    val uiState: StateFlow<ConnectUiState> = _uiState.asStateFlow()

    init {
        // Try to restore an existing session on launch
        viewModelScope.launch {
            val result = repository.restoreSession()
            if (result is Result.Success && result.data) {
                _uiState.update { it.copy(isConnected = true, isLoading = false) }
            } else if (result is Result.Error) {
                _uiState.update { it.copy(error = result.message) }
            }
        }

        // Watch wallet state for connection step changes
        viewModelScope.launch {
            repository.walletState.collect { state ->
                when (state) {
                    is WalletState.Connected -> {
                        _uiState.update { it.copy(isConnected = true, isLoading = false) }
                    }
                    is WalletState.Connecting -> {
                        val message = when (state.step) {
                            ConnectionStep.AUTHENTICATING -> "Authenticating..."
                            ConnectionStep.GENERATING_SESSION_KEY -> "Generating session key..."
                            ConnectionStep.SETTING_UP_GRANTS -> "Setting up authorization grants..."
                            ConnectionStep.VERIFYING_GRANTS -> "Verifying grants..."
                        }
                        _uiState.update { it.copy(isLoading = true, loadingMessage = message) }
                    }
                    is WalletState.Disconnected -> {
                        _uiState.update { it.copy(isConnected = false, isLoading = false) }
                    }
                }
            }
        }

        // Watch auth callbacks — receives meta account address from abstraxion dashboard
        viewModelScope.launch {
            oAuthManager.callbackFlow.collect { callback ->
                handleAuthCallback(callback.metaAccountAddress)
            }
        }
    }

    fun startOAuthFlow(activity: android.app.Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Generating session key...", error = null) }

            when (val result = repository.prepareSessionKey()) {
                is Result.Success -> {
                    val granteeAddress = result.data
                    _uiState.update { it.copy(loadingMessage = "Authenticating...") }
                    oAuthManager.launchAuth(activity, granteeAddress)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun handleAuthCallback(metaAccountAddress: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Completing sign-in...", error = null) }
            when (val result = repository.completeConnection(metaAccountAddress)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isConnected = true, isLoading = false) }
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
}
