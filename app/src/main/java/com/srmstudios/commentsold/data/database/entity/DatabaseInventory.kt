package com.srmstudios.commentsold.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.srmstudios.commentsold.ui.model.Inventory

@Entity(tableName = "inventory")
data class DatabaseInventory(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "product_id") var productId: Int,
    @ColumnInfo(name = "quantity") var quantity: Int? = null,
    @ColumnInfo(name = "color") var color: String? = null,
    @ColumnInfo(name = "size") var size: String? = null,
    @ColumnInfo(name = "weight") var weight: String? = null,
    @ColumnInfo(name = "price_cents") var priceCents: Int? = null,
    @ColumnInfo(name = "sale_price_cents") var salePriceCents: Int? = null,
    @ColumnInfo(name = "cost_cents") var costCents: Int? = null,
    @ColumnInfo(name = "sku") var sku: String? = null,
    @ColumnInfo(name = "length") var length: String? = null,
    @ColumnInfo(name = "width") var width: String? = null,
    @ColumnInfo(name = "height") var height: String? = null,
    @ColumnInfo(name = "note") var note: String? = null
)

data class DatabaseInventoryJoinProduct(
    @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "product_id") var productId: Int,
    @ColumnInfo(name = "product_name") var productName: String? = null,
    @ColumnInfo(name = "quantity") var quantity: Int? = null,
    @ColumnInfo(name = "color") var color: String? = null,
    @ColumnInfo(name = "size") var size: String? = null,
    @ColumnInfo(name = "weight") var weight: String? = null,
    @ColumnInfo(name = "price_cents") var priceCents: Int? = null,
    @ColumnInfo(name = "sale_price_cents") var salePriceCents: Int? = null,
    @ColumnInfo(name = "cost_cents") var costCents: Int? = null,
    @ColumnInfo(name = "sku") var sku: String? = null,
    @ColumnInfo(name = "length") var length: String? = null,
    @ColumnInfo(name = "width") var width: String? = null,
    @ColumnInfo(name = "height") var height: String? = null,
    @ColumnInfo(name = "note") var note: String? = null
)

fun DatabaseInventory.toInventory(): Inventory {
    return Inventory(
        id = id,
        productId = productId,
        quantity = quantity,
        color = color,
        size = size,
        weight = weight,
        priceCents = priceCents,
        salePriceCents = salePriceCents,
        costCents = costCents,
        sku = sku,
        length = length,
        width = width,
        height = height,
        note = note
    )
}

fun List<DatabaseInventory>.toInventory() = map {
    Inventory(
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

fun List<DatabaseInventoryJoinProduct>.toInventoryJoinProduct() = map {
    Inventory(
        id = it.id,
        productId = it.productId,
        productName = it.productName,
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