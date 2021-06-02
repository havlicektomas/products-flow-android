package com.example.products.ui

import androidx.lifecycle.*
import com.example.products.ProductRepository
import com.example.products.domain.NoCategory
import com.example.products.domain.Product
import com.example.products.domain.ProductCategory
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ProductListViewModel internal constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?>
        get() = _snackbar

    private val _spinner = MutableLiveData<Boolean>(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    private val productCategory = MutableLiveData<ProductCategory>(NoCategory)

    val products: LiveData<List<Product>> = productCategory.switchMap { category ->
        if (category == NoCategory) {
            productRepository.products
        } else {
            productRepository.getProductsWithCategory(category)
        }
    }

    init {
        clearProductCategoryNumber()
    }

    fun clearProductCategoryNumber() {
        productCategory.value = NoCategory

        launchDataLoad { productRepository.tryUpdateRecentProductsCache() }
    }

    fun setProductCategoryNumber(num: Int) {
        productCategory.value = ProductCategory(num)

        launchDataLoad { productRepository.tryUpdateRecentProductsForCategoryCache(ProductCategory(num)) }
    }

    fun isFiltered() = productCategory.value != NoCategory

    fun onSnackbarShown() {
        _snackbar.value = null
    }

    private fun launchDataLoad(block: suspend () -> Unit): Job {
        return viewModelScope.launch {
            try {
                _spinner.value = true
                block()
            } catch (error: Throwable) {
                _snackbar.value = error.message
            } finally {
                _spinner.value = false
            }
        }
    }

}