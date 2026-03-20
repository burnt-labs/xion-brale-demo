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
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0012\n\u0002\b\u0003\bf\u0018\u0000 \t2\u00020\u0001:\u0001\tJ\b\u0010\u0002\u001a\u00020\u0003H&J\b\u0010\u0004\u001a\u00020\u0003H&J\b\u0010\u0005\u001a\u00020\u0003H&J\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u0007H&\u00a8\u0006\n"}, d2 = {"Luniffi/mob/SignerInterface;", "", "address", "", "addressPrefix", "publicKeyHex", "signBytes", "", "message", "Companion", "app_debug"})
public abstract interface SignerInterface {
    @org.jetbrains.annotations.NotNull()
    public static final uniffi.mob.SignerInterface.Companion Companion = null;
    
    /**
     * Get the signer's address
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String address();
    
    /**
     * Get the address prefix
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String addressPrefix();
    
    /**
     * Get the signer's public key as hex
     */
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String publicKeyHex();
    
    /**
     * Sign arbitrary bytes
     */
    @org.jetbrains.annotations.NotNull()
    public abstract byte[] signBytes(@org.jetbrains.annotations.NotNull()
    byte[] message);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Luniffi/mob/SignerInterface$Companion;", "", "()V", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}