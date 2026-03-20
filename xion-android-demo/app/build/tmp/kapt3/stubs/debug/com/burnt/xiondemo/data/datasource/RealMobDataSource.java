package com.burnt.xiondemo.data.datasource;

import com.burnt.xiondemo.data.model.BalanceInfo;
import com.burnt.xiondemo.data.model.TransactionResult;
import kotlinx.coroutines.Dispatchers;
import uniffi.mob.ChainConfig;
import uniffi.mob.Client;
import uniffi.mob.Coin;
import uniffi.mob.Signer;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000P\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\t\b\u0007\u0018\u00002\u00020\u0001B\u0007\b\u0007\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\nJ\b\u0010\u000b\u001a\u00020\fH\u0016JJ\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\b2\u0006\u0010\u0010\u001a\u00020\u00112\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00140\u00132\b\u0010\u0015\u001a\u0004\u0018\u00010\b2\b\u0010\u0016\u001a\u0004\u0018\u00010\b2\b\u0010\u0017\u001a\u0004\u0018\u00010\bH\u0096@\u00a2\u0006\u0002\u0010\u0018J\u001e\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\b2\u0006\u0010\u001c\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\u001dJ\u000e\u0010\u001e\u001a\u00020\u001fH\u0096@\u00a2\u0006\u0002\u0010 J\n\u0010!\u001a\u0004\u0018\u00010\bH\u0016J\u0016\u0010\"\u001a\u00020\u000e2\u0006\u0010#\u001a\u00020\bH\u0096@\u00a2\u0006\u0002\u0010\nJB\u0010$\u001a\u00020\u000e2\u0006\u0010%\u001a\u00020\b2\f\u0010&\u001a\b\u0012\u0004\u0012\u00020\u00140\u00132\b\u0010\u0015\u001a\u0004\u0018\u00010\b2\b\u0010\u0016\u001a\u0004\u0018\u00010\b2\b\u0010\u0017\u001a\u0004\u0018\u00010\bH\u0096@\u00a2\u0006\u0002\u0010\'R\u0010\u0010\u0003\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006("}, d2 = {"Lcom/burnt/xiondemo/data/datasource/RealMobDataSource;", "Lcom/burnt/xiondemo/data/datasource/MobDataSource;", "()V", "client", "Luniffi/mob/Client;", "signer", "Luniffi/mob/Signer;", "createClientWithSigner", "", "mnemonic", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "disconnect", "", "executeContract", "Lcom/burnt/xiondemo/data/model/TransactionResult;", "contractAddress", "msg", "", "funds", "", "Luniffi/mob/Coin;", "granter", "feeGranter", "memo", "(Ljava/lang/String;[BLjava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBalance", "Lcom/burnt/xiondemo/data/model/BalanceInfo;", "address", "denom", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getHeight", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getSignerAddress", "getTx", "txHash", "send", "toAddress", "coins", "(Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class RealMobDataSource implements com.burnt.xiondemo.data.datasource.MobDataSource {
    @org.jetbrains.annotations.Nullable()
    private uniffi.mob.Client client;
    @org.jetbrains.annotations.Nullable()
    private uniffi.mob.Signer signer;
    
    @javax.inject.Inject()
    public RealMobDataSource() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object createClientWithSigner(@org.jetbrains.annotations.NotNull()
    java.lang.String mnemonic, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getHeight(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getBalance(@org.jetbrains.annotations.NotNull()
    java.lang.String address, @org.jetbrains.annotations.NotNull()
    java.lang.String denom, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.BalanceInfo> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object send(@org.jetbrains.annotations.NotNull()
    java.lang.String toAddress, @org.jetbrains.annotations.NotNull()
    java.util.List<uniffi.mob.Coin> coins, @org.jetbrains.annotations.Nullable()
    java.lang.String granter, @org.jetbrains.annotations.Nullable()
    java.lang.String feeGranter, @org.jetbrains.annotations.Nullable()
    java.lang.String memo, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.TransactionResult> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object executeContract(@org.jetbrains.annotations.NotNull()
    java.lang.String contractAddress, @org.jetbrains.annotations.NotNull()
    byte[] msg, @org.jetbrains.annotations.NotNull()
    java.util.List<uniffi.mob.Coin> funds, @org.jetbrains.annotations.Nullable()
    java.lang.String granter, @org.jetbrains.annotations.Nullable()
    java.lang.String feeGranter, @org.jetbrains.annotations.Nullable()
    java.lang.String memo, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.TransactionResult> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object getTx(@org.jetbrains.annotations.NotNull()
    java.lang.String txHash, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.TransactionResult> $completion) {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.String getSignerAddress() {
        return null;
    }
    
    @java.lang.Override()
    public void disconnect() {
    }
}