package com.srmstudios.commentsold.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.StringRes
import com.google.gson.Gson
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.data.network.model.ErrorResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import okio.IOException
import retrofit2.HttpException
import java.text.NumberFormat
import java.util.*
import javax.inject.Inject

fun isEmailValid(email: String?): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun convertCentsToDollars(priceInCents: Int?): String {
    val dollars = (priceInCents?.toDouble() ?: 0.0) / 100.0
    return NumberFormat.getCurrencyInstance(Locale.US).format(dollars)
}

enum class TRIGGER {
    TRIGGER
}

class Util @Inject constructor(
    @ApplicationContext private val applicationContext: Context
) {
    fun parseApiErrorThrowable(ex: Throwable?): String {
        return when (ex) {
            is HttpException -> {
                val messageToShow = when (ex.code()) {
                    CODE_INTERNAL_SERVER_ERROR -> {
                        // parse error json body
                        val errorResponseString = ex.response()?.errorBody()?.string()
                        Gson().fromJson(errorResponseString, ErrorResponse::class.java)?.error
                            ?: applicationContext.getString(R.string.something_went_wrong)
                    }
                    CODE_UNAUTHORIZED -> {
                        applicationContext.getString(R.string.user_not_found)
                    }
                    else -> {
                        applicationContext.getString(R.string.something_went_wrong)
                    }
                }
                messageToShow
            }
            is IOException -> {
                if(isNetworkAvailable()){
                    applicationContext.getString(R.string.please_check_internent)
                }else {
                    applicationContext.getString(R.string.weak_internet_connection)
                }
            }
            else -> {
                applicationContext.getString(R.string.something_went_wrong)
            }
        }
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
        } else {
            // for API levels below Android Q
            try {
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    fun getStringByResId(@StringRes stringResId: Int) = applicationContext.getString(stringResId)
}