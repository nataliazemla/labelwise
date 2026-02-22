package com.example.data.network

import com.squareup.moshi.Json

data class ProductResponseDto(
    @Json(name = "status") val status: Int?,
    @Json(name = "product") val product: ProductDto?
)

data class ProductDto(
    @Json(name = "code") val code: String?,
    @Json(name = "product_name") val productName: String?,
    @Json(name = "brands") val brands: String?,
    @Json(name = "ingredients_text") val ingredientsText: String?
)