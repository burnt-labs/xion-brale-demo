package com.burnt.xiondemo.di;

import com.burnt.xiondemo.data.remote.XionOAuthApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class NetworkModule_ProvideXionOAuthApiFactory implements Factory<XionOAuthApi> {
  private final Provider<Retrofit> retrofitProvider;

  public NetworkModule_ProvideXionOAuthApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public XionOAuthApi get() {
    return provideXionOAuthApi(retrofitProvider.get());
  }

  public static NetworkModule_ProvideXionOAuthApiFactory create(
      Provider<Retrofit> retrofitProvider) {
    return new NetworkModule_ProvideXionOAuthApiFactory(retrofitProvider);
  }

  public static XionOAuthApi provideXionOAuthApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(NetworkModule.INSTANCE.provideXionOAuthApi(retrofit));
  }
}
