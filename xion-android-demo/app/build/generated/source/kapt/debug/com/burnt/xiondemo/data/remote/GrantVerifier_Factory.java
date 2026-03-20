package com.burnt.xiondemo.data.remote;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class GrantVerifier_Factory implements Factory<GrantVerifier> {
  private final Provider<OkHttpClient> httpClientProvider;

  public GrantVerifier_Factory(Provider<OkHttpClient> httpClientProvider) {
    this.httpClientProvider = httpClientProvider;
  }

  @Override
  public GrantVerifier get() {
    return newInstance(httpClientProvider.get());
  }

  public static GrantVerifier_Factory create(Provider<OkHttpClient> httpClientProvider) {
    return new GrantVerifier_Factory(httpClientProvider);
  }

  public static GrantVerifier newInstance(OkHttpClient httpClient) {
    return new GrantVerifier(httpClient);
  }
}
