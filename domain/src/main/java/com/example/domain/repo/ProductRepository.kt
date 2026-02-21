package com.example.domain.repo

import com.example.core.result.AppResult
import com.example.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeRecent(): Flow<List<Product>>
    suspend fun refreshByBarcode(barcode: String): AppResult<Unit>
}