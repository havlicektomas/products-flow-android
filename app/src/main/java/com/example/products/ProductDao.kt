package com.example.products

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.products.domain.Product

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY name")
    fun getProducts(): LiveData<List<Product>>

    @Query("SELECT * FROM products WHERE categoryNumber = :categoryNumber ORDER BY name")
    fun getProductsWithCategory(categoryNumber: Int): LiveData<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<Product>)
}