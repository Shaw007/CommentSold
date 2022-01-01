package com.srmstudios.commentsold.data.repo

import com.srmstudios.commentsold.data.database.CommentSoldDatabase
import com.srmstudios.commentsold.data.database.entity.DatabaseInventory
import com.srmstudios.commentsold.data.database.entity.toDatabaseInventory
import com.srmstudios.commentsold.data.network.ICommentSoldApi
import com.srmstudios.commentsold.data.network.model.CreateUpdateInventoryRequest
import com.srmstudios.commentsold.data.network.model.InventoryListMainResponse
import com.srmstudios.commentsold.data.network.model.toDatabaseInventory
import com.srmstudios.commentsold.di.CommentSoldAuthApi
import com.srmstudios.commentsold.util.FIRST_PAGE_INVENTORY_OFFSET
import com.srmstudios.commentsold.util.Resource
import com.srmstudios.commentsold.util.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class InventoryRepository @Inject constructor(
    @CommentSoldAuthApi private val iCommentSoldApi: ICommentSoldApi,
    private val commentSoldDatabase: CommentSoldDatabase
) {
    fun getInventoryList(productId: Int? = null, color: String? = null, size: String? = null, quantity: String? = null) = networkBoundResource(
        query = {
            // query local database
            if(productId == null) {
                commentSoldDatabase.inventoryDao().getInventoryListJoinProduct()
            }else{
                commentSoldDatabase.inventoryDao().getInventoryListJoinProduct(productId)
            }
        },
        fetch = {
            // fetch inventory list from network
            if(productId == null){
                iCommentSoldApi.getInventoryList(
                    page = FIRST_PAGE_INVENTORY_OFFSET,
                    color = color,
                    size = size,
                    quantity = quantity
                )
            }else {
                InventoryListMainResponse()
            }
        },
        saveFetchResult = {
            // save fetched inventory list from the network to local database
            it.inventory?.let { inventory ->
                // delete all existing cached inventory from the database
                // and insert new data
                deleteAllInventoryFromDB()
                commentSoldDatabase.inventoryDao().insert(inventory.toDatabaseInventory())
            }
        }
    )

    fun loadMoreInventory(page: Int, color: String? = null, size: String? = null, quantity: String? = null) = flow {
        try {
            emit(Resource.Loading(null))
            val response = iCommentSoldApi.getInventoryList(
                page = page,
                color = color,
                size = size,
                quantity = quantity
            )
            response.inventory?.let { inventory ->
                commentSoldDatabase.inventoryDao().insert(inventory.toDatabaseInventory())
            }
            emit(Resource.Success(response))
        } catch (ex: Throwable) {
            emit(Resource.Error(ex, null))
        }
    }

    fun getInventory(id: Int) = commentSoldDatabase.inventoryDao().getInventoryById(id)

    fun createInventory(
        createInventoryRequest: CreateUpdateInventoryRequest
    ) = flow {
        try {
            emit(Resource.Loading(null))
            val response = iCommentSoldApi.createInventory(createInventoryRequest)
            emit(Resource.Success(response))
        } catch (ex: Throwable) {
            ex.printStackTrace()
            emit(Resource.Error(ex, null))
        }
    }

    suspend fun createInventoryInDb(id: Int, createInventoryRequest: CreateUpdateInventoryRequest) =
        withContext(Dispatchers.IO) {
            // create new inventory in DB with
            // attributes that successfully created a inventory on the Server
            // also set the primary key as the inventory id returned from the server
            val databaseInventoryUpdate = DatabaseInventory(
                id = id,
                productId = createInventoryRequest.productId,
                quantity = createInventoryRequest.quantity,
                color = createInventoryRequest.color,
                size = createInventoryRequest.size,
                weight = createInventoryRequest.weight?.toString() ?: "0",
                priceCents = createInventoryRequest.priceCents,
                salePriceCents = createInventoryRequest.salePriceCents,
                costCents = createInventoryRequest.costCents,
                sku = createInventoryRequest.sku,
                length = createInventoryRequest.length?.toString() ?: "0",
                width = createInventoryRequest.width?.toString() ?: "0",
                height = createInventoryRequest.height?.toString() ?: "0",
                note = createInventoryRequest.note,
            )

            commentSoldDatabase.inventoryDao().insert(
                databaseInventoryUpdate
            )
        }

    fun updateInventory(
        id: Int,
        createInventoryRequest: CreateUpdateInventoryRequest
    ) = flow {
        try {
            emit(Resource.Loading(null))
            val response = iCommentSoldApi.updateInventory(id, createInventoryRequest)
            emit(Resource.Success(response))
        } catch (ex: Throwable) {
            ex.printStackTrace()
            emit(Resource.Error(ex, null))
        }
    }

    suspend fun updateInventoryInDb(id: Int, updateInventoryRequest: CreateUpdateInventoryRequest) =
        withContext(Dispatchers.IO) {
            // get the inventory present in DB
            commentSoldDatabase.inventoryDao().getInventoryById(id).first()
                ?.let { databaseInventory ->

                    // update the attributes that were successfully updated on the Server
                    val databaseInventoryUpdate = databaseInventory.copy(
                        id = id,
                        productId = updateInventoryRequest.productId,
                        quantity = updateInventoryRequest.quantity,
                        color = updateInventoryRequest.color,
                        size = updateInventoryRequest.size,
                        weight = updateInventoryRequest.weight?.toString() ?: "0",
                        priceCents = updateInventoryRequest.priceCents,
                        salePriceCents = updateInventoryRequest.salePriceCents,
                        costCents = updateInventoryRequest.costCents,
                        sku = updateInventoryRequest.sku,
                        length = updateInventoryRequest.length?.toString() ?: "0",
                        width = updateInventoryRequest.width?.toString() ?: "0",
                        height = updateInventoryRequest.height?.toString() ?: "0",
                        note = updateInventoryRequest.note,
                    )

                    commentSoldDatabase.inventoryDao().update(
                        databaseInventoryUpdate.toDatabaseInventory()
                    )
                }
        }

    fun deleteInventory(
        id: Int
    ) = flow {
        try {
            emit(Resource.Loading(null))
            val response = iCommentSoldApi.deleteInventory(id)
            emit(Resource.Success(response))
        } catch (ex: Throwable) {
            ex.printStackTrace()
            emit(Resource.Error(ex, null))
        }
    }

    suspend fun deleteInventoryInDb(id: Int) =
        withContext(Dispatchers.IO) {
            // delete the inventory from local DB as well
            // when deleted on the server
            commentSoldDatabase.inventoryDao().delete(id)
        }

    suspend fun deleteAllInventoryFromDB() = withContext(Dispatchers.IO) {
        commentSoldDatabase.inventoryDao().deleteAllInventory()
    }
}