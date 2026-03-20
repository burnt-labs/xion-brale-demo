package com.burnt.xiondemo.auth;

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
public final class OAuthManager_Factory implements Factory<OAuthManager> {
  private final Provider<Context> contextProvider;

  public OAuthManager_Factory(Provider<Context> contextProvider) {
    this.contextProvider = contextProvider;
  }

  @Override
  public OAuthManager get() {
    return newInstance(contextProvider.get());
  }

  public static OAuthManager_Factory create(Provider<Context> contextProvider) {
    return new OAuthManager_Factory(contextProvider);
  }

  public static OAuthManager newInstance(Context context) {
    return new OAuthManager(context);
  }
}
