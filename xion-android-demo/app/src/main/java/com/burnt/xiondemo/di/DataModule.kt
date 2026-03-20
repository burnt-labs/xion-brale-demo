package com.burnt.xiondemo.di

import com.burnt.xiondemo.data.repository.XionRepository
import com.burnt.xiondemo.data.repository.XionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindXionRepository(impl: XionRepositoryImpl): XionRepository
}
