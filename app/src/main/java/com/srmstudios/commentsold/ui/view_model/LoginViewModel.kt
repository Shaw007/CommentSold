package com.srmstudios.commentsold.ui.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.srmstudios.commentsold.R
import com.srmstudios.commentsold.data.repo.UserRepository
import com.srmstudios.commentsold.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import okhttp3.Credentials
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val commentSoldPrefsManager: CommentSoldPrefsManager,
    private val userRepository: UserRepository,
    private val util: Util
) : ViewModel() {

    private var _message = MutableLiveData<String?>()
    val message: LiveData<String?>
        get() = _message

    private var _navigateToHomeScreen = MutableLiveData<Boolean>()
    val navigateToHomeScreen: LiveData<Boolean>
        get() = _navigateToHomeScreen

    private var _progressBar = MutableLiveData<Boolean>()
    val progressBar: LiveData<Boolean>
        get() = _progressBar

    private fun login(email: String, password: String) = viewModelScope.launch {
        var credentials: String =
            Credentials.basic(email, password)

        userRepository.login(credentials).collect { result ->
            _progressBar.value = result is Resource.Loading

            when (result) {
                is Resource.Success -> {
                    result.data?.token?.let { token ->
                        commentSoldPrefsManager.jwtToken = token
                        _navigateToHomeScreen.value = true
                    }
                }
                is Resource.Error -> {
                    val error = util.parseApiErrorThrowable(result.error)
                    _message.value = error
                }
            }
        }
    }

    fun validateCredentialsAndLogin(email: String?, password: String?) {
        if (!util.isNetworkAvailable()) {
            _message.value = util.getStringByResId(R.string.please_check_internent)
            return
        }
        if (email.isNullOrEmpty() || !isEmailValid(email)) {
            _message.value = util.getStringByResId(R.string.please_enter_valid_email)
            return
        }
        if (password.isNullOrEmpty()) {
            _message.value = util.getStringByResId(R.string.please_enter_password)
            return
        }
        login(email, password)
    }

    fun doneShowingMessage() {
        _message.value = null
    }

    fun doneNavigatingToHomeScreen() {
        _navigateToHomeScreen.value = false
    }
}











