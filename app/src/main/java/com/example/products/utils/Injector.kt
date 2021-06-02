package com.example.products.utils

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.example.products.NetworkService
import com.example.products.ProductRepository
import com.example.products.ui.ProductListViewModelFactory

interface ViewModelFactoryProvider {
    fun provideProductListViewModelFactory(context: Context): ProductListViewModelFactory
}

val Injector: ViewModelFactoryProvider
    get() = currentInjector

private object DefaultViewModelProvider: ViewModelFactoryProvider {
    private fun getProductRepository(context: Context): ProductRepository {
        return ProductRepository.getInstance(
            productDao(context),
            networkService()
        )
    }

    private fun networkService() = NetworkService()

    private fun productDao(context: Context) =
        AppDatabase.getInstance(context.applicationContext).productDao()

    override fun provideProductListViewModelFactory(context: Context): ProductListViewModelFactory {
        val repository = getProductRepository(context)
        return ProductListViewModelFactory(repository)
    }
}

private object Lock

@Volatile private var currentInjector: ViewModelFactoryProvider =
    DefaultViewModelProvider


@VisibleForTesting
private fun setInjectorForTesting(injector: ViewModelFactoryProvider?) {
    synchronized(Lock) {
        currentInjector = injector ?: DefaultViewModelProvider
    }
}

@VisibleForTesting
private fun resetInjector() =
    setInjectorForTesting(null)