package com.burnt.xiondemo.di

import com.burnt.xiondemo.data.remote.BraleProxyApi
import com.burnt.xiondemo.data.repository.BraleRepository
import com.burnt.xiondemo.data.repository.BraleRepositoryImpl
import com.burnt.xiondemo.util.Constants
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BraleRetrofit

@Module
@InstallIn(SingletonComponent::class)
object BraleNetworkModule {

    @Provides
    @Singleton
    @BraleRetrofit
    fun provideBraleRetrofit(client: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(Constants.BRALE_PROXY_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideBraleProxyApi(@BraleRetrofit retrofit: Retrofit): BraleProxyApi {
        return retrofit.create(BraleProxyApi::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BraleBindingsModule {
    @Binds
    @Singleton
    abstract fun bindBraleRepository(impl: BraleRepositoryImpl): BraleRepository
}
