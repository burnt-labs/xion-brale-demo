package com.burnt.xiondemo.data.datasource;

import com.burnt.xiondemo.data.remote.XionOAuthApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class OAuthDataSource_Factory implements Factory<OAuthDataSource> {
  private final Provider<XionOAuthApi> oAuthApiProvider;

  public OAuthDataSource_Factory(Provider<XionOAuthApi> oAuthApiProvider) {
    this.oAuthApiProvider = oAuthApiProvider;
  }

  @Override
  public OAuthDataSource get() {
    return newInstance(oAuthApiProvider.get());
  }

  public static OAuthDataSource_Factory create(Provider<XionOAuthApi> oAuthApiProvider) {
    return new OAuthDataSource_Factory(oAuthApiProvider);
  }

  public static OAuthDataSource newInstance(XionOAuthApi oAuthApi) {
    return new OAuthDataSource(oAuthApi);
  }
}
