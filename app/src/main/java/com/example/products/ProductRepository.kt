package com.example.products

import androidx.annotation.AnyThread
import com.example.products.domain.Product
import com.example.products.domain.ProductCategory
import com.example.products.utils.CacheOnSuccess
import com.example.products.utils.ComparablePair
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
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

    private val customSort = flow { emit(productListSortOrderCache.getOrAwait()) }

    // In this version both operations run in parallel
    val products: Flow<List<Product>>
        get() = productDao.getProducts()
            // When the result of customSort is available,
            // this will combine it with the latest value from
            // the flow above.  Thus, as long as both `products`
            // and `sortOrder` have an initial value (their
            // flow has emitted at least one value), any change
            // to either `products` or `sortOrder`  will call
            // `products.applySort(sortOrder)`.
            .combine(customSort) { products, sortOrder ->
                products.applySort(sortOrder)
            }
            .flowOn(defaultDispatcher)
            // Modifies the buffer of flowOn to store only the last result.
            // If another result comes in before the previous one is read,
            // it gets overwritten.
            .conflate()


    // In this version both operations run sequentially
    fun getProductsWithCategory(category: ProductCategory): Flow<List<Product>> {
        return productDao.getProductsWithCategory(category.number)
            .map { products ->
                val sortOrderFromNetwork = productListSortOrderCache.getOrAwait()
                val nextValue = products.applyMainSafeSort(sortOrderFromNetwork)
                nextValue
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