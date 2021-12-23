package com.srmstudios.commentsold.data.repo

import com.srmstudios.commentsold.data.network.ICommentSoldApi
import com.srmstudios.commentsold.di.CommentSoldBasicApi
import com.srmstudios.commentsold.util.LOGIN_API_SUCCESS_CODE
import com.srmstudios.commentsold.util.Resource
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UserRepository @Inject constructor(
    @CommentSoldBasicApi private val iCommentSoldApi: ICommentSoldApi
) {

    suspend fun login(credentials: String) = flow {
        emit(Resource.Loading(null))
        try {
            val response = iCommentSoldApi.login(
                credentials
            )
            if (response.errorCode == LOGIN_API_SUCCESS_CODE) {
                // success
                emit(Resource.Success(response))
            } else {
                emit(Resource.Success(null))
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
            emit(Resource.Error(ex, null))
        }
    }
}