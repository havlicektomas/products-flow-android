package com.example.products

import androidx.annotation.AnyThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.example.products.domain.Product
import com.example.products.domain.ProductCategory
import com.example.products.utils.CacheOnSuccess
import com.example.products.utils.ComparablePair
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository private constructor(
    private val productDao: ProductDao,
    private val networkService: NetworkService,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) {

    private var productListSortOrderCache =
        CacheOnSuccess(onErrorFallback = { listOf() }) {
            networkService.customProductSortOrder()
        }

    val products: LiveData<List<Product>> = liveData {
        val productsLiveData = productDao.getProducts()
        val customSortOrder = productListSortOrderCache.getOrAwait()
        emitSource(productsLiveData.map { productList ->
            productList.applySort(customSortOrder)
        })
    }

    /*fun getProductsWithCategory(category: ProductCategory) = liveData {
        val productsWithCategoryLiveData = productDao.getProductsWithCategory(category.number)
        val customSortOrder = productListSortOrderCache.getOrAwait()
        emitSource(productsWithCategoryLiveData.map { productList ->
            productList.applySort(customSortOrder)
        })
    }*/

    fun getProductsWithCategory(category: ProductCategory) =
        productDao.getProductsWithCategory(category.number)
            .switchMap { productList ->
                liveData {
                    val customSortOrder = productListSortOrderCache.getOrAwait()
                    emit(productList.applyMainSafeSort(customSortOrder))
                }
            }

    private fun shouldUpdateProductsCache(): Boolean {
        // suspending function, so you can e.g. check the status of the database here
        return true
    }

    suspend fun tryUpdateRecentProductsCache() {
        if (shouldUpdateProductsCache()) fetchRecentProducts()
    }

    suspend fun tryUpdateRecentProductsForCategoryCache(category: ProductCategory) {
        if (shouldUpdateProductsCache()) fetchProductsForCategory(category)
    }

    private suspend fun fetchRecentProducts() {
        val products = networkService.allProducts()
        productDao.insertAll(products)
    }

    private suspend fun fetchProductsForCategory(category: ProductCategory) {
        val products = networkService.productsByCategory(category)
        productDao.insertAll(products)
    }

    private fun List<Product>.applySort(customSortOrder: List<String>): List<Product> {
        return sortedBy { product ->
            val positionForItem = customSortOrder.indexOf(product.productId).let { order ->
                if (order > -1) order else Int.MAX_VALUE
            }
            ComparablePair(positionForItem, product.name)
        }
    }

    @AnyThread
    suspend fun List<Product>.applyMainSafeSort(customSortOrder: List<String>) =
        withContext(defaultDispatcher) {
            this@applyMainSafeSort.applySort(customSortOrder)
        }

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: ProductRepository? = null

        fun getInstance(productDao: ProductDao, networkService: NetworkService) =
            instance ?: synchronized(this) {
                instance ?: ProductRepository(productDao, networkService).also { instance = it }
            }
    }
}