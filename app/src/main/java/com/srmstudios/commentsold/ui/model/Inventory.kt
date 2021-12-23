package com.srmstudios.commentsold.ui.model

import android.os.Parcelable
import com.srmstudios.commentsold.data.database.entity.DatabaseProduct
import kotlinx.parcelize.Parcelize

@Parcelize
data class Inventory(
    var id: Int,
    var productId: Int,
    var productName: String? = null,
    var quantity: Int? = null,
    var color: String? = null,
    var size: String? = null,
    var weight: String? = null,
    var priceCents: Int? = null,
    var salePriceCents: Int? = null,
    var costCents: Int? = null,
    var sku: String? = null,
    var length: String? = null,
    var width: String? = null,
    var height: String? = null,
    var note: String? = null
) : Parcelable
