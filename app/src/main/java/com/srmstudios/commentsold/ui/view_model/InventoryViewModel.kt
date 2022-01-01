package com.srmstudios.commentsold.ui.view_model

import androidx.lifecycle.*
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.data.repo.InventoryRepository
import com.srmstudios.commentsold.data.repo.ProductRepository
import com.srmstudios.commentsold.ui.model.Product
import com.srmstudios.commentsold.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val productRepository: ProductRepository,
    private val util: Util
) : ViewModel() {

    // this is to trigger the getInventoryList() method in InventoryRepository
    // to start fetch fresh data from the API and replace that with the local data
    // This trigger is helpful when there is some error and data is not fetched properly
    // So when user taps the Retry button, this mutableLiveData gets triggered
    // and eventually triggers the getInventoryList() method again in InventoryRepository
    private var _triggerFetchInventory = MutableLiveData<TRIGGER>()

    private var _triggerFetchProductColors = MutableLiveData<TRIGGER>()

    private var _selectedProduct = MutableLiveData<Product?>(null)
    private var _selectedColor = MutableLiveData<String?>(null)
    private var _selectedSize = MutableLiveData<String?>(null)
    private var _selectedQuantity = MutableLiveData<Int?>(null)

    private var _progressBarPagination = MutableLiveData<Boolean>()
    val progressBarPagination: LiveData<Boolean>
        get() = _progressBarPagination

    private var _message = MutableLiveData<String?>()
    val message: LiveData<String?>
        get() = _message

    private var page = FIRST_PAGE_INVENTORY_OFFSET
    private var isLoadMoreInProgress = false
    private var allInventoryLoaded = false

    init {
        // set this for initial call
        _triggerFetchInventory.value = TRIGGER.TRIGGER
        fetchProductColors()
    }

    val products = productRepository.getAllProductsFromDB()

    val productColors = _triggerFetchProductColors.switchMap {
        productRepository.getProductColors().asLiveData()
    }

    val inventoryList = _triggerFetchInventory.switchMap {
        // this lambda will be triggered everytime _trigger value gets modified
        page = 0
        allInventoryLoaded = false
        inventoryRepository.getInventoryList(
            productId = _selectedProduct.value?.id,
            color = _selectedColor?.value,
            size = _selectedSize?.value,
            quantity = if (_selectedQuantity?.value == null) null else "<${_selectedQuantity.value}"
        )
    }

    fun loadMore() = viewModelScope.launch {
        if (isLoadMoreInProgress || allInventoryLoaded) return@launch

        if (!util.isNetworkAvailable()) {
            _message.value = util.getStringByResId(R.string.please_check_internent)
            return@launch
        }

        isLoadMoreInProgress = true
        page++

        inventoryRepository.loadMoreInventory(
            page = page,
            color = _selectedColor?.value,
            size = _selectedSize?.value,
            quantity = if (_selectedQuantity?.value == null) null else "<${_selectedQuantity.value}"
        )
            .collect { result ->
                _progressBarPagination.value = result is Resource.Loading

                when (result) {
                    is Resource.Success -> {
                        allInventoryLoaded =
                            result.data?.inventory?.size ?: PAGE_SIZE_INVENTORY < PAGE_SIZE_INVENTORY
                        isLoadMoreInProgress = false
                    }
                    is Resource.Error -> {
                        page--
                        isLoadMoreInProgress = false
                    }
                }
            }
    }

    fun fetchInventoryList(): Boolean {
        return if (!util.isNetworkAvailable()) {
            _message.value = util.getStringByResId(R.string.please_check_internent)
            return false
        } else {
            _triggerFetchInventory.value = TRIGGER.TRIGGER
            true
        }
    }

    fun fetchProductColors() {
        if (!util.isNetworkAvailable()) {
            _message.value = util.getStringByResId(R.string.please_check_internent)
            return
        }
        _triggerFetchProductColors.value = TRIGGER.TRIGGER
    }

    fun setSelectedProduct(product: Product){
        _selectedProduct.value = product
        _triggerFetchInventory.value = TRIGGER.TRIGGER
    }

    fun setSelectedColor(color: String?) {
        _selectedColor.value = color
        _triggerFetchInventory.value = TRIGGER.TRIGGER
    }

    fun setSelectedSize(size: String?) {
        _selectedSize.value = size
        _triggerFetchInventory.value = TRIGGER.TRIGGER
    }

    fun setSelectedQuantity(quantity: String?) {
        _selectedQuantity.value = quantity?.toIntOrNull()
        _triggerFetchInventory.value = TRIGGER.TRIGGER
    }

    fun doneShowingMessage() {
        _message.value = null
    }
}
