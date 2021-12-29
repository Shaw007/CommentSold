package com.srmstudios.commentsold.data.repo

import androidx.lifecycle.LiveData
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.srmstudios.commentsold.data.database.CommentSoldDatabase
import com.srmstudios.commentsold.data.database.entity.DatabaseProduct
import com.srmstudios.commentsold.data.network.ICommentSoldApi
import com.srmstudios.commentsold.data.network.model.CreateUpdateProductRequest
import com.srmstudios.commentsold.data.network.model.ProductResponse
import com.srmstudios.commentsold.data.network.model.toDatabaseProducts
import com.srmstudios.commentsold.data.paging.ProductsPagingSource
import com.srmstudios.commentsold.di.CommentSoldAuthApi
import com.srmstudios.commentsold.util.FIRST_PAGE_PRODUCTS_OFFSET
import com.srmstudios.commentsold.util.PAGE_SIZE_PRODUCTS
import com.srmstudios.commentsold.util.Resource
import com.srmstudios.commentsold.util.networkBoundResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ProductRepository @Inject constructor(
    @CommentSoldAuthApi private val iCommentSoldApi: ICommentSoldApi,
    private val commentSoldDatabase: CommentSoldDatabase
) {

    fun getProducts(): LiveData<PagingData<ProductResponse>> {
        return Pager(
            config = PagingConfig(
                pageSize = PAGE_SIZE_PRODUCTS,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { ProductsPagingSource(iCommentSoldApi) }
        ).liveData
    }

    fun getProduct(id: Int) = commentSoldDatabase.productDao().getProductById(id)

    fun createProduct(
        createProductRequest: CreateUpdateProductRequest
    ) = flow {
        try {
            emit(Resource.Loading(null))
            val response = iCommentSoldApi.createProduct(createProductRequest)
            emit(Resource.Success(response))
        } catch (ex: Throwable) {
            ex.printStackTrace()
            emit(Resource.Error(ex, null))
        }
    }

    suspend fun createProductInDb(id: Int, createProductRequest: CreateUpdateProductRequest) =
        withContext(Dispatchers.IO) {
            // create new product in DB with
            // attributes that successfully created a product on the Server
            // also set the primary key as the product id returned from the server
            val databaseProductUpdate = DatabaseProduct(
                id = id,
                productName = createProductRequest.name,
                description = createProductRequest.description,
                style = createProductRequest.style,
                brand = createProductRequest.brand,
                shippingPrice = createProductRequest.shippingPriceCents
            )

            commentSoldDatabase.productDao().insert(
                databaseProductUpdate
            )
        }

    fun updateProduct(
        id: Int,
        createProductRequest: CreateUpdateProductRequest
    ) = flow {
        try {
            emit(Resource.Loading(null))
            val response = iCommentSoldApi.updateProduct(id, createProductRequest)
            emit(Resource.Success(response))
        } catch (ex: Throwable) {
            ex.printStackTrace()
            emit(Resource.Error(ex, null))
        }
    }

    suspend fun updateProductInDb(id: Int, updateProductRequest: CreateUpdateProductRequest) =
        withContext(Dispatchers.IO) {
            // get the product present in DB
            commentSoldDatabase.productDao().getProductById(id).first()?.let { databaseProduct ->

                // update the attributes that were successfully updated on the Server
                val databaseProductUpdate = databaseProduct.copy(
                    productName = updateProductRequest.name,
                    description = updateProductRequest.description,
                    style = updateProductRequest.style,
                    brand = updateProductRequest.brand,
                    shippingPrice = updateProductRequest.shippingPriceCents
                )

                commentSoldDatabase.productDao().update(
                    databaseProductUpdate
                )
            }
        }

    fun deleteProduct(
        id: Int
    ) = flow {
        try {
            emit(Resource.Loading(null))
            val response = iCommentSoldApi.deleteProduct(id)
            emit(Resource.Success(response))
        } catch (ex: Throwable) {
            ex.printStackTrace()
            emit(Resource.Error(ex, null))
        }
    }

    suspend fun deleteProductInDb(id: Int) =
        withContext(Dispatchers.IO) {
            // delete the product from local DB as well
            // when deleted on the server
            commentSoldDatabase.productDao().delete(id)
        }

    fun getProductStyles() = flow {
        try {
            emit(Resource.Loading(null))
            val response = iCommentSoldApi.getProductStyles()
            emit(Resource.Success(response))
        } catch (ex: Throwable) {
            ex.printStackTrace()
            emit(Resource.Error(ex, null))
        }
    }

    suspend fun deleteAllProductsFromDB() = withContext(Dispatchers.IO) {
        commentSoldDatabase.productDao().deleteAllProducts()
    }
}













