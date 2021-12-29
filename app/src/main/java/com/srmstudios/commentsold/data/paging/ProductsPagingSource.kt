package com.srmstudios.commentsold.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.srmstudios.commentsold.data.network.ICommentSoldApi
import com.srmstudios.commentsold.data.network.model.ProductResponse
import com.srmstudios.commentsold.util.FIRST_PAGE_PRODUCTS_OFFSET
import com.srmstudios.commentsold.util.PAGE_SIZE_PRODUCTS

class ProductsPagingSource(
    private val iCommentSoldApi: ICommentSoldApi
) : PagingSource<Int, ProductResponse>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ProductResponse> {
        var pageNumber = params.key ?: FIRST_PAGE_PRODUCTS_OFFSET

        if (params.loadSize == (3 * PAGE_SIZE_PRODUCTS)) {
            // this means pull to refresh is initiated
            pageNumber = FIRST_PAGE_PRODUCTS_OFFSET
        }

        return try {
            val response = iCommentSoldApi.getProductsList(pageNumber, params.loadSize)
            val products = response.products
            val nextKey = if (products.isEmpty()) {
                null
            } else {
                // in paging library, the initial load size is always 3 times
                // initial load size = 3 * PAGE_SIZE_PRODUCTS
                // ensuring we're not requesting duplicating items at the 2nd request
                pageNumber + (params.loadSize / PAGE_SIZE_PRODUCTS)
            }
            LoadResult.Page(
                data = products,
                prevKey = if (pageNumber == FIRST_PAGE_PRODUCTS_OFFSET) null else pageNumber - 1,
                nextKey = nextKey
            )
        } catch (exception: Exception) {
            return LoadResult.Error(exception)
        }
    }

    // this method is called by the paging library whenever it wants to replace current list
    // with new list AFTER the initial load, for example in event of a process death
    // state.anchorPosition gives us the last queried index by the paging library
    override fun getRefreshKey(state: PagingState<Int, ProductResponse>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }

}