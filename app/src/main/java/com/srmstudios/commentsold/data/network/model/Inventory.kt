package com.srmstudios.commentsold.data.network.model

import com.google.gson.annotations.SerializedName
import com.srmstudios.commentsold.data.database.entity.DatabaseInventory

data class InventoryListMainResponse(
    @SerializedName("count") var count: Int? = null,
    @SerializedName("inventory") var inventory: List<InventoryResponse>? = null,
    @SerializedName("total") var total: Int? = null
)

data class InventoryResponse(
    @SerializedName("id") var id: Int,
    @SerializedName("product_id") var productId: Int,
    @SerializedName("quantity") var quantity: Int? = null,
    @SerializedName("color") var color: String? = null,
    @SerializedName("size") var size: String? = null,
    @SerializedName("weight") var weight: String? = null,
    @SerializedName("price_cents") var priceCents: Int? = null,
    @SerializedName("sale_price_cents") var salePriceCents: Int? = null,
    @SerializedName("cost_cents") var costCents: Int? = null,
    @SerializedName("sku") var sku: String? = null,
    @SerializedName("length") var length: String? = null,
    @SerializedName("width") var width: String? = null,
    @SerializedName("height") var height: String? = null,
    @SerializedName("note") var note: String? = null
)

data class CreateUpdateInventoryRequest(
    @SerializedName("product_id") var productId: Int,
    @SerializedName("quantity") var quantity: Int? = null,
    @SerializedName("color") var color: String? = null,
    @SerializedName("size") var size: String? = null,
    @SerializedName("weight") var weight: Double? = null,
    @SerializedName("price_cents") var priceCents: Int? = null,
    @SerializedName("sale_price_cents") var salePriceCents: Int? = null,
    @SerializedName("cost_cents") var costCents: Int? = null,
    @SerializedName("sku") var sku: String? = null,
    @SerializedName("length") var length: Double? = null,
    @SerializedName("width") var width: Double? = null,
    @SerializedName("height") var height: Double? = null,
    @SerializedName("note") var note: String? = null
)

data class CreateUpdateDeleteInventoryResponse(
    @SerializedName("message") var message: String,
    @SerializedName("inventory_id") var inventoryId: Int? = null
)

fun List<InventoryResponse>.toDatabaseInventory() = map {
    DatabaseInventory(
        id = it.id,
        productId = it.productId,
        quantity = it.quantity,
        color = it.color,
        size = it.size,
        weight = it.weight,
        priceCents = it.priceCents,
        salePriceCents = it.salePriceCents,
        costCents = it.costCents,
        sku = it.sku,
        length = it.length,
        width = it.width,
        height = it.height,
        note = it.note
    )
}










