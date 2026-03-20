package com.burnt.xiondemo.data.remote;

import com.burnt.xiondemo.util.Constants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.json.JSONObject;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J(\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\r2\b\b\u0002\u0010\u000f\u001a\u00020\u0010H\u0086@\u00a2\u0006\u0002\u0010\u0011R\u001b\u0010\u0005\u001a\u00020\u00038BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\b\u0010\t\u001a\u0004\b\u0006\u0010\u0007R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/burnt/xiondemo/data/remote/GrantVerifier;", "", "httpClient", "Lokhttp3/OkHttpClient;", "(Lokhttp3/OkHttpClient;)V", "grantClient", "getGrantClient", "()Lokhttp3/OkHttpClient;", "grantClient$delegate", "Lkotlin/Lazy;", "pollForGrants", "", "granter", "", "grantee", "maxAttempts", "", "(Ljava/lang/String;Ljava/lang/String;ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public final class GrantVerifier {
    @org.jetbrains.annotations.NotNull()
    private final okhttp3.OkHttpClient httpClient = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy grantClient$delegate = null;
    
    @javax.inject.Inject()
    public GrantVerifier(@org.jetbrains.annotations.NotNull()
    okhttp3.OkHttpClient httpClient) {
        super();
    }
    
    private final okhttp3.OkHttpClient getGrantClient() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object pollForGrants(@org.jetbrains.annotations.NotNull()
    java.lang.String granter, @org.jetbrains.annotations.NotNull()
    java.lang.String grantee, int maxAttempts, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
}