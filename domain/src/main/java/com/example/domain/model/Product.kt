package com.example.domain.model

data class Product(
    val barcode: String,
    val name: String,
    val brands: String?,
    val ingredientsText: String?
)