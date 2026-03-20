package com.burnt.xiondemo.data.datasource;

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
public final class RealMobDataSource_Factory implements Factory<RealMobDataSource> {
  @Override
  public RealMobDataSource get() {
    return newInstance();
  }

  public static RealMobDataSource_Factory create() {
    return InstanceHolder.INSTANCE;
  }

  public static RealMobDataSource newInstance() {
    return new RealMobDataSource();
  }

  private static final class InstanceHolder {
    private static final RealMobDataSource_Factory INSTANCE = new RealMobDataSource_Factory();
  }
}
