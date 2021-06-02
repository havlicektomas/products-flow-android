package com.example.products.domain

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey @ColumnInfo(name = "id") val productId: String,
    val name: String,
    val description: String,
    val categoryNumber: Int,
    val imageUrl: String = ""
) {
    override fun toString() = name
}


inline class ProductCategory(val number: Int)
val NoCategory = ProductCategory(-1)