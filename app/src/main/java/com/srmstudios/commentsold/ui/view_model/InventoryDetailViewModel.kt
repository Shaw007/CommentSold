package com.srmstudios.commentsold.ui.view_model

import androidx.lifecycle.*
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.data.network.model.CreateUpdateInventoryRequest
import com.srmstudios.commentsold.data.repo.InventoryRepository
import com.srmstudios.commentsold.data.repo.ProductRepository
import com.srmstudios.commentsold.ui.model.Inventory
import com.srmstudios.commentsold.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val inventoryRepository: InventoryRepository,
    private val savedStateHandle: SavedStateHandle, // this is used to fetch NavArgs provided to the Destination
    private val util: Util
) : ViewModel() {

    private var _inventoryId = MutableLiveData<Int>()

    private var _isCreateScreen = MutableLiveData(true)
    val isCreateScreen: LiveData<Boolean>
        get() = _isCreateScreen

    private var _inventoryDeleted = MutableLiveData<Boolean>()
    val inventoryDeleted: LiveData<Boolean>
        get() = _inventoryDeleted

    private var _message = MutableLiveData<String?>()
    val message: LiveData<String?>
        get() = _message

    private var _progressBar = MutableLiveData<Boolean>()
    val progressBar: LiveData<Boolean>
        get() = _progressBar

    private var _triggerFetchProductColors = MutableLiveData<TRIGGER>()

    init {
        // if inventory is null then we know user came to Create a new inventory
        // otherwise to Edit a inventory or came to see inventory details
        val inventory: Inventory? = savedStateHandle["inventory"]
        inventory?.let { inventoryFromNavArg ->
            _isCreateScreen.value = false
            _inventoryId.value = inventoryFromNavArg.id
        }
        fetchProductColors()
    }

    val inventory = _inventoryId.switchMap { inventoryId ->
        inventoryRepository.getInventory(inventoryId).asLiveData()
    }

    val productColors = _triggerFetchProductColors.switchMap {
        productRepository.getProductColors().asLiveData()
    }

    fun createEditInventory(
        color: String,
        size: String,
        quantity: String,
        weight: String,
        price: String,
        salePrice: String,
        costPrice: String,
        length: String,
        width: String,
        height: String,
        note: String?
    ) {
        if (!util.isNetworkAvailable()) {
            _message.value = util.getStringByResId(R.string.please_check_internent)
            return
        }
        if (color.isNullOrEmpty()) {
            _message.value = util.getStringByResId(R.string.color_validation_mesg)
            return
        }
        if (size.isNullOrEmpty()) {
            _message.value = util.getStringByResId(R.string.size_validation_mesg)
            return
        }
        if (quantity.isNullOrEmpty() || quantity.toIntOrNull() == null) {
            _message.value = util.getStringByResId(R.string.quantity_validation_mesg)
            return
        }
        if (weight.isNullOrEmpty() || weight.toDoubleOrNull() == null) {
            _message.value = util.getStringByResId(R.string.weight_validation_mesg)
            return
        }
        if (price.isNullOrEmpty() || price.toIntOrNull() == null) {
            _message.value = util.getStringByResId(R.string.price_validation_mesg)
            return
        }
        if (salePrice.isNullOrEmpty() || salePrice.toIntOrNull() == null) {
            _message.value = util.getStringByResId(R.string.sale_price_validation_mesg)
            return
        }
        if (costPrice.isNullOrEmpty() || costPrice.toIntOrNull() == null) {
            _message.value = util.getStringByResId(R.string.cost_price_validation_mesg)
            return
        }

        if (length.isNullOrEmpty() || length.toDoubleOrNull() == null) {
            _message.value = util.getStringByResId(R.string.length_validation_mesg)
            return
        }
        if (width.isNullOrEmpty() || width.toDoubleOrNull() == null) {
            _message.value = util.getStringByResId(R.string.width_validation_mesg)
            return
        }
        if (height.isNullOrEmpty() || height.toDoubleOrNull() == null) {
            _message.value = util.getStringByResId(R.string.height_validation_mesg)
            return
        }


        val createUpdateInventoryRequest = CreateUpdateInventoryRequest(
            productId = 11012, // testing
            quantity = quantity.toInt(),
            color = color,
            size = size,
            weight = weight.toDouble(),
            priceCents = price.toInt(),
            salePriceCents = salePrice.toInt(),
            costCents = costPrice.toInt(),
            length = length.toDouble(),
            width = width.toDouble(),
            height = height.toDouble(),
            note = note,
        )

        if (_isCreateScreen.value == true) {
            // Create new inventory
            handleCreateInventory(createUpdateInventoryRequest)
        } else {
            // Update existing inventory
            handleEditInventory(createUpdateInventoryRequest)
        }
    }

    private fun handleCreateInventory(createInventoryRequest: CreateUpdateInventoryRequest) =
        viewModelScope.launch {
            inventoryRepository.createInventory(
                createInventoryRequest
            ).collect { result ->
                _progressBar.value = result is Resource.Loading

                when (result) {
                    is Resource.Success -> {
                        val createdInventoryId = result.data?.inventoryId ?: -1
                        if (createdInventoryId > 0) {
                            // create new inventory in DB with
                            // attributes that successfully created a inventory on the Server
                            // also set the primary key as the inventory id returned from the server
                            inventoryRepository.createInventoryInDb(
                                createdInventoryId,
                                createInventoryRequest
                            )
                            _message.value = util.getStringByResId(R.string.inventory_created_mesg)
                        } else {
                            _message.value = util.getStringByResId(R.string.something_went_wrong)
                        }
                    }
                    is Resource.Error -> {
                        val error = util.parseApiErrorThrowable(result.error)
                        _message.value = error
                    }
                }
            }
        }

    private fun handleEditInventory(editInventoryRequest: CreateUpdateInventoryRequest) =
        viewModelScope.launch {
            _inventoryId.value?.let { inventoryId ->
                inventoryRepository.updateInventory(
                    inventoryId,
                    editInventoryRequest
                ).collect { result ->
                    _progressBar.value = result is Resource.Loading

                    when (result) {
                        is Resource.Success -> {
                            val updatedInventoryId = result.data?.inventoryId ?: -1
                            if (updatedInventoryId > 0) {
                                // update the inventory in local DB as well
                                // to sync the local inventory with the server
                                inventoryRepository.updateInventoryInDb(
                                    updatedInventoryId,
                                    editInventoryRequest
                                )
                                _message.value =
                                    util.getStringByResId(R.string.inventory_updated_mesg)
                            } else {
                                _message.value = util.getStringByResId(R.string.something_went_wrong)
                            }
                        }
                        is Resource.Error -> {
                            val error = util.parseApiErrorThrowable(result.error)
                            _message.value = error
                        }
                    }
                }
            }
        }

    fun deleteInventory() {
        if (!util.isNetworkAvailable()) {
            _message.value = util.getStringByResId(R.string.please_check_internent)
            return
        }

        viewModelScope.launch {
            _inventoryId.value?.let { inventoryId ->
                inventoryRepository.deleteInventory(inventoryId).collect { result ->
                    _progressBar.value = result is Resource.Loading

                    when (result) {
                        is Resource.Success -> {
                            val deletedInventoryId = result.data?.inventoryId ?: -1
                            if (deletedInventoryId > 0) {
                                // delete inventory in local DB as well
                                // to sync the local inventory with the server
                                inventoryRepository.deleteInventoryInDb(deletedInventoryId)

                                _message.value =
                                    util.getStringByResId(R.string.inventory_deleted_mesg)
                                _inventoryDeleted.value = true
                            } else {
                                _message.value = util.getStringByResId(R.string.something_went_wrong)
                            }
                        }
                        is Resource.Error -> {
                            val error = util.parseApiErrorThrowable(result.error)
                            _message.value = error
                        }
                    }
                }
            }
        }
    }

    fun fetchProductColors() {
        if (!util.isNetworkAvailable()) {
            _message.value = util.getStringByResId(R.string.please_check_internent)
            return
        }
        _triggerFetchProductColors.value = TRIGGER.TRIGGER
    }

    fun doneShowingMessage() {
        _message.value = null
    }
}