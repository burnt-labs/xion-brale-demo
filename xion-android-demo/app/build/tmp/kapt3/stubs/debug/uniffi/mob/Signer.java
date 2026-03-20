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
 * A signer that manages keys and can sign transactions
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\u0012\n\u0002\b\u0005\b\u0016\u0018\u0000 )2\u00020\u00012\u00020\u00022\u00020\u0003:\u0002)*B\u0017\b\u0016\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bB\u000f\b\u0016\u0012\u0006\u0010\t\u001a\u00020\n\u00a2\u0006\u0002\u0010\u000bJ\b\u0010\u0016\u001a\u00020\u0017H\u0016J\b\u0010\u0018\u001a\u00020\u0017H\u0016J<\u0010\u0019\u001a\u0002H\u001a\"\u0004\b\u0000\u0010\u001a2!\u0010\u001b\u001a\u001d\u0012\u0013\u0012\u00110\u0007\u00a2\u0006\f\b\u001d\u0012\b\b\u001e\u0012\u0004\b\b(\u0006\u0012\u0004\u0012\u0002H\u001a0\u001cH\u0080\b\u00f8\u0001\u0000\u00a2\u0006\u0004\b\u001f\u0010 J\b\u0010!\u001a\u00020\"H\u0016J\b\u0010#\u001a\u00020\"H\u0016J\b\u0010$\u001a\u00020\u0017H\u0016J\u0010\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020&H\u0016J\u0006\u0010(\u001a\u00020\u0007R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000e\u001a\u00020\u000fX\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0014\u0010\u0006\u001a\u00020\u0007X\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0007\n\u0005\b\u009920\u0001\u00a8\u0006+"}, d2 = {"Luniffi/mob/Signer;", "Luniffi/mob/Disposable;", "Ljava/lang/AutoCloseable;", "Luniffi/mob/SignerInterface;", "withHandle", "Luniffi/mob/UniffiWithHandle;", "handle", "", "(Luniffi/mob/UniffiWithHandle;J)V", "noHandle", "Luniffi/mob/NoHandle;", "(Luniffi/mob/NoHandle;)V", "callCounter", "Ljava/util/concurrent/atomic/AtomicLong;", "cleanable", "Luniffi/mob/UniffiCleaner$Cleanable;", "getCleanable", "()Luniffi/mob/UniffiCleaner$Cleanable;", "getHandle", "()J", "wasDestroyed", "Ljava/util/concurrent/atomic/AtomicBoolean;", "address", "", "addressPrefix", "callWithHandle", "R", "block", "Lkotlin/Function1;", "Lkotlin/ParameterName;", "name", "callWithHandle$app_debug", "(Lkotlin/jvm/functions/Function1;)Ljava/lang/Object;", "close", "", "destroy", "publicKeyHex", "signBytes", "", "message", "uniffiCloneHandle", "Companion", "UniffiCleanAction", "app_debug"})
public class Signer implements uniffi.mob.Disposable, java.lang.AutoCloseable, uniffi.mob.SignerInterface {
    private final long handle = 0L;
    @org.jetbrains.annotations.NotNull()
    private final uniffi.mob.UniffiCleaner.Cleanable cleanable = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.atomic.AtomicBoolean wasDestroyed = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.atomic.AtomicLong callCounter = null;
    @org.jetbrains.annotations.NotNull()
    public static final uniffi.mob.Signer.Companion Companion = null;
    
    /**
     * @suppress
     */
    @kotlin.Suppress(names = {"UNUSED_PARAMETER"})
    public Signer(@org.jetbrains.annotations.NotNull()
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
    public Signer(@org.jetbrains.annotations.NotNull()
    uniffi.mob.NoHandle noHandle) {
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
     * Get the signer's address
     */
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String address() {
        return null;
    }
    
    /**
     * Get the address prefix
     */
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String addressPrefix() {
        return null;
    }
    
    /**
     * Get the signer's public key as hex
     */
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String publicKeyHex() {
        return null;
    }
    
    /**
     * Sign arbitrary bytes
     */
    @java.lang.Override()
    @kotlin.jvm.Throws(exceptionClasses = {uniffi.mob.MobException.class})
    @org.jetbrains.annotations.NotNull()
    public byte[] signBytes(@org.jetbrains.annotations.NotNull()
    byte[] message) throws uniffi.mob.MobException {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J \u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00062\b\u0010\b\u001a\u0004\u0018\u00010\u0006\u00a8\u0006\t"}, d2 = {"Luniffi/mob/Signer$Companion;", "", "()V", "fromMnemonic", "Luniffi/mob/Signer;", "mnemonic", "", "addressPrefix", "derivationPath", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        /**
         * Create a new signer from a mnemonic phrase
         */
        @kotlin.jvm.Throws(exceptionClasses = {uniffi.mob.MobException.class})
        @org.jetbrains.annotations.NotNull()
        public final uniffi.mob.Signer fromMnemonic(@org.jetbrains.annotations.NotNull()
        java.lang.String mnemonic, @org.jetbrains.annotations.NotNull()
        java.lang.String addressPrefix, @org.jetbrains.annotations.Nullable()
        java.lang.String derivationPath) throws uniffi.mob.MobException {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0005\u001a\u00020\u0006H\u0016R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Luniffi/mob/Signer$UniffiCleanAction;", "Ljava/lang/Runnable;", "handle", "", "(J)V", "run", "", "app_debug"})
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