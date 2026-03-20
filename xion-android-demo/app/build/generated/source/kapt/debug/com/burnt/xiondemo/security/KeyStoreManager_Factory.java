package com.burnt.xiondemo.security;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

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
public final class KeyStoreManager_Factory implements Factory<KeyStoreManager> {
  @Override
  public KeyStoreManager get() {
    return newInstance();
  }

  public static KeyStoreManager_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static KeyStoreManager newInstance() {
    return new KeyStoreManager();
  }

  private static final class InstanceHolder {
    private static final KeyStoreManager_Factory INSTANCE = new KeyStoreManager_Factory();
  }
}
