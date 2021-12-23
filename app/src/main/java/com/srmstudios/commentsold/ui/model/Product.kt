package com.srmstudios.commentsold.ui.model

import android.os.Parcelable
import com.srmstudios.commentsold.data.database.entity.DatabaseProduct
import kotlinx.parcelize.Parcelize

@Parcelize
data class Product(
    var id: Int,
    var productName: String? = null,
    var description: String? = null,
    var style: String? = null,
    var brand: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null,
    var url: String? = null,
    var productType: String? = null,
    var shippingPrice: Int? = null,
    var note: String? = null,
    var adminId: Int? = null
) : Parcelable