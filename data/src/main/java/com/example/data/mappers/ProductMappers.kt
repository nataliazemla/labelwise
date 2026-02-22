package com.example.data.mappers

import com.example.data.db.ProductEntity
import com.example.data.network.ProductDto
import com.example.domain.model.Product

fun ProductEntity.toDomain(): Product =
    Product(
        barcode = barcode,
        name = name,
        brands = brands,
        ingredientsText = ingredientsText
    )

fun ProductDto.toEntity(nowEpochMs: Long): ProductEntity? {
    val barcode = (code ?: "").trim()
    if (barcode.isBlank()) return null

    return ProductEntity(
        barcode = barcode,
        name = (productName ?: "").trim(),
        brands = brands?.trim(),
        ingredientsText = ingredientsText?.trim(),
        updatedAtEpochMs = nowEpochMs
    )
}