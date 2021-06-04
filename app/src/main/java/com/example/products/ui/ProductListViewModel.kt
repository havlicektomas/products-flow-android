package com.example.products.ui

import androidx.lifecycle.*
import com.example.products.ProductRepository
import com.example.products.domain.NoCategory
import com.example.products.domain.Product
import com.example.products.domain.ProductCategory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

class ProductListViewModel internal constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _snackbar = MutableLiveData<String?>()
    val snackbar: LiveData<String?>
        get() = _snackbar

    private val _spinner = MutableLiveData<Boolean>(false)
    val spinner: LiveData<Boolean>
        get() = _spinner

    private val productCategory = MutableStateFlow<ProductCategory>(NoCategory)

    @ExperimentalCoroutinesApi
    val products: LiveData<List<Product>> = productCategory.flatMapLatest { category ->
        if (category == NoCategory) {
            productRepository.products
        } else {
            productRepository.getProductsWithCategory(category)
        }
    }.asLiveData()

    init {
        clearProductCategoryNumber()

        loadDataFor(productCategory) {
            if (it == NoCategory) {
                productRepository.tryUpdateRecentProductsCache()
            } else {
                productRepository.tryUpdateRecentProductsForCategoryCache(it)
            }
        }

//        productCategory.mapLatest { category ->
//            _spinner.value = true
//            if (category == NoCategory) {
//                productRepository.tryUpdateRecentProductsCache()
//            } else {
//                productRepository.tryUpdateRecentProductsForCategoryCache(category)
//            }
//        }
//        .onEach {  _spinner.value = false }
//        .catch { throwable ->  _snackbar.value = throwable.message  }
//        .launchIn(viewModelScope)
    }

    fun clearProductCategoryNumber() {
        productCategory.value = NoCategory
    }

    fun setProductCategoryNumber(num: Int) {
        productCategory.value = ProductCategory(num)
    }

    fun isFiltered() = productCategory.value != NoCategory

    fun onSnackbarShown() {
        _snackbar.value = null
    }

    private fun <T> loadDataFor(source: StateFlow<T>, block: suspend (T) -> Unit) {
        source.mapLatest { category ->
            _spinner.value = true
            block(category)
        }
            .onEach {  _spinner.value = false }
            .catch { throwable ->  _snackbar.value = throwable.message  }
            .launchIn(viewModelScope)
    }

}