package com.srmstudios.commentsold.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.srmstudios.commentsold.ui.model.Product

@Entity(tableName = "product")
data class DatabaseProduct(
    @PrimaryKey @ColumnInfo(name = "id") var id: Int,
    @ColumnInfo(name = "product_name") var productName: String? = null,
    @ColumnInfo(name = "description") var description: String? = null,
    @ColumnInfo(name = "style") var style: String? = null,
    @ColumnInfo(name = "brand") var brand: String? = null,
    @ColumnInfo(name = "created_at") var createdAt: String? = null,
    @ColumnInfo(name = "updated_at") var updatedAt: String? = null,
    @ColumnInfo(name = "url") var url: String? = null,
    @ColumnInfo(name = "product_type") var productType: String? = null,
    @ColumnInfo(name = "shipping_price") var shippingPrice: Int? = null,
    @ColumnInfo(name = "note") var note: String? = null,
    @ColumnInfo(name = "admin_id") var adminId: Int? = null
)

fun DatabaseProduct.toProduct(): Product {
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

fun List<DatabaseProduct>.toProducts() = map {
    Product(
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