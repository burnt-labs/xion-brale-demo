package com.burnt.xiondemo.data.repository;

import com.burnt.xiondemo.data.model.BalanceInfo;
import com.burnt.xiondemo.data.model.TransactionResult;
import com.burnt.xiondemo.data.model.WalletState;
import com.burnt.xiondemo.util.Result;
import kotlinx.coroutines.flow.StateFlow;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000N\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0005\bf\u0018\u00002\u00020\u0001J\u001c\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\r0\f2\u0006\u0010\u000e\u001a\u00020\rH\u00a6@\u00a2\u0006\u0002\u0010\u000fJ\b\u0010\u0010\u001a\u00020\u0011H&J.\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00050\f2\u0006\u0010\u0013\u001a\u00020\r2\u0006\u0010\u0014\u001a\u00020\r2\b\u0010\u0015\u001a\u0004\u0018\u00010\rH\u00a6@\u00a2\u0006\u0002\u0010\u0016J\u0014\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u00180\fH\u00a6@\u00a2\u0006\u0002\u0010\u0019J\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001b0\fH\u00a6@\u00a2\u0006\u0002\u0010\u0019J\u001c\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u00050\f2\u0006\u0010\u001d\u001a\u00020\rH\u00a6@\u00a2\u0006\u0002\u0010\u000fJ\u0014\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\r0\fH\u00a6@\u00a2\u0006\u0002\u0010\u0019J\u0014\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020 0\fH\u00a6@\u00a2\u0006\u0002\u0010\u0019J,\u0010!\u001a\b\u0012\u0004\u0012\u00020\u00050\f2\u0006\u0010\"\u001a\u00020\r2\u0006\u0010#\u001a\u00020\r2\u0006\u0010$\u001a\u00020\rH\u00a6@\u00a2\u0006\u0002\u0010\u0016R\u001e\u0010\u0002\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u00040\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0006\u0010\u0007R\u0018\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\n\u0010\u0007\u00a8\u0006%"}, d2 = {"Lcom/burnt/xiondemo/data/repository/XionRepository;", "", "transactionHistory", "Lkotlinx/coroutines/flow/StateFlow;", "", "Lcom/burnt/xiondemo/data/model/TransactionResult;", "getTransactionHistory", "()Lkotlinx/coroutines/flow/StateFlow;", "walletState", "Lcom/burnt/xiondemo/data/model/WalletState;", "getWalletState", "completeConnection", "Lcom/burnt/xiondemo/util/Result;", "", "metaAccountAddress", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "disconnect", "", "executeContract", "contractAddress", "msg", "funds", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBalance", "Lcom/burnt/xiondemo/data/model/BalanceInfo;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBlockHeight", "", "getTx", "txHash", "prepareSessionKey", "restoreSession", "", "send", "toAddress", "amount", "memo", "app_debug"})
public abstract interface XionRepository {
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.StateFlow<com.burnt.xiondemo.data.model.WalletState> getWalletState();
    
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.StateFlow<java.util.List<com.burnt.xiondemo.data.model.TransactionResult>> getTransactionHistory();
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object prepareSessionKey(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<java.lang.String>> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object completeConnection(@org.jetbrains.annotations.NotNull()
    java.lang.String metaAccountAddress, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<java.lang.String>> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object restoreSession(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<java.lang.Boolean>> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getBalance(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<com.burnt.xiondemo.data.model.BalanceInfo>> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getBlockHeight(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<java.lang.Long>> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object send(@org.jetbrains.annotations.NotNull()
    java.lang.String toAddress, @org.jetbrains.annotations.NotNull()
    java.lang.String amount, @org.jetbrains.annotations.NotNull()
    java.lang.String memo, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<com.burnt.xiondemo.data.model.TransactionResult>> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object executeContract(@org.jetbrains.annotations.NotNull()
    java.lang.String contractAddress, @org.jetbrains.annotations.NotNull()
    java.lang.String msg, @org.jetbrains.annotations.Nullable()
    java.lang.String funds, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<com.burnt.xiondemo.data.model.TransactionResult>> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getTx(@org.jetbrains.annotations.NotNull()
    java.lang.String txHash, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.util.Result<com.burnt.xiondemo.data.model.TransactionResult>> $completion);
    
    public abstract void disconnect();
}