package com.srmstudios.commentsold.ui.view_model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.srmstudios.commentsold.util.CommentSoldPrefsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val commentSoldPrefsManager: CommentSoldPrefsManager
) : ViewModel() {

    private var _splashNavigation = MutableLiveData<SPLASH_NAVIGATION>()
    val splashNavigation
        get() = _splashNavigation

    init {
        viewModelScope.launch {
            // simulate a delay then navigate to the appropriate screen
            delay(1000)

            if (commentSoldPrefsManager.isUserLoggedIn) {
                _splashNavigation.value = SPLASH_NAVIGATION.HOME
            } else {
                _splashNavigation.value = SPLASH_NAVIGATION.LOGIN
            }
        }
    }

}

enum class SPLASH_NAVIGATION {
    LOGIN,
    HOME
}