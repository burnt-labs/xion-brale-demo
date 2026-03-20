package com.burnt.xiondemo.security;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class SecureStorage_Factory implements Factory<SecureStorage> {
  private final Provider<Context> contextProvider;

  private final Provider<KeyStoreManager> keyStoreManagerProvider;

  public SecureStorage_Factory(Provider<Context> contextProvider,
      Provider<KeyStoreManager> keyStoreManagerProvider) {
    this.contextProvider = contextProvider;
    this.keyStoreManagerProvider = keyStoreManagerProvider;
  }

  @Override
  public SecureStorage get() {
    return newInstance(contextProvider.get(), keyStoreManagerProvider.get());
  }

  public static SecureStorage_Factory create(Provider<Context> contextProvider,
      Provider<KeyStoreManager> keyStoreManagerProvider) {
    return new SecureStorage_Factory(contextProvider, keyStoreManagerProvider);
  }

  public static SecureStorage newInstance(Context context, KeyStoreManager keyStoreManager) {
    return new SecureStorage(context, keyStoreManager);
  }
}
