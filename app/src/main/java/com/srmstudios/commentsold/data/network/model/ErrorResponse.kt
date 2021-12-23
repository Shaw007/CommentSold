package com.srmstudios.commentsold.data.network.model

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("error") var error: String? = null
)