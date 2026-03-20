package com.burnt.xiondemo.di

import com.burnt.xiondemo.data.datasource.MobDataSource
import com.burnt.xiondemo.data.datasource.RealMobDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMobDataSource(): MobDataSource {
        return RealMobDataSource()
    }
}
