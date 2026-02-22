package com.example.data.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ProductEntity::class],
    version = 1,
    exportSchema = true
)
abstract class LabelWiseDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
}