package com.srmstudios.commentsold.util

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentSoldPrefsManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val pref: SharedPreferences
    private val editor: SharedPreferences.Editor
    private val PRIVATE_MODE = 0

    init {
        pref = context.getSharedPreferences(PREFS_NAME, PRIVATE_MODE)
        editor = pref.edit()
    }

    fun clear() {
        editor.clear()
        editor.commit()
    }

    val isUserLoggedIn: Boolean
        get() = !pref.getString(
            PREFS_JWT_TOKEN,
            ""
        ).isNullOrEmpty()

    var jwtToken: String?
        get() = pref.getString(
            PREFS_JWT_TOKEN,
            ""
        )
        set(token) {
            editor.putString(
                PREFS_JWT_TOKEN,
                token
            ).apply()
        }

    companion object {
        const val PREFS_NAME = "prefs_comment_sold"
        const val PREFS_JWT_TOKEN = "PREFS_AZURE_ACCESS_TOKEN"
    }
}