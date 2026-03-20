package com.burnt.xiondemo.ui.screens.wallet;

import androidx.lifecycle.ViewModel;
import com.burnt.xiondemo.data.model.WalletState;
import com.burnt.xiondemo.data.repository.XionRepository;
import com.burnt.xiondemo.util.Constants;
import com.burnt.xiondemo.util.Result;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import javax.inject.Inject;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\"\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001B}\u0012\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\t\u001a\u00020\u0007\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\u0003\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0007\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\u0010J\u000b\u0010\u001e\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\u001f\u001a\u00020\u0007H\u00c6\u0003J\t\u0010 \u001a\u00020\u0007H\u00c6\u0003J\u000b\u0010!\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010\"\u001a\u00020\u0003H\u00c6\u0003J\t\u0010#\u001a\u00020\u0007H\u00c6\u0003J\u000b\u0010$\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010%\u001a\u00020\u0007H\u00c6\u0003J\u0010\u0010&\u001a\u0004\u0018\u00010\u000bH\u00c6\u0003\u00a2\u0006\u0002\u0010\u0015J\t\u0010\'\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010(\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u0086\u0001\u0010)\u001a\u00020\u00002\n\b\u0002\u0010\u0002\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\t\u001a\u00020\u00072\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000b2\b\b\u0002\u0010\f\u001a\u00020\u00032\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u000e\u001a\u00020\u00072\b\b\u0002\u0010\u000f\u001a\u00020\u0007H\u00c6\u0001\u00a2\u0006\u0002\u0010*J\u0013\u0010+\u001a\u00020\u00072\b\u0010,\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010-\u001a\u00020.H\u00d6\u0001J\t\u0010/\u001a\u00020\u0003H\u00d6\u0001R\u0013\u0010\u0002\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u0012R\u0013\u0010\b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0012R\u0015\u0010\n\u001a\u0004\u0018\u00010\u000b\u00a2\u0006\n\n\u0002\u0010\u0016\u001a\u0004\b\u0014\u0010\u0015R\u0011\u0010\f\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0012R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0012R\u0013\u0010\r\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0012R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u001bR\u0011\u0010\t\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\u001bR\u0011\u0010\u000f\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u001bR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u0012R\u0011\u0010\u000e\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001b\u00a8\u00060"}, d2 = {"Lcom/burnt/xiondemo/ui/screens/wallet/WalletUiState;", "", "address", "", "sessionAddress", "connectionType", "grantsActive", "", "balance", "isBalanceLoading", "blockHeight", "", "chainId", "error", "sessionExpiryWarning", "isDisconnected", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;ZLjava/lang/Long;Ljava/lang/String;Ljava/lang/String;ZZ)V", "getAddress", "()Ljava/lang/String;", "getBalance", "getBlockHeight", "()Ljava/lang/Long;", "Ljava/lang/Long;", "getChainId", "getConnectionType", "getError", "getGrantsActive", "()Z", "getSessionAddress", "getSessionExpiryWarning", "component1", "component10", "component11", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/String;ZLjava/lang/Long;Ljava/lang/String;Ljava/lang/String;ZZ)Lcom/burnt/xiondemo/ui/screens/wallet/WalletUiState;", "equals", "other", "hashCode", "", "toString", "app_debug"})
public final class WalletUiState {
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String address = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String sessionAddress = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String connectionType = null;
    private final boolean grantsActive = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String balance = null;
    private final boolean isBalanceLoading = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Long blockHeight = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String chainId = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String error = null;
    private final boolean sessionExpiryWarning = false;
    private final boolean isDisconnected = false;
    
    public WalletUiState(@org.jetbrains.annotations.Nullable()
    java.lang.String address, @org.jetbrains.annotations.Nullable()
    java.lang.String sessionAddress, @org.jetbrains.annotations.NotNull()
    java.lang.String connectionType, boolean grantsActive, @org.jetbrains.annotations.Nullable()
    java.lang.String balance, boolean isBalanceLoading, @org.jetbrains.annotations.Nullable()
    java.lang.Long blockHeight, @org.jetbrains.annotations.NotNull()
    java.lang.String chainId, @org.jetbrains.annotations.Nullable()
    java.lang.String error, boolean sessionExpiryWarning, boolean isDisconnected) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getAddress() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSessionAddress() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getConnectionType() {
        return null;
    }
    
    public final boolean getGrantsActive() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getBalance() {
        return null;
    }
    
    public final boolean isBalanceLoading() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long getBlockHeight() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getChainId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getError() {
        return null;
    }
    
    public final boolean getSessionExpiryWarning() {
        return false;
    }
    
    public final boolean isDisconnected() {
        return false;
    }
    
    public WalletUiState() {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component1() {
        return null;
    }
    
    public final boolean component10() {
        return false;
    }
    
    public final boolean component11() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    public final boolean component4() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component5() {
        return null;
    }
    
    public final boolean component6() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long component7() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.burnt.xiondemo.ui.screens.wallet.WalletUiState copy(@org.jetbrains.annotations.Nullable()
    java.lang.String address, @org.jetbrains.annotations.Nullable()
    java.lang.String sessionAddress, @org.jetbrains.annotations.NotNull()
    java.lang.String connectionType, boolean grantsActive, @org.jetbrains.annotations.Nullable()
    java.lang.String balance, boolean isBalanceLoading, @org.jetbrains.annotations.Nullable()
    java.lang.Long blockHeight, @org.jetbrains.annotations.NotNull()
    java.lang.String chainId, @org.jetbrains.annotations.Nullable()
    java.lang.String error, boolean sessionExpiryWarning, boolean isDisconnected) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}