package com.burnt.xiondemo;

import com.burnt.xiondemo.auth.OAuthManager;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class MainActivity_MembersInjector implements MembersInjector<MainActivity> {
  private final Provider<OAuthManager> oAuthManagerProvider;

  public MainActivity_MembersInjector(Provider<OAuthManager> oAuthManagerProvider) {
    this.oAuthManagerProvider = oAuthManagerProvider;
  }

  public static MembersInjector<MainActivity> create(Provider<OAuthManager> oAuthManagerProvider) {
    return new MainActivity_MembersInjector(oAuthManagerProvider);
  }

  @Override
  public void injectMembers(MainActivity instance) {
    injectOAuthManager(instance, oAuthManagerProvider.get());
  }

  @InjectedFieldSignature("com.burnt.xiondemo.MainActivity.oAuthManager")
  public static void injectOAuthManager(MainActivity instance, OAuthManager oAuthManager) {
    instance.oAuthManager = oAuthManager;
  }
}
