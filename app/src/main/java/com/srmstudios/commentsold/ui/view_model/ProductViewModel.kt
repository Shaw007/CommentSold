package com.srmstudios.commentsold.ui.view_model

import androidx.lifecycle.*
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.data.repo.InventoryRepository
import com.srmstudios.commentsold.data.repo.ProductRepository
import com.srmstudios.commentsold.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val commentSoldPrefsManager: CommentSoldPrefsManager,
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository,
    private val util: Util
) : ViewModel() {

    // this is to trigger the getProducts() method in ProductRepository
    // to start fetch fresh data from the API and replace that with the local data
    // This trigger is helpful when there is some error and data is not fetched properly
    // So when user taps the Retry button, this mutableLiveData gets triggered
    // and eventually triggers the getProducts() method again in ProductRepository
    private var _triggerFetchProducts = MutableLiveData<TRIGGER>()

    private var _progressBarPagination = MutableLiveData<Boolean>()
    val progressBarPagination: LiveData<Boolean>
        get() = _progressBarPagination

    private var _navigateToLoginScreen = MutableLiveData<Boolean>()
    val navigateToLoginScreen: LiveData<Boolean>
        get() = _navigateToLoginScreen

    private var _message = MutableLiveData<String?>()
    val message: LiveData<String?>
        get() = _message

    private var page = FIRST_PAGE_PRODUCTS_OFFSET
    private var isLoadMoreInProgress = false
    private var allProductsLoaded = false

    init {
        // set this for initial call
        _triggerFetchProducts.value = TRIGGER.TRIGGER
    }

    val products = _triggerFetchProducts.switchMap {
        // this lambda will be triggered everytime _trigger value gets modified
        productRepository.getProducts()
    }

    fun loadMore() = viewModelScope.launch {
        if (isLoadMoreInProgress || allProductsLoaded) return@launch

        if (!util.isNetworkAvailable()) {
            _message.value = util.getStringByResId(R.string.please_check_internent)
            return@launch
        }

        isLoadMoreInProgress = true
        page++

        productRepository.loadMoreProducts(page).collect { result ->
            _progressBarPagination.value = result is Resource.Loading

            when (result) {
                is Resource.Success -> {
                    allProductsLoaded = result.data?.products?.isNullOrEmpty() == true
                    isLoadMoreInProgress = false
                }
                is Resource.Error -> {
                    page--
                    isLoadMoreInProgress = false
                }
            }
        }
    }

    fun fetchProducts(): Boolean {
        return if (!util.isNetworkAvailable()) {
            _message.value = util.getStringByResId(R.string.please_check_internent)
            return false
        } else {
            page = 0
            allProductsLoaded = false
            _triggerFetchProducts.value = TRIGGER.TRIGGER
            true
        }
    }

    fun logout() = viewModelScope.launch {
        productRepository.deleteAllProductsFromDB()
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






