package com.srmstudios.commentsold.data.network.model

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("error")
    var errorCode: Int,
    @SerializedName("token")
    var token: String? = null
)