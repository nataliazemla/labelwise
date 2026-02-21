package com.example.labelwise.di

import com.example.data.repo.FakeProductRepository
import com.example.domain.repo.ProductRepository
import com.example.domain.usecase.ObserveRecentProductsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideProductRepository(): ProductRepository = FakeProductRepository()

    @Provides @Singleton
    fun provideObserveRecentProductsUseCase(repo: ProductRepository) =
        ObserveRecentProductsUseCase(repo)
}