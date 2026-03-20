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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\n\n\u0002\b\u0011\b\u00c0\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\t\u0010\u0003\u001a\u00020\u0004H\u0086 J\t\u0010\u0005\u001a\u00020\u0006H\u0086 J\t\u0010\u0007\u001a\u00020\u0006H\u0086 J\t\u0010\b\u001a\u00020\u0006H\u0086 J\t\u0010\t\u001a\u00020\u0006H\u0086 J\t\u0010\n\u001a\u00020\u0006H\u0086 J\t\u0010\u000b\u001a\u00020\u0006H\u0086 J\t\u0010\f\u001a\u00020\u0006H\u0086 J\t\u0010\r\u001a\u00020\u0006H\u0086 J\t\u0010\u000e\u001a\u00020\u0006H\u0086 J\t\u0010\u000f\u001a\u00020\u0006H\u0086 J\t\u0010\u0010\u001a\u00020\u0006H\u0086 J\t\u0010\u0011\u001a\u00020\u0006H\u0086 J\t\u0010\u0012\u001a\u00020\u0006H\u0086 J\t\u0010\u0013\u001a\u00020\u0006H\u0086 J\t\u0010\u0014\u001a\u00020\u0006H\u0086 J\t\u0010\u0015\u001a\u00020\u0006H\u0086 J\t\u0010\u0016\u001a\u00020\u0006H\u0086 \u00a8\u0006\u0017"}, d2 = {"Luniffi/mob/IntegrityCheckingUniffiLib;", "", "()V", "ffi_mob_uniffi_contract_version", "", "uniffi_mob_checksum_constructor_client_new", "", "uniffi_mob_checksum_constructor_client_new_with_signer", "uniffi_mob_checksum_constructor_signer_from_mnemonic", "uniffi_mob_checksum_method_client_attach_signer", "uniffi_mob_checksum_method_client_execute_contract", "uniffi_mob_checksum_method_client_get_account", "uniffi_mob_checksum_method_client_get_all_balances", "uniffi_mob_checksum_method_client_get_balance", "uniffi_mob_checksum_method_client_get_chain_id", "uniffi_mob_checksum_method_client_get_height", "uniffi_mob_checksum_method_client_get_tx", "uniffi_mob_checksum_method_client_is_synced", "uniffi_mob_checksum_method_client_send", "uniffi_mob_checksum_method_signer_address", "uniffi_mob_checksum_method_signer_address_prefix", "uniffi_mob_checksum_method_signer_public_key_hex", "uniffi_mob_checksum_method_signer_sign_bytes", "app_debug"})
public final class IntegrityCheckingUniffiLib {
    @org.jetbrains.annotations.NotNull()
    public static final uniffi.mob.IntegrityCheckingUniffiLib INSTANCE = null;
    
    private IntegrityCheckingUniffiLib() {
        super();
    }
    
    public final native short uniffi_mob_checksum_method_client_attach_signer() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_method_client_execute_contract() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_method_client_get_account() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_method_client_get_all_balances() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_method_client_get_balance() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_method_client_get_chain_id() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_method_client_get_height() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_method_client_get_tx() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_method_client_is_synced() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_method_client_send() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_method_signer_address() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_method_signer_address_prefix() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_method_signer_public_key_hex() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_method_signer_sign_bytes() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_constructor_client_new() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_constructor_client_new_with_signer() {
        return 0;
    }
    
    public final native short uniffi_mob_checksum_constructor_signer_from_mnemonic() {
        return 0;
    }
    
    public final native int ffi_mob_uniffi_contract_version() {
        return 0;
    }
}