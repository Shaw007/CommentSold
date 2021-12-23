package com.srmstudios.commentsold.util

import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection
import javax.inject.Inject

class CommentSoldRequestInterceptor @Inject constructor(
    private val commentSoldPrefsManager: CommentSoldPrefsManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val builder = request.newBuilder()
        builder.header(
            AUTHORIZATION,
            "$BEARER ${commentSoldPrefsManager.jwtToken}"
        )
        request = builder.build()
        val response = chain.proceed(request)
        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            // refresh token or logout user

        }
        return response
    }
}