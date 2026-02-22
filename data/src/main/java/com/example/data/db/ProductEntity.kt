package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val barcode: String,
    val name: String,
    val brands: String?,
    val ingredientsText: String?,
    val updatedAtEpochMs: Long
)