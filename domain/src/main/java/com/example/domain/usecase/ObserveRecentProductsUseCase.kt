package com.example.domain.usecase

import com.example.domain.repo.ProductRepository

class ObserveRecentProductsUseCase(
    private val repo: ProductRepository
) {
    operator fun invoke() = repo.observeRecent()
}