package com.srmstudios.commentsold.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.srmstudios.commentsold.data.database.CommentSoldDatabase
import com.srmstudios.commentsold.data.database.entity.DatabaseProduct
import com.srmstudios.commentsold.data.database.entity.RemoteKeysProduct
import com.srmstudios.commentsold.data.network.ICommentSoldApi
import com.srmstudios.commentsold.data.network.model.ProductResponse
import com.srmstudios.commentsold.data.network.model.toDatabaseProducts
import com.srmstudios.commentsold.di.CommentSoldAuthApi
import com.srmstudios.commentsold.util.FIRST_PAGE_PRODUCTS_OFFSET

@OptIn(ExperimentalPagingApi::class)
class ProductRemoteMediator(
    @CommentSoldAuthApi private val iCommentSoldApi: ICommentSoldApi,
    private val commentSoldDatabase: CommentSoldDatabase
) : RemoteMediator<Int, DatabaseProduct>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, DatabaseProduct>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getRemoteKeyClosestToCurrentPosition(state)
                remoteKeys?.nextKey?.minus(1) ?: FIRST_PAGE_PRODUCTS_OFFSET
            }
            LoadType.PREPEND -> {
                val remoteKeys = getRemoteKeyForFirstItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                val prevKey = remoteKeys?.prevKey
                if (prevKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                }
                prevKey
            }
            LoadType.APPEND -> {
                val remoteKeys = getRemoteKeyForLastItem(state)
                // If remoteKeys is null, that means the refresh result is not in the database yet.
                // We can return Success with endOfPaginationReached = false because Paging
                // will call this method again if RemoteKeys becomes non-null.
                // If remoteKeys is NOT NULL but its nextKey is null, that means we've reached
                // the end of pagination for append.
                val nextKey = remoteKeys?.nextKey
                if (nextKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = remoteKeys != null)
                }
                nextKey
            }
        }

        try {
            val apiResponse = iCommentSoldApi.getProductsList(page, state.config.pageSize)

            val products = apiResponse.products
            val endOfPaginationReached = products.isEmpty()
            commentSoldDatabase.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    commentSoldDatabase.remoteKeysProductDao().clearRemoteKeys()
                    commentSoldDatabase.productDao().deleteAllProducts()
                }
                val prevKey = if (page == FIRST_PAGE_PRODUCTS_OFFSET) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1
                val keys = products.map {
                    RemoteKeysProduct(productId = it.id, prevKey = prevKey, nextKey = nextKey)
                }
                commentSoldDatabase.remoteKeysProductDao().insertAll(keys)
                commentSoldDatabase.productDao().insert(products.toDatabaseProducts())
            }
            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (exception: Exception) {
            return MediatorResult.Error(exception)
        }
    }

    private suspend fun getRemoteKeyForLastItem(state: PagingState<Int, DatabaseProduct>): RemoteKeysProduct? {
        // Get the last page that was retrieved, that contained items.
        // From that last page, get the last item
        val repo = state.pages.lastOrNull { it.data.isNotEmpty() }?.data?.lastOrNull()
        return repo?.let {
            // Get the remote keys of the last item retrieved
            commentSoldDatabase.remoteKeysProductDao().getRemoteKeyByProductId(repo.id)
        }
    }

    private suspend fun getRemoteKeyForFirstItem(state: PagingState<Int, DatabaseProduct>): RemoteKeysProduct? {
        // Get the first page that was retrieved, that contained items.
        // From that first page, get the first item
        val repo = state.pages.firstOrNull { it.data.isNotEmpty() }?.data?.firstOrNull()
        return repo?.let {
            // Get the remote keys of the last item retrieved
            commentSoldDatabase.remoteKeysProductDao().getRemoteKeyByProductId(repo.id)
        }
    }

    private suspend fun getRemoteKeyClosestToCurrentPosition(
        state: PagingState<Int, DatabaseProduct>
    ): RemoteKeysProduct? {
        // The paging library is trying to load data after the anchor position
        // Get the item closest to the anchor position
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.id?.let { repoId ->
                commentSoldDatabase.remoteKeysProductDao().getRemoteKeyByProductId(repoId)
            }
        }
    }

}