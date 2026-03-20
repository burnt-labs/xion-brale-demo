package com.burnt.xiondemo.ui.screens.connect;

import com.burnt.xiondemo.auth.OAuthManager;
import com.burnt.xiondemo.data.repository.XionRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class ConnectViewModel_Factory implements Factory<ConnectViewModel> {
  private final Provider<XionRepository> repositoryProvider;

  private final Provider<OAuthManager> oAuthManagerProvider;

  public ConnectViewModel_Factory(Provider<XionRepository> repositoryProvider,
      Provider<OAuthManager> oAuthManagerProvider) {
    this.repositoryProvider = repositoryProvider;
    this.oAuthManagerProvider = oAuthManagerProvider;
  }

  @Override
  public ConnectViewModel get() {
    return newInstance(repositoryProvider.get(), oAuthManagerProvider.get());
  }

  public static ConnectViewModel_Factory create(Provider<XionRepository> repositoryProvider,
      Provider<OAuthManager> oAuthManagerProvider) {
    return new ConnectViewModel_Factory(repositoryProvider, oAuthManagerProvider);
  }

  public static ConnectViewModel newInstance(XionRepository repository, OAuthManager oAuthManager) {
    return new ConnectViewModel(repository, oAuthManager);
  }
}
