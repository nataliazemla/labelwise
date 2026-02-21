package com.example.data.repo

import com.example.core.result.AppResult
import com.example.domain.model.Product
import com.example.domain.repo.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeProductRepository : ProductRepository {
    private val recent = MutableStateFlow(
        listOf(
            Product("5901234123457", "Sample Oat Bar", "DemoBrand", "oats, sugar"),
            Product("2000000000000", "Sample Yogurt", "DemoBrand", "milk, cultures")
        )
    )

    override fun observeRecent(): Flow<List<Product>> = recent

    override suspend fun refreshByBarcode(barcode: String): AppResult<Unit> {
        return AppResult.Success(Unit)
    }
}