package com.burnt.xiondemo.di;

import com.burnt.xiondemo.data.datasource.MobDataSource;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class AppModule_ProvideMobDataSourceFactory implements Factory<MobDataSource> {
  @Override
  public MobDataSource get() {
    return provideMobDataSource();
  }

  public static AppModule_ProvideMobDataSourceFactory create() {
    return InstanceHolder.INSTANCE;
  }

  public static MobDataSource provideMobDataSource() {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideMobDataSource());
  }

  private static final class InstanceHolder {
    private static final AppModule_ProvideMobDataSourceFactory INSTANCE = new AppModule_ProvideMobDataSourceFactory();
  }
}
