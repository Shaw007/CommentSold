package com.srmstudios.commentsold.ui.view_model

import androidx.lifecycle.*
import androidx.paging.cachedIn
import androidx.paging.map
import com.srmstudios.commentsold.data.database.entity.toProduct
import com.srmstudios.commentsold.data.network.model.toProduct
import com.srmstudios.commentsold.data.repo.InventoryRepository
import com.srmstudios.commentsold.data.repo.ProductRepository
import com.srmstudios.commentsold.util.CommentSoldPrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val commentSoldPrefsManager: CommentSoldPrefsManager,
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository
) : ViewModel() {

    private var _navigateToLoginScreen = MutableLiveData<Boolean>()
    val navigateToLoginScreen: LiveData<Boolean>
        get() = _navigateToLoginScreen

    private var _message = MutableLiveData<String?>()
    val message: LiveData<String?>
        get() = _message

    val products = productRepository.getProducts()
        .map { pagingData ->
            pagingData.map { databaseProduct ->
                databaseProduct.toProduct()
            }
        }.cachedIn(viewModelScope)

    fun logout() = viewModelScope.launch {
        productRepository.deleteAllProductsFromDB()
        productRepository.deleteAllRemoteKeysProductsFromDB()
        productRepository
        inventoryRepository.deleteAllInventoryFromDB()
        commentSoldPrefsManager.clear()
        _navigateToLoginScreen.value = true
    }

    fun doneNavigatingToLoginScreen() {
        _navigateToLoginScreen.value = false
    }

    fun doneShowingMessage() {
        _message.value = null
    }
}






