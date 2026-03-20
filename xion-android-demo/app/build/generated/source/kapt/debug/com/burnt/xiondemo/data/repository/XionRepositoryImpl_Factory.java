package com.burnt.xiondemo.data.repository;

import com.burnt.xiondemo.data.datasource.MobDataSource;
import com.burnt.xiondemo.security.SecureStorage;
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
public final class XionRepositoryImpl_Factory implements Factory<XionRepositoryImpl> {
  private final Provider<MobDataSource> mobDataSourceProvider;

  private final Provider<SecureStorage> secureStorageProvider;

  public XionRepositoryImpl_Factory(Provider<MobDataSource> mobDataSourceProvider,
      Provider<SecureStorage> secureStorageProvider) {
    this.mobDataSourceProvider = mobDataSourceProvider;
    this.secureStorageProvider = secureStorageProvider;
  }

  @Override
  public XionRepositoryImpl get() {
    return newInstance(mobDataSourceProvider.get(), secureStorageProvider.get());
  }

  public static XionRepositoryImpl_Factory create(Provider<MobDataSource> mobDataSourceProvider,
      Provider<SecureStorage> secureStorageProvider) {
    return new XionRepositoryImpl_Factory(mobDataSourceProvider, secureStorageProvider);
  }

  public static XionRepositoryImpl newInstance(MobDataSource mobDataSource,
      SecureStorage secureStorage) {
    return new XionRepositoryImpl(mobDataSource, secureStorage);
  }
}
