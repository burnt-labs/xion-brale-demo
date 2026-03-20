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
 * Transaction response
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\"\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\b\u0018\u0000 32\u00020\u0001:\u00013B5\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\b\u0012\u0006\u0010\n\u001a\u00020\u000b\u00a2\u0006\u0002\u0010\fJ\t\u0010!\u001a\u00020\u0003H\u00c6\u0003J\u0016\u0010\"\u001a\u00020\u0005H\u00c6\u0003\u00f8\u0001\u0001\u00f8\u0001\u0000\u00a2\u0006\u0004\b#\u0010\u000eJ\t\u0010$\u001a\u00020\u0003H\u00c6\u0003J\u0016\u0010%\u001a\u00020\bH\u00c6\u0003\u00f8\u0001\u0001\u00f8\u0001\u0000\u00a2\u0006\u0004\b&\u0010\u0013J\u0016\u0010\'\u001a\u00020\bH\u00c6\u0003\u00f8\u0001\u0001\u00f8\u0001\u0000\u00a2\u0006\u0004\b(\u0010\u0013J\t\u0010)\u001a\u00020\u000bH\u00c6\u0003JO\u0010*\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\u000bH\u00c6\u0001\u00f8\u0001\u0000\u00a2\u0006\u0004\b+\u0010,J\u0013\u0010-\u001a\u00020.2\b\u0010/\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u00100\u001a\u000201H\u00d6\u0001J\t\u00102\u001a\u00020\u0003H\u00d6\u0001R\"\u0010\u0004\u001a\u00020\u0005X\u0086\u000e\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0010\n\u0002\u0010\u0011\u001a\u0004\b\r\u0010\u000e\"\u0004\b\u000f\u0010\u0010R\"\u0010\t\u001a\u00020\bX\u0086\u000e\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0010\n\u0002\u0010\u0016\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015R\"\u0010\u0007\u001a\u00020\bX\u0086\u000e\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0010\n\u0002\u0010\u0016\u001a\u0004\b\u0017\u0010\u0013\"\u0004\b\u0018\u0010\u0015R\u001a\u0010\n\u001a\u00020\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u0013\"\u0004\b\u001a\u0010\u0015R\u001a\u0010\u0006\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001b\u0010\u001c\"\u0004\b\u001d\u0010\u001eR\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010\u001c\"\u0004\b \u0010\u001e\u0082\u0002\u000b\n\u0005\b\u00a1\u001e0\u0001\n\u0002\b!\u00a8\u00064"}, d2 = {"Luniffi/mob/TxResponse;", "", "txhash", "", "code", "Lkotlin/UInt;", "rawLog", "gasWanted", "Lkotlin/ULong;", "gasUsed", "height", "", "(Ljava/lang/String;ILjava/lang/String;JJJLkotlin/jvm/internal/DefaultConstructorMarker;)V", "getCode-pVg5ArA", "()I", "setCode-WZ4Q5Ns", "(I)V", "I", "getGasUsed-s-VKNKU", "()J", "setGasUsed-VKZWuLQ", "(J)V", "J", "getGasWanted-s-VKNKU", "setGasWanted-VKZWuLQ", "getHeight", "setHeight", "getRawLog", "()Ljava/lang/String;", "setRawLog", "(Ljava/lang/String;)V", "getTxhash", "setTxhash", "component1", "component2", "component2-pVg5ArA", "component3", "component4", "component4-s-VKNKU", "component5", "component5-s-VKNKU", "component6", "copy", "copy-5l_DLcc", "(Ljava/lang/String;ILjava/lang/String;JJJ)Luniffi/mob/TxResponse;", "equals", "", "other", "hashCode", "", "toString", "Companion", "app_debug"})
public final class TxResponse {
    @org.jetbrains.annotations.NotNull()
    private java.lang.String txhash;
    private int code;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String rawLog;
    private long gasWanted;
    private long gasUsed;
    private long height;
    @org.jetbrains.annotations.NotNull()
    public static final uniffi.mob.TxResponse.Companion Companion = null;
    
    private TxResponse(java.lang.String txhash, int code, java.lang.String rawLog, long gasWanted, long gasUsed, long height) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getTxhash() {
        return null;
    }
    
    public final void setTxhash(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRawLog() {
        return null;
    }
    
    public final void setRawLog(@org.jetbrains.annotations.NotNull()
    java.lang.String p0) {
    }
    
    public final long getHeight() {
        return 0L;
    }
    
    public final void setHeight(long p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    public final long component6() {
        return 0L;
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Luniffi/mob/TxResponse$Companion;", "", "()V", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}