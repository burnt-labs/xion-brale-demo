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
 * Main error type for the mob library
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u000f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u0000 \b2\u00060\u0001j\u0002`\u0002:\u000e\u0006\u0007\b\t\n\u000b\f\r\u000e\u000f\u0010\u0011\u0012\u0013B\u000f\b\u0004\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\u0002\u0010\u0005\u0082\u0001\r\u0014\u0015\u0016\u0017\u0018\u0019\u001a\u001b\u001c\u001d\u001e\u001f \u00a8\u0006!"}, d2 = {"Luniffi/mob/MobException;", "Ljava/lang/Exception;", "Lkotlin/Exception;", "message", "", "(Ljava/lang/String;)V", "Account", "Address", "ErrorHandler", "GasEstimation", "Generic", "InsufficientFunds", "InvalidInput", "KeyDerivation", "Network", "Rpc", "Serialization", "Signing", "Timeout", "Transaction", "Luniffi/mob/MobException$Account;", "Luniffi/mob/MobException$Address;", "Luniffi/mob/MobException$GasEstimation;", "Luniffi/mob/MobException$Generic;", "Luniffi/mob/MobException$InsufficientFunds;", "Luniffi/mob/MobException$InvalidInput;", "Luniffi/mob/MobException$KeyDerivation;", "Luniffi/mob/MobException$Network;", "Luniffi/mob/MobException$Rpc;", "Luniffi/mob/MobException$Serialization;", "Luniffi/mob/MobException$Signing;", "Luniffi/mob/MobException$Timeout;", "Luniffi/mob/MobException$Transaction;", "app_debug"})
public abstract class MobException extends java.lang.Exception {
    @org.jetbrains.annotations.NotNull()
    public static final uniffi.mob.MobException.ErrorHandler ErrorHandler = null;
    
    private MobException(java.lang.String message) {
        super();
    }
    
    /**
     * Account-related errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Luniffi/mob/MobException$Account;", "Luniffi/mob/MobException;", "message", "", "(Ljava/lang/String;)V", "app_debug"})
    public static final class Account extends uniffi.mob.MobException {
        
        public Account(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
    }
    
    /**
     * Address parsing/generation errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Luniffi/mob/MobException$Address;", "Luniffi/mob/MobException;", "message", "", "(Ljava/lang/String;)V", "app_debug"})
    public static final class Address extends uniffi.mob.MobException {
        
        public Address(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0016\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00022\u0006\u0010\u0005\u001a\u00020\u0006H\u0016\u00a8\u0006\u0007"}, d2 = {"Luniffi/mob/MobException$ErrorHandler;", "Luniffi/mob/UniffiRustCallStatusErrorHandler;", "Luniffi/mob/MobException;", "()V", "lift", "error_buf", "Luniffi/mob/RustBuffer$ByValue;", "app_debug"})
    public static final class ErrorHandler implements uniffi.mob.UniffiRustCallStatusErrorHandler<uniffi.mob.MobException> {
        
        private ErrorHandler() {
            super();
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public uniffi.mob.MobException lift(@org.jetbrains.annotations.NotNull()
        uniffi.mob.RustBuffer.ByValue error_buf) {
            return null;
        }
    }
    
    /**
     * Gas estimation errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Luniffi/mob/MobException$GasEstimation;", "Luniffi/mob/MobException;", "message", "", "(Ljava/lang/String;)V", "app_debug"})
    public static final class GasEstimation extends uniffi.mob.MobException {
        
        public GasEstimation(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
    }
    
    /**
     * Generic error
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Luniffi/mob/MobException$Generic;", "Luniffi/mob/MobException;", "message", "", "(Ljava/lang/String;)V", "app_debug"})
    public static final class Generic extends uniffi.mob.MobException {
        
        public Generic(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
    }
    
    /**
     * Insufficient funds
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Luniffi/mob/MobException$InsufficientFunds;", "Luniffi/mob/MobException;", "message", "", "(Ljava/lang/String;)V", "app_debug"})
    public static final class InsufficientFunds extends uniffi.mob.MobException {
        
        public InsufficientFunds(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
    }
    
    /**
     * Invalid input errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Luniffi/mob/MobException$InvalidInput;", "Luniffi/mob/MobException;", "message", "", "(Ljava/lang/String;)V", "app_debug"})
    public static final class InvalidInput extends uniffi.mob.MobException {
        
        public InvalidInput(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
    }
    
    /**
     * Key derivation errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Luniffi/mob/MobException$KeyDerivation;", "Luniffi/mob/MobException;", "message", "", "(Ljava/lang/String;)V", "app_debug"})
    public static final class KeyDerivation extends uniffi.mob.MobException {
        
        public KeyDerivation(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
    }
    
    /**
     * Network-related errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Luniffi/mob/MobException$Network;", "Luniffi/mob/MobException;", "message", "", "(Ljava/lang/String;)V", "app_debug"})
    public static final class Network extends uniffi.mob.MobException {
        
        public Network(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
    }
    
    /**
     * RPC-related errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Luniffi/mob/MobException$Rpc;", "Luniffi/mob/MobException;", "message", "", "(Ljava/lang/String;)V", "app_debug"})
    public static final class Rpc extends uniffi.mob.MobException {
        
        public Rpc(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
    }
    
    /**
     * Serialization/deserialization errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Luniffi/mob/MobException$Serialization;", "Luniffi/mob/MobException;", "message", "", "(Ljava/lang/String;)V", "app_debug"})
    public static final class Serialization extends uniffi.mob.MobException {
        
        public Serialization(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
    }
    
    /**
     * Signing-related errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Luniffi/mob/MobException$Signing;", "Luniffi/mob/MobException;", "message", "", "(Ljava/lang/String;)V", "app_debug"})
    public static final class Signing extends uniffi.mob.MobException {
        
        public Signing(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
    }
    
    /**
     * Timeout errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Luniffi/mob/MobException$Timeout;", "Luniffi/mob/MobException;", "message", "", "(Ljava/lang/String;)V", "app_debug"})
    public static final class Timeout extends uniffi.mob.MobException {
        
        public Timeout(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
    }
    
    /**
     * Transaction-related errors
     */
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004\u00a8\u0006\u0005"}, d2 = {"Luniffi/mob/MobException$Transaction;", "Luniffi/mob/MobException;", "message", "", "(Ljava/lang/String;)V", "app_debug"})
    public static final class Transaction extends uniffi.mob.MobException {
        
        public Transaction(@org.jetbrains.annotations.NotNull()
        java.lang.String message) {
        }
    }
}