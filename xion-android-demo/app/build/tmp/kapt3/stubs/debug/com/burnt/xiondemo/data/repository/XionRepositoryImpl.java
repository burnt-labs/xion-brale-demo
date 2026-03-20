package com.burnt.xiondemo.data.repository;

import com.burnt.xiondemo.data.datasource.MobDataSource;
import com.burnt.xiondemo.data.model.BalanceInfo;
import com.burnt.xiondemo.data.model.ConnectionStep;
import com.burnt.xiondemo.data.model.GrantExpiredException;
import com.burnt.xiondemo.data.model.TransactionResult;
import com.burnt.xiondemo.data.model.WalletState;
import com.burnt.xiondemo.security.Bip39;
import com.burnt.xiondemo.security.SecureStorage;
import com.burnt.xiondemo.util.Constants;
import com.burnt.xiondemo.util.Result;
import kotlinx.coroutines.flow.StateFlow;
import uniffi.mob.Coin;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J,\u0010\u0016\u001a\u0004\u0018\u00010\n2\u0006\u0010\u0017\u001a\u00020\u000e2\b\b\u0002\u0010\u0018\u001a\u00020\u00192\b\b\u0002\u0010\u001a\u001a\u00020\u001bH\u0082@\u00a2\u0006\u0002\u0010\u001cJ\u001c\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\u000e0\u001e2\u0006\u0010\u001f\u001a\u00020\u000eH\u0096@\u00a2\u0006\u0002\u0010 J\b\u0010!\u001a\u00020\"H\u0016J.\u0010#\u001a\b\u0012\u0004\u0012\u00020\n0\u001e2\u0006\u0010$\u001a\u00020\u000e2\u0006\u0010%\u001a\u00020\u000e2\b\u0010&\u001a\u0004\u0018\u00010\u000eH\u0096@\u00a2\u0006\u0002\u0010\'J\u0014\u0010(\u001a\b\u0012\u0004\u0012\u00020)0\u001eH\u0096@\u00a2\u0006\u0002\u0010*J\u0014\u0010+\u001a\b\u0012\u0004\u0012\u00020\u001b0\u001eH\u0096@\u00a2\u0006\u0002\u0010*J\u001c\u0010,\u001a\b\u0012\u0004\u0012\u00020\n0\u001e2\u0006\u0010\u0017\u001a\u00020\u000eH\u0096@\u00a2\u0006\u0002\u0010 J\u0014\u0010-\u001a\b\u0012\u0004\u0012\u00020\u000e0\u001eH\u0096@\u00a2\u0006\u0002\u0010*J\u0014\u0010.\u001a\b\u0012\u0004\u0012\u00020/0\u001eH\u0096@\u00a2\u0006\u0002\u0010*J,\u00100\u001a\b\u0012\u0004\u0012\u00020\n0\u001e2\u0006\u00101\u001a\u00020\u000e2\u0006\u00102\u001a\u00020\u000e2\u0006\u00103\u001a\u00020\u000eH\u0096@\u00a2\u0006\u0002\u0010\'J8\u00104\u001a\b\u0012\u0004\u0012\u0002H50\u001e\"\u0004\b\u0000\u001052\u001c\u00106\u001a\u0018\b\u0001\u0012\n\u0012\b\u0012\u0004\u0012\u0002H508\u0012\u0006\u0012\u0004\u0018\u00010907H\u0082@\u00a2\u0006\u0002\u0010:R\u001a\u0010\u0007\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R \u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\n0\t0\u0011X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u001a\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\f0\u0011X\u0096\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0013\u00a8\u0006;"}, d2 = {"Lcom/burnt/xiondemo/data/repository/XionRepositoryImpl;", "Lcom/burnt/xiondemo/data/repository/XionRepository;", "mobDataSource", "Lcom/burnt/xiondemo/data/datasource/MobDataSource;", "secureStorage", "Lcom/burnt/xiondemo/security/SecureStorage;", "(Lcom/burnt/xiondemo/data/datasource/MobDataSource;Lcom/burnt/xiondemo/security/SecureStorage;)V", "_transactionHistory", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/burnt/xiondemo/data/model/TransactionResult;", "_walletState", "Lcom/burnt/xiondemo/data/model/WalletState;", "pendingSessionKeyAddress", "", "pendingSessionMnemonic", "transactionHistory", "Lkotlinx/coroutines/flow/StateFlow;", "getTransactionHistory", "()Lkotlinx/coroutines/flow/StateFlow;", "walletState", "getWalletState", "awaitTxConfirmation", "txHash", "maxAttempts", "", "delayMs", "", "(Ljava/lang/String;IJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "completeConnection", "Lcom/burnt/xiondemo/util/Result;", "metaAccountAddress", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "disconnect", "", "executeContract", "contractAddress", "msg", "funds", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBalance", "Lcom/burnt/xiondemo/data/model/BalanceInfo;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBlockHeight", "getTx", "prepareSessionKey", "restoreSession", "", "send", "toAddress", "amount", "memo", "withGrantRecovery", "T", "block", "Lkotlin/Function1;", "Lkotlin/coroutines/Continuation;", "", "(Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class XionRepositoryImpl implements com.burnt.xiondemo.data.repository.XionRepository {
    @org.jetbrains.annotations.NotNull()
    private final com.burnt.xiondemo.data.datasource.MobDataSource mobDataSource = null;
    @org.jetbrains.annotations.NotNull()
    private final com.burnt.xiondemo.security.SecureStorage secureStorage = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.burnt.xiondemo.data.model.WalletState> _walletState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.burnt.xiondemo.data.model.WalletState> walletState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.burnt.xiondemo.data.model.TransactionResult>> _transactionHistory = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.burnt.xiondemo.data.model.TransactionResult>> transactionHistory = null;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String pendingSessionMnemonic;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String pendingSessionKeyAddress;
    
    @javax.inject.Inject()
    public XionRepositoryImpl(@org.jetbrains.annotations.NotNull()
    com.burnt.xiondemo.data.datasource.MobDataSource mobDataSource, @org.jetbrains.annotations.NotNull()
    com.burnt.xiondemo.security.SecureStorage secureStorage) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.StateFlow<com.burnt.xiondemo.data.model.WalletState> getWalletState() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public kotlinx.coroutines.flow.StateFlow<java.util.List<com.burnt.xiondemo.data.model.TransactionResult>> getTransactionHistory() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object prepareSessionKey(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<java.lang.String>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object completeConnection(@org.jetbrains.annotations.NotNull()
    java.lang.String metaAccountAddress, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<java.lang.String>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object restoreSession(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<java.lang.Boolean>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getBalance(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<com.burnt.xiondemo.data.model.BalanceInfo>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getBlockHeight(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<java.lang.Long>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object send(@org.jetbrains.annotations.NotNull()
    java.lang.String toAddress, @org.jetbrains.annotations.NotNull()
    java.lang.String amount, @org.jetbrains.annotations.NotNull()
    java.lang.String memo, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<com.burnt.xiondemo.data.model.TransactionResult>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object executeContract(@org.jetbrains.annotations.NotNull()
    java.lang.String contractAddress, @org.jetbrains.annotations.NotNull()
    java.lang.String msg, @org.jetbrains.annotations.Nullable()
    java.lang.String funds, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<com.burnt.xiondemo.data.model.TransactionResult>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getTx(@org.jetbrains.annotations.NotNull()
    java.lang.String txHash, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<com.burnt.xiondemo.data.model.TransactionResult>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    public void disconnect() {
    }
    
    private final java.lang.Object awaitTxConfirmation(java.lang.String txHash, int maxAttempts, long delayMs, kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.TransactionResult> $completion) {
        return null;
    }
    
    private final <T extends java.lang.Object>java.lang.Object withGrantRecovery(kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super T>, ? extends java.lang.Object> block, kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<? extends T>> $completion) {
        return null;
    }
}