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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\t\n\u0002\b\t\bf\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u0005J\b\u0010\u0006\u001a\u00020\u0007H&JJ\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u00032\u0006\u0010\u000b\u001a\u00020\f2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e2\b\u0010\u0010\u001a\u0004\u0018\u00010\u00032\b\u0010\u0011\u001a\u0004\u0018\u00010\u00032\b\u0010\u0012\u001a\u0004\u0018\u00010\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u0013J\u001e\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00032\u0006\u0010\u0017\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u0018J\u000e\u0010\u0019\u001a\u00020\u001aH\u00a6@\u00a2\u0006\u0002\u0010\u001bJ\n\u0010\u001c\u001a\u0004\u0018\u00010\u0003H&J\u0016\u0010\u001d\u001a\u00020\t2\u0006\u0010\u001e\u001a\u00020\u0003H\u00a6@\u00a2\u0006\u0002\u0010\u0005JB\u0010\u001f\u001a\u00020\t2\u0006\u0010 \u001a\u00020\u00032\f\u0010!\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e2\b\u0010\u0010\u001a\u0004\u0018\u00010\u00032\b\u0010\u0011\u001a\u0004\u0018\u00010\u00032\b\u0010\u0012\u001a\u0004\u0018\u00010\u0003H\u00a6@\u00a2\u0006\u0002\u0010\"\u00a8\u0006#"}, d2 = {"Lcom/burnt/xiondemo/data/datasource/MobDataSource;", "", "createClientWithSigner", "", "mnemonic", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "disconnect", "", "executeContract", "Lcom/burnt/xiondemo/data/model/TransactionResult;", "contractAddress", "msg", "", "funds", "", "Luniffi/mob/Coin;", "granter", "feeGranter", "memo", "(Ljava/lang/String;[BLjava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBalance", "Lcom/burnt/xiondemo/data/model/BalanceInfo;", "address", "denom", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getHeight", "", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getSignerAddress", "getTx", "txHash", "send", "toAddress", "coins", "(Ljava/lang/String;Ljava/util/List;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface MobDataSource {
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object createClientWithSigner(@org.jetbrains.annotations.NotNull()
    java.lang.String mnemonic, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.String> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getHeight(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getBalance(@org.jetbrains.annotations.NotNull()
    java.lang.String address, @org.jetbrains.annotations.NotNull()
    java.lang.String denom, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.BalanceInfo> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object send(@org.jetbrains.annotations.NotNull()
    java.lang.String toAddress, @org.jetbrains.annotations.NotNull()
    java.util.List<uniffi.mob.Coin> coins, @org.jetbrains.annotations.Nullable()
    java.lang.String granter, @org.jetbrains.annotations.Nullable()
    java.lang.String feeGranter, @org.jetbrains.annotations.Nullable()
    java.lang.String memo, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.TransactionResult> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object executeContract(@org.jetbrains.annotations.NotNull()
    java.lang.String contractAddress, @org.jetbrains.annotations.NotNull()
    byte[] msg, @org.jetbrains.annotations.NotNull()
    java.util.List<uniffi.mob.Coin> funds, @org.jetbrains.annotations.Nullable()
    java.lang.String granter, @org.jetbrains.annotations.Nullable()
    java.lang.String feeGranter, @org.jetbrains.annotations.Nullable()
    java.lang.String memo, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.TransactionResult> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getTx(@org.jetbrains.annotations.NotNull()
    java.lang.String txHash, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.burnt.xiondemo.data.model.TransactionResult> $completion);
    
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.String getSignerAddress();
    
    public abstract void disconnect();
}