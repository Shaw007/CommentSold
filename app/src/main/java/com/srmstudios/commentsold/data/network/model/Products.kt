package com.srmstudios.commentsold.data.network.model

import com.google.gson.annotations.SerializedName
import com.srmstudios.commentsold.data.database.entity.DatabaseProduct
import com.srmstudios.commentsold.ui.model.Product

data class ProductsListMainResponse(
    @SerializedName("count") var count: Int? = null,
    @SerializedName("products") var products: List<ProductResponse> = listOf(),
    @SerializedName("total") var total: Int? = null
)

data class ProductResponse(
    @SerializedName("id") var id: Int,
    @SerializedName("product_name") var productName: String? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("style") var style: String? = null,
    @SerializedName("brand") var brand: String? = null,
    @SerializedName("created_at") var createdAt: String? = null,
    @SerializedName("updated_at") var updatedAt: String? = null,
    @SerializedName("url") var url: String? = null,
    @SerializedName("product_type") var productType: String? = null,
    @SerializedName("shipping_price") var shippingPrice: Int? = null,
    @SerializedName("note") var note: String? = null,
    @SerializedName("admin_id") var adminId: Int? = null
)

fun ProductResponse.toProduct(): Product {
    return Product(
        id = id,
        productName = productName,
        description = description,
        style = style,
        brand = brand,
        createdAt = createdAt,
        updatedAt = updatedAt,
        url = url,
        productType = productType,
        shippingPrice = shippingPrice,
        note = note,
        adminId = adminId
    )
}

data class CreateUpdateProductRequest(
    @SerializedName("name") var name: String? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("style") var style: String? = null,
    @SerializedName("brand") var brand: String? = null,
    @SerializedName("shipping_price_cents") var shippingPriceCents: Int? = null
)

data class CreateUpdateDeleteProductResponse(
    @SerializedName("message") var message: String,
    @SerializedName("product_id") var productId: Int? = null
)

data class StylesResponse(
    @SerializedName("styles") var styles: List<String>? = null
)

data class ColorsResponse(
    @SerializedName("colors") var colors: List<String>? = null
)

fun List<ProductResponse>.toDatabaseProducts() = map {
    DatabaseProduct(
        id = it.id,
        productName = it.productName,
        description = it.description,
        style = it.style,
        brand = it.brand,
        createdAt = it.createdAt,
        updatedAt = it.updatedAt,
        url = it.url,
        productType = it.productType,
        shippingPrice = it.shippingPrice,
        note = it.note,
        adminId = it.adminId
    )
}











