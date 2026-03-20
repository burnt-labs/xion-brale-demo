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
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0094\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0012\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0007\b\u0016\u0018\u0000 E2\u00020\u00012\u00020\u00022\u00020\u0003:\u0002EFB\u0017\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bB\u000f\b\u0016\u0012\u0006\u0010\t\u001a\u00020\n\u00a2\u0006\u0002\u0010\u000bB\u000f\b\u0016\u0012\u0006\u0010\f\u001a\u00020\r\u00a2\u0006\u0002\u0010\u000eJ\u0010\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001cH\u0016J<\u0010\u001d\u001a\u0002H\u001e\"\u0004\b\u0000\u0010\u001e2!\u0010\u001f\u001a\u001d\u0012\u0013\u0012\u00110\u0007\u00a2\u0006\f\b!\u0012\b\b\"\u0012\u0004\b\b(\u0006\u0012\u0004\u0012\u0002H\u001e0 H\u0080\b\u00f8\u0001\u0000\u00a2\u0006\u0004\b#\u0010$J\b\u0010%\u001a\u00020\u001aH\u0016J\b\u0010&\u001a\u00020\u001aH\u0016JD\u0010\'\u001a\u00020(2\u0006\u0010)\u001a\u00020*2\u0006\u0010+\u001a\u00020,2\f\u0010-\u001a\b\u0012\u0004\u0012\u00020/0.2\b\u00100\u001a\u0004\u0018\u00010*2\b\u00101\u001a\u0004\u0018\u00010*2\b\u00102\u001a\u0004\u0018\u00010*H\u0016J\u0010\u00103\u001a\u0002042\u0006\u00105\u001a\u00020*H\u0016J\u0016\u00106\u001a\b\u0012\u0004\u0012\u00020/0.2\u0006\u00105\u001a\u00020*H\u0016J\u0018\u00107\u001a\u00020/2\u0006\u00105\u001a\u00020*2\u0006\u00108\u001a\u00020*H\u0016J\b\u00109\u001a\u00020*H\u0016J\u0015\u0010:\u001a\u00020;H\u0016\u00f8\u0001\u0001\u00f8\u0001\u0002\u00a2\u0006\u0004\b<\u0010\u0016J\u0010\u0010=\u001a\u00020(2\u0006\u0010>\u001a\u00020*H\u0016J\b\u0010?\u001a\u00020@H\u0016J<\u0010A\u001a\u00020(2\u0006\u0010B\u001a\u00020*2\f\u0010C\u001a\b\u0012\u0004\u0012\u00020/0.2\b\u00100\u001a\u0004\u0018\u00010*2\b\u00101\u001a\u0004\u0018\u00010*2\b\u00102\u001a\u0004\u0018\u00010*H\u0016J\u0006\u0010D\u001a\u00020\u0007R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\u00020\u0012X\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0014\u0010\u0006\u001a\u00020\u0007X\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0016R\u000e\u0010\u0017\u001a\u00020\u0018X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0012\n\u0005\b\u009920\u0001\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006G"}, d2 = {"Luniffi/mob/Client;", "Luniffi/mob/Disposable;", "Ljava/lang/AutoCloseable;", "Luniffi/mob/ClientInterface;", "withHandle", "Luniffi/mob/UniffiWithHandle;", "handle", "", "(Luniffi/mob/UniffiWithHandle;J)V", "noHandle", "Luniffi/mob/NoHandle;", "(Luniffi/mob/NoHandle;)V", "config", "Luniffi/mob/ChainConfig;", "(Luniffi/mob/ChainConfig;)V", "callCounter", "Ljava/util/concurrent/atomic/AtomicLong;", "cleanable", "Luniffi/mob/UniffiCleaner$Cleanable;", "getCleanable", "()Luniffi/mob/UniffiCleaner$Cleanable;", "getHandle", "()J", "wasDestroyed", "Ljava/util/concurrent/atomic/AtomicBoolean;", "attachSigner", "", "signer", "Luniffi/mob/Signer;", "callWithHandle", "R", "block", "Lkotlin/Function1;", "Lkotlin/ParameterName;", "name", "callWithHandle$app_debug", "(Lkotlin/jvm/functions/Function1;)Ljava/lang/Object;", "close", "destroy", "executeContract", "Luniffi/mob/TxResponse;", "contractAddress", "", "msg", "", "funds", "", "Luniffi/mob/Coin;", "granter", "feeGranter", "memo", "getAccount", "Luniffi/mob/AccountInfo;", "address", "getAllBalances", "getBalance", "denom", "getChainId", "getHeight", "Lkotlin/ULong;", "getHeight-s-VKNKU", "getTx", "hash", "isSynced", "", "send", "toAddress", "amount", "uniffiCloneHandle", "Companion", "UniffiCleanAction", "app_debug"})
public class Client implements uniffi.mob.Disposable, java.lang.AutoCloseable, uniffi.mob.ClientInterface {
    private final long handle = 0L;
    @org.jetbrains.annotations.NotNull()
    private final uniffi.mob.UniffiCleaner.Cleanable cleanable = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.atomic.AtomicBoolean wasDestroyed = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.atomic.AtomicLong callCounter = null;
    @org.jetbrains.annotations.NotNull()
    public static final uniffi.mob.Client.Companion Companion = null;
    
    /**
     * @suppress
     */
    @kotlin.Suppress(names = {"UNUSED_PARAMETER"})
    public Client(@org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiWithHandle withHandle, long handle) {
        super();
    }
    
    /**
     * @suppress
     *
     * This constructor can be used to instantiate a fake object. Only used for tests. Any
     * attempt to actually use an object constructed this way will fail as there is no
     * connected Rust object.
     */
    @kotlin.Suppress(names = {"UNUSED_PARAMETER"})
    public Client(@org.jetbrains.annotations.NotNull()
    uniffi.mob.NoHandle noHandle) {
        super();
    }
    
    /**
     * Create a new RPC client (synchronous wrapper for FFI)
     */
    public Client(@org.jetbrains.annotations.NotNull()
    uniffi.mob.ChainConfig config) {
        super();
    }
    
    protected final long getHandle() {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final uniffi.mob.UniffiCleaner.Cleanable getCleanable() {
        return null;
    }
    
    @java.lang.Override()
    public void destroy() {
    }
    
    @java.lang.Override()
    @kotlin.jvm.Synchronized()
    public synchronized void close() {
    }
    
    public final <R extends java.lang.Object>R callWithHandle$app_debug(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.Long, ? extends R> block) {
        return null;
    }
    
    /**
     * @suppress
     */
    public final long uniffiCloneHandle() {
        return 0L;
    }
    
    /**
     * Attach a signer to the client
     */
    @java.lang.Override()
    @kotlin.jvm.Throws(exceptionClasses = {uniffi.mob.MobException.class})
    public void attachSigner(@org.jetbrains.annotations.NotNull()
    uniffi.mob.Signer signer) throws uniffi.mob.MobException {
    }
    
    /**
     * Execute a CosmWasm contract (synchronous wrapper).
     * Mirrors xion.js GranteeSignerClient behavior:
     * - If `granter` is set, wraps MsgExecuteContract in MsgExec (authz),
     * using the granter as the contract sender and the signer as the grantee.
     * - If `fee_granter` is set, the fee granter (e.g. treasury) pays gas.
     */
    @java.lang.Override()
    @kotlin.jvm.Throws(exceptionClasses = {uniffi.mob.MobException.class})
    @org.jetbrains.annotations.NotNull()
    public uniffi.mob.TxResponse executeContract(@org.jetbrains.annotations.NotNull()
    java.lang.String contractAddress, @org.jetbrains.annotations.NotNull()
    byte[] msg, @org.jetbrains.annotations.NotNull()
    java.util.List<uniffi.mob.Coin> funds, @org.jetbrains.annotations.Nullable()
    java.lang.String granter, @org.jetbrains.annotations.Nullable()
    java.lang.String feeGranter, @org.jetbrains.annotations.Nullable()
    java.lang.String memo) {
        return null;
    }
    
    /**
     * Query account information (synchronous wrapper)
     */
    @java.lang.Override()
    @kotlin.jvm.Throws(exceptionClasses = {uniffi.mob.MobException.class})
    @org.jetbrains.annotations.NotNull()
    public uniffi.mob.AccountInfo getAccount(@org.jetbrains.annotations.NotNull()
    java.lang.String address) throws uniffi.mob.MobException {
        return null;
    }
    
    /**
     * Query all balances for an address (synchronous wrapper)
     */
    @java.lang.Override()
    @kotlin.jvm.Throws(exceptionClasses = {uniffi.mob.MobException.class})
    @org.jetbrains.annotations.NotNull()
    public java.util.List<uniffi.mob.Coin> getAllBalances(@org.jetbrains.annotations.NotNull()
    java.lang.String address) {
        return null;
    }
    
    /**
     * Query account balance (synchronous wrapper)
     */
    @java.lang.Override()
    @kotlin.jvm.Throws(exceptionClasses = {uniffi.mob.MobException.class})
    @org.jetbrains.annotations.NotNull()
    public uniffi.mob.Coin getBalance(@org.jetbrains.annotations.NotNull()
    java.lang.String address, @org.jetbrains.annotations.NotNull()
    java.lang.String denom) throws uniffi.mob.MobException {
        return null;
    }
    
    /**
     * Get chain ID (synchronous wrapper)
     */
    @java.lang.Override()
    @kotlin.jvm.Throws(exceptionClasses = {uniffi.mob.MobException.class})
    @org.jetbrains.annotations.NotNull()
    public java.lang.String getChainId() throws uniffi.mob.MobException {
        return null;
    }
    
    /**
     * Query transaction by hash (synchronous wrapper)
     */
    @java.lang.Override()
    @kotlin.jvm.Throws(exceptionClasses = {uniffi.mob.MobException.class})
    @org.jetbrains.annotations.NotNull()
    public uniffi.mob.TxResponse getTx(@org.jetbrains.annotations.NotNull()
    java.lang.String hash) throws uniffi.mob.MobException {
        return null;
    }
    
    /**
     * Check if the node is synced (synchronous wrapper)
     */
    @java.lang.Override()
    @kotlin.jvm.Throws(exceptionClasses = {uniffi.mob.MobException.class})
    public boolean isSynced() throws uniffi.mob.MobException {
        return false;
    }
    
    /**
     * Send tokens to a recipient (synchronous wrapper).
     * If `granter` is set, wraps MsgSend in MsgExec (authz),
     * using the granter as the sender and the signer as the grantee.
     * If `fee_granter` is set, the fee granter (e.g. treasury) pays gas.
     */
    @java.lang.Override()
    @kotlin.jvm.Throws(exceptionClasses = {uniffi.mob.MobException.class})
    @org.jetbrains.annotations.NotNull()
    public uniffi.mob.TxResponse send(@org.jetbrains.annotations.NotNull()
    java.lang.String toAddress, @org.jetbrains.annotations.NotNull()
    java.util.List<uniffi.mob.Coin> amount, @org.jetbrains.annotations.Nullable()
    java.lang.String granter, @org.jetbrains.annotations.Nullable()
    java.lang.String feeGranter, @org.jetbrains.annotations.Nullable()
    java.lang.String memo) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0016\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\b\u00a8\u0006\t"}, d2 = {"Luniffi/mob/Client$Companion;", "", "()V", "newWithSigner", "Luniffi/mob/Client;", "config", "Luniffi/mob/ChainConfig;", "signer", "Luniffi/mob/Signer;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Create a new RPC client with a signer attached (synchronous wrapper for FFI)
         */
        @kotlin.jvm.Throws(exceptionClasses = {uniffi.mob.MobException.class})
        @org.jetbrains.annotations.NotNull()
        public final uniffi.mob.Client newWithSigner(@org.jetbrains.annotations.NotNull()
        uniffi.mob.ChainConfig config, @org.jetbrains.annotations.NotNull()
        uniffi.mob.Signer signer) throws uniffi.mob.MobException {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0005\u001a\u00020\u0006H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Luniffi/mob/Client$UniffiCleanAction;", "Ljava/lang/Runnable;", "handle", "", "(J)V", "run", "", "app_debug"})
    static final class UniffiCleanAction implements java.lang.Runnable {
        private final long handle = 0L;
        
        public UniffiCleanAction(long handle) {
            super();
        }
        
        @java.lang.Override()
        public void run() {
        }
    }
}