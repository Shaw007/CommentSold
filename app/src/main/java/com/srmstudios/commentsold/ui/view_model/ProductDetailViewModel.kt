package com.srmstudios.commentsold.ui.view_model

import androidx.lifecycle.*
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.data.network.model.CreateUpdateProductRequest
import com.srmstudios.commentsold.data.repo.ProductRepository
import com.srmstudios.commentsold.ui.model.Product
import com.srmstudios.commentsold.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val savedStateHandle: SavedStateHandle, // this is used to fetch NavArgs provided to the Destination
    private val util: Util
) : ViewModel() {

    private var _productId = MutableLiveData<Int>()

    private var _isCreateScreen = MutableLiveData(true)
    val isCreateScreen: LiveData<Boolean>
        get() = _isCreateScreen

    private var _productDeleted = MutableLiveData<Boolean>()
    val productDeleted: LiveData<Boolean>
        get() = _productDeleted

    private var _message = MutableLiveData<String?>()
    val message: LiveData<String?>
        get() = _message

    private var _progressBar = MutableLiveData<Boolean>()
    val progressBar: LiveData<Boolean>
        get() = _progressBar

    private var _triggerFetchProductStyles = MutableLiveData<TRIGGER>()

    init {
        // if product is null then we know user came to Create a new product
        // otherwise to Edit a product or came to see product details
        val product: Product? = savedStateHandle["product"]
        product?.let { productFromNavArg ->
            _isCreateScreen.value = false
            _productId.value = productFromNavArg.id
        }
        fetchProductStyles()
    }

    val product = _productId.switchMap { productId ->
        productRepository.getProduct(productId).asLiveData()
    }

    val productStyles = _triggerFetchProductStyles.switchMap {
        productRepository.getProductStyles().asLiveData()
    }

    fun createEditProduct(
        name: String,
        description: String,
        style: String,
        brand: String,
        shippingPrice: String
    ) {
        if (!util.isNetworkAvailable()) {
            _message.value = util.getStringByResId(R.string.please_check_internent)
            return
        }
        if (name.isNullOrEmpty()) {
            _message.value = util.getStringByResId(R.string.name_validation_mesg)
            return
        }
        if (description.isNullOrEmpty()) {
            _message.value = util.getStringByResId(R.string.desc_validation_mesg)
            return
        }
        if (style.isNullOrEmpty()) {
            _message.value = util.getStringByResId(R.string.style_validation_mesg)
            return
        }
        if (brand.isNullOrEmpty()) {
            _message.value = util.getStringByResId(R.string.brand_validation_mesg)
            return
        }
        if (shippingPrice.isNullOrEmpty() || shippingPrice.toIntOrNull() == null) {
            _message.value = util.getStringByResId(R.string.shipping_price_validation_mesg)
            return
        }

        val createUpdateProductRequest = CreateUpdateProductRequest(
            name,
            description,
            style,
            brand, shippingPrice.toInt()
        )

        if (_isCreateScreen.value == true) {
            // Create new product
            handleCreateProduct(createUpdateProductRequest)
        } else {
            // Update existing product
            handleEditProduct(createUpdateProductRequest)
        }
    }

    private fun handleCreateProduct(createProductRequest: CreateUpdateProductRequest) =
        viewModelScope.launch {
            productRepository.createProduct(
                createProductRequest
            ).collect { result ->
                _progressBar.value = result is Resource.Loading

                when (result) {
                    is Resource.Success -> {
                        val createdProductId = result.data?.productId ?: -1
                        if (createdProductId > 0) {
                            // create new product in DB with
                            // attributes that successfully created a product on the Server
                            // also set the primary key as the product id returned from the server
                            productRepository.createProductInDb(
                                createdProductId,
                                createProductRequest
                            )
                            _message.value = util.getStringByResId(R.string.product_created_mesg)
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

    private fun handleEditProduct(editProductRequest: CreateUpdateProductRequest) =
        viewModelScope.launch {
            _productId.value?.let { productId ->
                productRepository.updateProduct(
                    productId,
                    editProductRequest
                ).collect { result ->
                    _progressBar.value = result is Resource.Loading

                    when (result) {
                        is Resource.Success -> {
                            val updatedProductId = result.data?.productId ?: -1
                            if (updatedProductId > 0) {
                                // update the product in local DB as well
                                // to sync the local products with the server
                                productRepository.updateProductInDb(
                                    updatedProductId,
                                    editProductRequest
                                )
                                _message.value =
                                    util.getStringByResId(R.string.product_updated_mesg)
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

    fun deleteProduct() {
        if (!util.isNetworkAvailable()) {
            _message.value = util.getStringByResId(R.string.please_check_internent)
            return
        }

        viewModelScope.launch {
            _productId.value?.let { productId ->
                productRepository.deleteProduct(productId).collect { result ->
                    _progressBar.value = result is Resource.Loading

                    when (result) {
                        is Resource.Success -> {
                            val deletedProductId = result.data?.productId ?: -1
                            if (deletedProductId > 0) {
                                // delete product in local DB as well
                                // to sync the local products with the server
                                productRepository.deleteProductInDb(deletedProductId)

                                _message.value =
                                    util.getStringByResId(R.string.product_deleted_mesg)
                                _productDeleted.value = true
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

    fun fetchProductStyles() {
        if (!util.isNetworkAvailable()) {
            _message.value = util.getStringByResId(R.string.please_check_internent)
            return
        }
        _triggerFetchProductStyles.value = TRIGGER.TRIGGER
    }

    fun doneShowingMessage() {
        _message.value = null
    }
}









