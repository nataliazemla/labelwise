package com.example.data.di

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Room
import com.example.data.db.LabelWiseDatabase
import com.example.data.db.ProductDao
import com.example.data.network.OpenFoodFactsApi
import com.example.data.repo.OfflineFirstProductRepository
import com.example.domain.repo.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideClock(): Clock = Clock.systemUTC()

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LabelWiseDatabase =
        Room.databaseBuilder(context, LabelWiseDatabase::class.java, "labelwise.db")
            .build()

    @Provides
    fun provideProductDao(db: LabelWiseDatabase): ProductDao = db.productDao()

    @Provides
    @Singleton
    fun provideUserAgentInterceptor(): Interceptor =
        Interceptor { chain ->
            val req = chain.request()
            val newReq = req.newBuilder()

                .header("User-Agent", "LabelWise/1.0 (Android; +https://github.com/nataliazemla/labelwise)")
                .build()
            chain.proceed(newReq)
        }

    @Provides
    @Singleton
    fun provideOkHttp(userAgent: Interceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(userAgent)
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttp: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://world.openfoodfacts.org/")
            .client(okHttp)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideOpenFoodFactsApi(retrofit: Retrofit): OpenFoodFactsApi =
        retrofit.create(OpenFoodFactsApi::class.java)

    @RequiresApi(Build.VERSION_CODES.O)
    @Provides
    @Singleton
    fun provideProductRepository(
        dao: ProductDao,
        api: OpenFoodFactsApi,
        clock: Clock
    ): ProductRepository = OfflineFirstProductRepository(dao, api, clock)
}