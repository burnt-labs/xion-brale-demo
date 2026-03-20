package uniffi.mob;

import com.sun.jna.Library;
import com.sun.jna.IntegerType;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Callback;
import com.sun.jna.ptr.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.CodingErrorAction;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RPC client for interacting with the blockchain
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0005\bf\u0018\u0000 $2\u00020\u0001:\u0001$J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&JD\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\f\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\r2\b\u0010\u000f\u001a\u0004\u0018\u00010\t2\b\u0010\u0010\u001a\u0004\u0018\u00010\t2\b\u0010\u0011\u001a\u0004\u0018\u00010\tH&J\u0010\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\tH&J\u0016\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u000e0\r2\u0006\u0010\u0014\u001a\u00020\tH&J\u0018\u0010\u0016\u001a\u00020\u000e2\u0006\u0010\u0014\u001a\u00020\t2\u0006\u0010\u0017\u001a\u00020\tH&J\b\u0010\u0018\u001a\u00020\tH&J\u0015\u0010\u0019\u001a\u00020\u001aH&\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u001b\u0010\u001cJ\u0010\u0010\u001d\u001a\u00020\u00072\u0006\u0010\u001e\u001a\u00020\tH&J\b\u0010\u001f\u001a\u00020 H&J<\u0010!\u001a\u00020\u00072\u0006\u0010\"\u001a\u00020\t2\f\u0010#\u001a\b\u0012\u0004\u0012\u00020\u000e0\r2\b\u0010\u000f\u001a\u0004\u0018\u00010\t2\b\u0010\u0010\u001a\u0004\u0018\u00010\t2\b\u0010\u0011\u001a\u0004\u0018\u00010\tH&\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006%"}, d2 = {"Luniffi/mob/ClientInterface;", "", "attachSigner", "", "signer", "Luniffi/mob/Signer;", "executeContract", "Luniffi/mob/TxResponse;", "contractAddress", "", "msg", "", "funds", "", "Luniffi/mob/Coin;", "granter", "feeGranter", "memo", "getAccount", "Luniffi/mob/AccountInfo;", "address", "getAllBalances", "getBalance", "denom", "getChainId", "getHeight", "Lkotlin/ULong;", "getHeight-s-VKNKU", "()J", "getTx", "hash", "isSynced", "", "send", "toAddress", "amount", "Companion", "app_debug"})
public abstract interface ClientInterface {
    @org.jetbrains.annotations.NotNull()
    public static final uniffi.mob.ClientInterface.Companion Companion = null;
    
    /**
     * Attach a signer to the client
     */
    public abstract void attachSigner(@org.jetbrains.annotations.NotNull()
    uniffi.mob.Signer signer);
    
    /**
     * Execute a CosmWasm contract (synchronous wrapper).
     * Mirrors xion.js GranteeSignerClient behavior:
     * - If `granter` is set, wraps MsgExecuteContract in MsgExec (authz),
     * using the granter as the contract sender and the signer as the grantee.
     * - If `fee_granter` is set, the fee granter (e.g. treasury) pays gas.
     */
    @org.jetbrains.annotations.NotNull()
    public abstract uniffi.mob.TxResponse executeContract(@org.jetbrains.annotations.NotNull()
    java.lang.String contractAddress, @org.jetbrains.annotations.NotNull()
    byte[] msg, @org.jetbrains.annotations.NotNull()
    java.util.List<uniffi.mob.Coin> funds, @org.jetbrains.annotations.Nullable()
    java.lang.String granter, @org.jetbrains.annotations.Nullable()
    java.lang.String feeGranter, @org.jetbrains.annotations.Nullable()
    java.lang.String memo);
    
    /**
     * Query account information (synchronous wrapper)
     */
    @org.jetbrains.annotations.NotNull()
    public abstract uniffi.mob.AccountInfo getAccount(@org.jetbrains.annotations.NotNull()
    java.lang.String address);
    
    /**
     * Query all balances for an address (synchronous wrapper)
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.util.List<uniffi.mob.Coin> getAllBalances(@org.jetbrains.annotations.NotNull()
    java.lang.String address);
    
    /**
     * Query account balance (synchronous wrapper)
     */
    @org.jetbrains.annotations.NotNull()
    public abstract uniffi.mob.Coin getBalance(@org.jetbrains.annotations.NotNull()
    java.lang.String address, @org.jetbrains.annotations.NotNull()
    java.lang.String denom);
    
    /**
     * Get chain ID (synchronous wrapper)
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String getChainId();
    
    /**
     * Query transaction by hash (synchronous wrapper)
     */
    @org.jetbrains.annotations.NotNull()
    public abstract uniffi.mob.TxResponse getTx(@org.jetbrains.annotations.NotNull()
    java.lang.String hash);
    
    /**
     * Check if the node is synced (synchronous wrapper)
     */
    public abstract boolean isSynced();
    
    /**
     * Send tokens to a recipient (synchronous wrapper).
     * If `granter` is set, wraps MsgSend in MsgExec (authz),
     * using the granter as the sender and the signer as the grantee.
     * If `fee_granter` is set, the fee granter (e.g. treasury) pays gas.
     */
    @org.jetbrains.annotations.NotNull()
    public abstract uniffi.mob.TxResponse send(@org.jetbrains.annotations.NotNull()
    java.lang.String toAddress, @org.jetbrains.annotations.NotNull()
    java.util.List<uniffi.mob.Coin> amount, @org.jetbrains.annotations.Nullable()
    java.lang.String granter, @org.jetbrains.annotations.Nullable()
    java.lang.String feeGranter, @org.jetbrains.annotations.Nullable()
    java.lang.String memo);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Luniffi/mob/ClientInterface$Companion;", "", "()V", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}