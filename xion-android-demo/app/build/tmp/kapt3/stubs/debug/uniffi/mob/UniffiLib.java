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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\f\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\n\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0005\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0013\n\u0002\u0018\u0002\n\u0002\b\u0012\n\u0002\u0018\u0002\n\u0002\b*\b\u00c0\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0011\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u0010\r\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u0010\u000e\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u0010\u000f\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u0010\u0010\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u0010\u0011\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u0010\u0012\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u0010\u0013\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u0010\u0014\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u0010\u0015\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u0010\u0016\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u0010\u0017\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0019\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010\u001e\u001a\u00020\u001f2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010 \u001a\u00020!2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010\"\u001a\u00020\f2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010#\u001a\u00020$2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010%\u001a\u00020&2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010\'\u001a\u00020\u001f2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010(\u001a\u00020!2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010)\u001a\u00020\f2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010*\u001a\u00020$2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010+\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0011\u0010,\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u0010-\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u0010.\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u0010/\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u00100\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u00101\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u00102\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u00103\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u00104\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u00105\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u00106\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J\u0011\u00107\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fH\u0086 J!\u00108\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020\fH\u0086 J!\u0010<\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020\fH\u0086 J!\u0010=\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020\fH\u0086 J!\u0010>\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020\fH\u0086 J!\u0010?\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020\fH\u0086 J!\u0010@\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020\fH\u0086 J!\u0010A\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020\fH\u0086 J!\u0010B\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020\fH\u0086 J!\u0010C\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020\fH\u0086 J!\u0010D\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020\fH\u0086 J!\u0010E\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020\fH\u0086 J!\u0010F\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020\fH\u0086 J\u0019\u0010G\u001a\u00020&2\u0006\u0010H\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010I\u001a\u00020\n2\u0006\u0010J\u001a\u00020&2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010K\u001a\u00020&2\u0006\u0010L\u001a\u00020M2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J!\u0010N\u001a\u00020&2\u0006\u0010J\u001a\u00020&2\u0006\u0010O\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010P\u001a\u00020\f2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010Q\u001a\u00020\f2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010R\u001a\u00020\f2\u0006\u0010S\u001a\u00020&2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J!\u0010T\u001a\u00020\f2\u0006\u0010S\u001a\u00020&2\u0006\u0010U\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J)\u0010V\u001a\u00020\f2\u0006\u0010W\u001a\u00020&2\u0006\u0010X\u001a\u00020&2\u0006\u0010Y\u001a\u00020&2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010Z\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010[\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J!\u0010\\\u001a\u00020\n2\u0006\u0010]\u001a\u00020\f2\u0006\u0010U\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 JI\u0010^\u001a\u00020&2\u0006\u0010]\u001a\u00020\f2\u0006\u0010_\u001a\u00020&2\u0006\u0010`\u001a\u00020&2\u0006\u0010a\u001a\u00020&2\u0006\u0010b\u001a\u00020&2\u0006\u0010c\u001a\u00020&2\u0006\u0010d\u001a\u00020&2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J!\u0010e\u001a\u00020&2\u0006\u0010]\u001a\u00020\f2\u0006\u0010f\u001a\u00020&2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J!\u0010g\u001a\u00020&2\u0006\u0010]\u001a\u00020\f2\u0006\u0010f\u001a\u00020&2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J)\u0010h\u001a\u00020&2\u0006\u0010]\u001a\u00020\f2\u0006\u0010f\u001a\u00020&2\u0006\u0010i\u001a\u00020&2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010j\u001a\u00020&2\u0006\u0010]\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010k\u001a\u00020\f2\u0006\u0010]\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J!\u0010l\u001a\u00020&2\u0006\u0010]\u001a\u00020\f2\u0006\u0010m\u001a\u00020&2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010n\u001a\u00020$2\u0006\u0010]\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 JA\u0010o\u001a\u00020&2\u0006\u0010]\u001a\u00020\f2\u0006\u0010p\u001a\u00020&2\u0006\u0010q\u001a\u00020&2\u0006\u0010b\u001a\u00020&2\u0006\u0010c\u001a\u00020&2\u0006\u0010d\u001a\u00020&2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010r\u001a\u00020&2\u0006\u0010]\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010s\u001a\u00020&2\u0006\u0010]\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J\u0019\u0010t\u001a\u00020&2\u0006\u0010]\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 J!\u0010u\u001a\u00020&2\u0006\u0010]\u001a\u00020\f2\u0006\u0010v\u001a\u00020&2\u0006\u0010\u001a\u001a\u00020\u001bH\u0086 R\u001b\u0010\u0003\u001a\u00020\u00048@X\u0080\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006w"}, d2 = {"Luniffi/mob/UniffiLib;", "", "()V", "CLEANER", "Luniffi/mob/UniffiCleaner;", "getCLEANER$app_debug", "()Luniffi/mob/UniffiCleaner;", "CLEANER$delegate", "Lkotlin/Lazy;", "ffi_mob_rust_future_cancel_f32", "", "handle", "", "ffi_mob_rust_future_cancel_f64", "ffi_mob_rust_future_cancel_i16", "ffi_mob_rust_future_cancel_i32", "ffi_mob_rust_future_cancel_i64", "ffi_mob_rust_future_cancel_i8", "ffi_mob_rust_future_cancel_rust_buffer", "ffi_mob_rust_future_cancel_u16", "ffi_mob_rust_future_cancel_u32", "ffi_mob_rust_future_cancel_u64", "ffi_mob_rust_future_cancel_u8", "ffi_mob_rust_future_cancel_void", "ffi_mob_rust_future_complete_f32", "", "uniffi_out_err", "Luniffi/mob/UniffiRustCallStatus;", "ffi_mob_rust_future_complete_f64", "", "ffi_mob_rust_future_complete_i16", "", "ffi_mob_rust_future_complete_i32", "", "ffi_mob_rust_future_complete_i64", "ffi_mob_rust_future_complete_i8", "", "ffi_mob_rust_future_complete_rust_buffer", "Luniffi/mob/RustBuffer$ByValue;", "ffi_mob_rust_future_complete_u16", "ffi_mob_rust_future_complete_u32", "ffi_mob_rust_future_complete_u64", "ffi_mob_rust_future_complete_u8", "ffi_mob_rust_future_complete_void", "ffi_mob_rust_future_free_f32", "ffi_mob_rust_future_free_f64", "ffi_mob_rust_future_free_i16", "ffi_mob_rust_future_free_i32", "ffi_mob_rust_future_free_i64", "ffi_mob_rust_future_free_i8", "ffi_mob_rust_future_free_rust_buffer", "ffi_mob_rust_future_free_u16", "ffi_mob_rust_future_free_u32", "ffi_mob_rust_future_free_u64", "ffi_mob_rust_future_free_u8", "ffi_mob_rust_future_free_void", "ffi_mob_rust_future_poll_f32", "callback", "Luniffi/mob/UniffiRustFutureContinuationCallback;", "callbackData", "ffi_mob_rust_future_poll_f64", "ffi_mob_rust_future_poll_i16", "ffi_mob_rust_future_poll_i32", "ffi_mob_rust_future_poll_i64", "ffi_mob_rust_future_poll_i8", "ffi_mob_rust_future_poll_rust_buffer", "ffi_mob_rust_future_poll_u16", "ffi_mob_rust_future_poll_u32", "ffi_mob_rust_future_poll_u64", "ffi_mob_rust_future_poll_u8", "ffi_mob_rust_future_poll_void", "ffi_mob_rustbuffer_alloc", "size", "ffi_mob_rustbuffer_free", "buf", "ffi_mob_rustbuffer_from_bytes", "bytes", "Luniffi/mob/ForeignBytes$ByValue;", "ffi_mob_rustbuffer_reserve", "additional", "uniffi_mob_fn_clone_client", "uniffi_mob_fn_clone_signer", "uniffi_mob_fn_constructor_client_new", "config", "uniffi_mob_fn_constructor_client_new_with_signer", "signer", "uniffi_mob_fn_constructor_signer_from_mnemonic", "mnemonic", "addressPrefix", "derivationPath", "uniffi_mob_fn_free_client", "uniffi_mob_fn_free_signer", "uniffi_mob_fn_method_client_attach_signer", "ptr", "uniffi_mob_fn_method_client_execute_contract", "contractAddress", "msg", "funds", "granter", "feeGranter", "memo", "uniffi_mob_fn_method_client_get_account", "address", "uniffi_mob_fn_method_client_get_all_balances", "uniffi_mob_fn_method_client_get_balance", "denom", "uniffi_mob_fn_method_client_get_chain_id", "uniffi_mob_fn_method_client_get_height", "uniffi_mob_fn_method_client_get_tx", "hash", "uniffi_mob_fn_method_client_is_synced", "uniffi_mob_fn_method_client_send", "toAddress", "amount", "uniffi_mob_fn_method_signer_address", "uniffi_mob_fn_method_signer_address_prefix", "uniffi_mob_fn_method_signer_public_key_hex", "uniffi_mob_fn_method_signer_sign_bytes", "message", "app_debug"})
public final class UniffiLib {
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy CLEANER$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final uniffi.mob.UniffiLib INSTANCE = null;
    
    private UniffiLib() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final uniffi.mob.UniffiCleaner getCLEANER$app_debug() {
        return null;
    }
    
    public final native long uniffi_mob_fn_clone_client(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0L;
    }
    
    public final native void uniffi_mob_fn_free_client(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
    }
    
    public final native long uniffi_mob_fn_constructor_client_new(@org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue config, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0L;
    }
    
    public final native long uniffi_mob_fn_constructor_client_new_with_signer(@org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue config, long signer, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0L;
    }
    
    public final native void uniffi_mob_fn_method_client_attach_signer(long ptr, long signer, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue uniffi_mob_fn_method_client_execute_contract(long ptr, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue contractAddress, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue msg, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue funds, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue granter, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue feeGranter, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue memo, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue uniffi_mob_fn_method_client_get_account(long ptr, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue address, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue uniffi_mob_fn_method_client_get_all_balances(long ptr, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue address, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue uniffi_mob_fn_method_client_get_balance(long ptr, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue address, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue denom, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue uniffi_mob_fn_method_client_get_chain_id(long ptr, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    public final native long uniffi_mob_fn_method_client_get_height(long ptr, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue uniffi_mob_fn_method_client_get_tx(long ptr, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue hash, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    public final native byte uniffi_mob_fn_method_client_is_synced(long ptr, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue uniffi_mob_fn_method_client_send(long ptr, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue toAddress, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue amount, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue granter, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue feeGranter, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue memo, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    public final native long uniffi_mob_fn_clone_signer(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0L;
    }
    
    public final native void uniffi_mob_fn_free_signer(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
    }
    
    public final native long uniffi_mob_fn_constructor_signer_from_mnemonic(@org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue mnemonic, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue addressPrefix, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue derivationPath, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0L;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue uniffi_mob_fn_method_signer_address(long ptr, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue uniffi_mob_fn_method_signer_address_prefix(long ptr, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue uniffi_mob_fn_method_signer_public_key_hex(long ptr, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue uniffi_mob_fn_method_signer_sign_bytes(long ptr, @org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue message, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue ffi_mob_rustbuffer_alloc(long size, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue ffi_mob_rustbuffer_from_bytes(@org.jetbrains.annotations.NotNull()
    uniffi.mob.ForeignBytes.ByValue bytes, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    public final native void ffi_mob_rustbuffer_free(@org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue buf, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue ffi_mob_rustbuffer_reserve(@org.jetbrains.annotations.NotNull()
    uniffi.mob.RustBuffer.ByValue buf, long additional, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    public final native void ffi_mob_rust_future_poll_u8(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustFutureContinuationCallback callback, long callbackData) {
    }
    
    public final native void ffi_mob_rust_future_cancel_u8(long handle) {
    }
    
    public final native void ffi_mob_rust_future_free_u8(long handle) {
    }
    
    public final native byte ffi_mob_rust_future_complete_u8(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0;
    }
    
    public final native void ffi_mob_rust_future_poll_i8(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustFutureContinuationCallback callback, long callbackData) {
    }
    
    public final native void ffi_mob_rust_future_cancel_i8(long handle) {
    }
    
    public final native void ffi_mob_rust_future_free_i8(long handle) {
    }
    
    public final native byte ffi_mob_rust_future_complete_i8(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0;
    }
    
    public final native void ffi_mob_rust_future_poll_u16(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustFutureContinuationCallback callback, long callbackData) {
    }
    
    public final native void ffi_mob_rust_future_cancel_u16(long handle) {
    }
    
    public final native void ffi_mob_rust_future_free_u16(long handle) {
    }
    
    public final native short ffi_mob_rust_future_complete_u16(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0;
    }
    
    public final native void ffi_mob_rust_future_poll_i16(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustFutureContinuationCallback callback, long callbackData) {
    }
    
    public final native void ffi_mob_rust_future_cancel_i16(long handle) {
    }
    
    public final native void ffi_mob_rust_future_free_i16(long handle) {
    }
    
    public final native short ffi_mob_rust_future_complete_i16(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0;
    }
    
    public final native void ffi_mob_rust_future_poll_u32(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustFutureContinuationCallback callback, long callbackData) {
    }
    
    public final native void ffi_mob_rust_future_cancel_u32(long handle) {
    }
    
    public final native void ffi_mob_rust_future_free_u32(long handle) {
    }
    
    public final native int ffi_mob_rust_future_complete_u32(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0;
    }
    
    public final native void ffi_mob_rust_future_poll_i32(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustFutureContinuationCallback callback, long callbackData) {
    }
    
    public final native void ffi_mob_rust_future_cancel_i32(long handle) {
    }
    
    public final native void ffi_mob_rust_future_free_i32(long handle) {
    }
    
    public final native int ffi_mob_rust_future_complete_i32(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0;
    }
    
    public final native void ffi_mob_rust_future_poll_u64(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustFutureContinuationCallback callback, long callbackData) {
    }
    
    public final native void ffi_mob_rust_future_cancel_u64(long handle) {
    }
    
    public final native void ffi_mob_rust_future_free_u64(long handle) {
    }
    
    public final native long ffi_mob_rust_future_complete_u64(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0L;
    }
    
    public final native void ffi_mob_rust_future_poll_i64(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustFutureContinuationCallback callback, long callbackData) {
    }
    
    public final native void ffi_mob_rust_future_cancel_i64(long handle) {
    }
    
    public final native void ffi_mob_rust_future_free_i64(long handle) {
    }
    
    public final native long ffi_mob_rust_future_complete_i64(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0L;
    }
    
    public final native void ffi_mob_rust_future_poll_f32(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustFutureContinuationCallback callback, long callbackData) {
    }
    
    public final native void ffi_mob_rust_future_cancel_f32(long handle) {
    }
    
    public final native void ffi_mob_rust_future_free_f32(long handle) {
    }
    
    public final native float ffi_mob_rust_future_complete_f32(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0.0F;
    }
    
    public final native void ffi_mob_rust_future_poll_f64(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustFutureContinuationCallback callback, long callbackData) {
    }
    
    public final native void ffi_mob_rust_future_cancel_f64(long handle) {
    }
    
    public final native void ffi_mob_rust_future_free_f64(long handle) {
    }
    
    public final native double ffi_mob_rust_future_complete_f64(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return 0.0;
    }
    
    public final native void ffi_mob_rust_future_poll_rust_buffer(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustFutureContinuationCallback callback, long callbackData) {
    }
    
    public final native void ffi_mob_rust_future_cancel_rust_buffer(long handle) {
    }
    
    public final native void ffi_mob_rust_future_free_rust_buffer(long handle) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final native uniffi.mob.RustBuffer.ByValue ffi_mob_rust_future_complete_rust_buffer(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
        return null;
    }
    
    public final native void ffi_mob_rust_future_poll_void(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustFutureContinuationCallback callback, long callbackData) {
    }
    
    public final native void ffi_mob_rust_future_cancel_void(long handle) {
    }
    
    public final native void ffi_mob_rust_future_free_void(long handle) {
    }
    
    public final native void ffi_mob_rust_future_complete_void(long handle, @org.jetbrains.annotations.NotNull()
    uniffi.mob.UniffiRustCallStatus uniffi_out_err) {
    }
}