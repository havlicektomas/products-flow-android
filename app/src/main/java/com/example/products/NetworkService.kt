package com.example.products

import com.example.products.domain.Product
import com.example.products.domain.ProductCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class NetworkService {

    suspend fun allProducts(): List<Product> = withContext(Dispatchers.Default) {
        delay(2000)
        fakeProducts
    }

    suspend fun productsByCategory(category: ProductCategory): List<Product> = withContext(Dispatchers.Default) {
        delay(2000)
        fakeProducts.filter { it.categoryNumber == category.number }
    }

    suspend fun customProductSortOrder(): List<String> = withContext(Dispatchers.Default) {
        delay(2000)
        customSortOrder
    }
}

val fakeProducts = listOf(
    Product("111", "A", "This is a product description", -1, "https://www.alstedefarms.com/wp-content/plugins/woocommerce/assets/images/placeholder.png"),
    Product("112", "B", "This is a product description", 9, "https://www.alstedefarms.com/wp-content/plugins/woocommerce/assets/images/placeholder.png"),
    Product("113", "C", "This is a product description", -1, "https://www.alstedefarms.com/wp-content/plugins/woocommerce/assets/images/placeholder.png"),
    Product("114", "D", "This is a product description", 9, "https://www.alstedefarms.com/wp-content/plugins/woocommerce/assets/images/placeholder.png"),
    Product("115", "E", "This is a product description", -1, "https://www.alstedefarms.com/wp-content/plugins/woocommerce/assets/images/placeholder.png"),
    Product("116", "F", "This is a product description", 9, "https://www.alstedefarms.com/wp-content/plugins/woocommerce/assets/images/placeholder.png"),
    Product("117", "G", "This is a product description", -1, "https://www.alstedefarms.com/wp-content/plugins/woocommerce/assets/images/placeholder.png"),
    Product("118", "H", "This is a product description", 9, "https://www.alstedefarms.com/wp-content/plugins/woocommerce/assets/images/placeholder.png"),
    Product("119", "I", "This is a product description", -1, "https://www.alstedefarms.com/wp-content/plugins/woocommerce/assets/images/placeholder.png"),
    Product("120", "J", "This is a product description", -1, "https://www.alstedefarms.com/wp-content/plugins/woocommerce/assets/images/placeholder.png"),
)

val customSortOrder = listOf("117", "118")