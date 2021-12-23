package com.srmstudios.commentsold.ui.view_model

import androidx.lifecycle.*
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.data.repo.InventoryRepository
import com.srmstudios.commentsold.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val inventoryRepository: InventoryRepository,
    private val util: Util
) : ViewModel() {

    // this is to trigger the getInventoryList() method in InventoryRepository
    // to start fetch fresh data from the API and replace that with the local data
    // This trigger is helpful when there is some error and data is not fetched properly
    // So when user taps the Retry button, this mutableLiveData gets triggered
    // and eventually triggers the getInventoryList() method again in InventoryRepository
    private var _triggerFetchInventory = MutableLiveData<TRIGGER>()

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
    }

    val inventoryList = _triggerFetchInventory.switchMap {
        // this lambda will be triggered everytime _trigger value gets modified
        inventoryRepository.getInventoryList()
    }

    fun loadMore() = viewModelScope.launch {
        if (isLoadMoreInProgress || allInventoryLoaded) return@launch

        if (!util.isNetworkAvailable()) {
            _message.value = util.getStringByResId(R.string.please_check_internent)
            return@launch
        }

        isLoadMoreInProgress = true
        page++

        inventoryRepository.loadMoreInventory(page).collect { result ->
            _progressBarPagination.value = result is Resource.Loading

            when (result) {
                is Resource.Success -> {
                    allInventoryLoaded = result.data?.inventory?.isNullOrEmpty() == true
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
            page = 0
            allInventoryLoaded = false
            _triggerFetchInventory.value = TRIGGER.TRIGGER
            true
        }
    }

    fun doneShowingMessage() {
        _message.value = null
    }
}