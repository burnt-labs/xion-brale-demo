package com.burnt.xiondemo.ui.screens.connect;

import androidx.lifecycle.ViewModel;
import com.burnt.xiondemo.auth.OAuthManager;
import com.burnt.xiondemo.data.model.ConnectionStep;
import com.burnt.xiondemo.data.model.WalletState;
import com.burnt.xiondemo.data.repository.XionRepository;
import com.burnt.xiondemo.util.Result;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0006\u0010\u000e\u001a\u00020\u000fJ\u0010\u0010\u0010\u001a\u00020\u000f2\u0006\u0010\u0011\u001a\u00020\u0012H\u0002J\u000e\u0010\u0013\u001a\u00020\u000f2\u0006\u0010\u0014\u001a\u00020\u0015R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\n\u001a\b\u0012\u0004\u0012\u00020\t0\u000b\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\r\u00a8\u0006\u0016"}, d2 = {"Lcom/burnt/xiondemo/ui/screens/connect/ConnectViewModel;", "Landroidx/lifecycle/ViewModel;", "repository", "Lcom/burnt/xiondemo/data/repository/XionRepository;", "oAuthManager", "Lcom/burnt/xiondemo/auth/OAuthManager;", "(Lcom/burnt/xiondemo/data/repository/XionRepository;Lcom/burnt/xiondemo/auth/OAuthManager;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/burnt/xiondemo/ui/screens/connect/ConnectUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "clearError", "", "handleAuthCallback", "metaAccountAddress", "", "startOAuthFlow", "activity", "Landroid/app/Activity;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class ConnectViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.burnt.xiondemo.data.repository.XionRepository repository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.burnt.xiondemo.auth.OAuthManager oAuthManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.burnt.xiondemo.ui.screens.connect.ConnectUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.burnt.xiondemo.ui.screens.connect.ConnectUiState> uiState = null;
    
    @javax.inject.Inject()
    public ConnectViewModel(@org.jetbrains.annotations.NotNull()
    com.burnt.xiondemo.data.repository.XionRepository repository, @org.jetbrains.annotations.NotNull()
    com.burnt.xiondemo.auth.OAuthManager oAuthManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.burnt.xiondemo.ui.screens.connect.ConnectUiState> getUiState() {
        return null;
    }
    
    public final void startOAuthFlow(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity) {
    }
    
    private final void handleAuthCallback(java.lang.String metaAccountAddress) {
    }
    
    public final void clearError() {
    }
}