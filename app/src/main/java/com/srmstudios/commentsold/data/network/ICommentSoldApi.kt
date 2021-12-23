package com.srmstudios.commentsold.data.network

import com.srmstudios.commentsold.data.network.model.*
import com.srmstudios.commentsold.util.*
import retrofit2.http.*

interface ICommentSoldApi {

    @GET("/api/status")
    suspend fun login(@Header(AUTHORIZATION) authorization: String): LoginResponse

    @GET("/api/products")
    suspend fun getProductsList(
        @Query(QUERY_PARAM_PAGE) page: Int,
        @Query(QUERY_PARAM_LIMIT) limit: Int = PAGE_SIZE_PRODUCTS
    ): ProductsListMainResponse

    @POST("/api/product")
    suspend fun createProduct(@Body createProductRequest: CreateUpdateProductRequest): CreateUpdateDeleteProductResponse

    @PUT("/api/product/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body updateProductRequest: CreateUpdateProductRequest
    ): CreateUpdateDeleteProductResponse

    @DELETE("/api/product/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): CreateUpdateDeleteProductResponse

    @GET("/api/styles")
    suspend fun getProductStyles(): StylesResponse

    @GET("/api/colors")
    suspend fun getProductColors(): ColorsResponse


    @GET("/api/inventory")
    suspend fun getInventoryList(
        @Query(QUERY_PARAM_PAGE) page: Int,
        @Query(QUERY_PARAM_LIMIT) limit: Int = PAGE_SIZE_INVENTORY,
        @Query(QUERY_PARAM_COLOR) color: String? = null,
        @Query(QUERY_PARAM_SIZE) size: String? = null,
        @Query(QUERY_PARAM_QUANTITY) quantity: String? = null
    ): InventoryListMainResponse

    @POST("/api/inventory")
    suspend fun createInventory(@Body createInventoryRequest: CreateUpdateInventoryRequest): CreateUpdateDeleteInventoryResponse

    @PUT("/api/inventory/{id}")
    suspend fun updateInventory(
        @Path("id") id: Int,
        @Body updateInventoryRequest: CreateUpdateInventoryRequest
    ): CreateUpdateDeleteInventoryResponse

    @DELETE("/api/inventory/{id}")
    suspend fun deleteInventory(@Path("id") id: Int): CreateUpdateDeleteInventoryResponse
}