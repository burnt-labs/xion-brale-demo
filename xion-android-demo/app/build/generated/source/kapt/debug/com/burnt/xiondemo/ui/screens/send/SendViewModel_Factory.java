package com.burnt.xiondemo.ui.screens.send;

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
public final class SendViewModel_Factory implements Factory<SendViewModel> {
  private final Provider<XionRepository> repositoryProvider;

  public SendViewModel_Factory(Provider<XionRepository> repositoryProvider) {
    this.repositoryProvider = repositoryProvider;
  }

  @Override
  public SendViewModel get() {
    return newInstance(repositoryProvider.get());
  }

  public static SendViewModel_Factory create(Provider<XionRepository> repositoryProvider) {
    return new SendViewModel_Factory(repositoryProvider);
  }

  public static SendViewModel newInstance(XionRepository repository) {
    return new SendViewModel(repository);
  }
}
