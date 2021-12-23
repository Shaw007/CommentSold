package com.srmstudios.commentsold.di

import android.content.Context
import androidx.room.Room
import com.srmstudios.commentsold.data.database.CommentSoldDatabase
import com.srmstudios.commentsold.data.network.ICommentSoldApi
import com.srmstudios.commentsold.util.API_BASE_URL
import com.srmstudios.commentsold.util.CommentSoldRequestInterceptor
import com.srmstudios.commentsold.util.DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Datasource {

    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        return loggingInterceptor
    }

    @Provides
    @BasicInterceptorOkHttpClient
    fun provideBasicOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    @Provides
    @AuthInterceptorOkHttpClient
    fun provideAuthInterceptorOkHttpClient(
        httpLoggingInterceptor: HttpLoggingInterceptor,
        commentSoldRequestInterceptor: CommentSoldRequestInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .addInterceptor(commentSoldRequestInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @CommentSoldBasicApi
    fun provideICommentSoldBasicApi(@BasicInterceptorOkHttpClient okHttpClient: OkHttpClient): ICommentSoldApi {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ICommentSoldApi::class.java)
    }

    @Provides
    @Singleton
    @CommentSoldAuthApi
    fun provideICommentSoldAuthApi(@AuthInterceptorOkHttpClient okHttpClient: OkHttpClient): ICommentSoldApi {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(ICommentSoldApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCommentSoldDatabase(@ApplicationContext context: Context): CommentSoldDatabase =
        Room.databaseBuilder(
            context,
            CommentSoldDatabase::class.java,
            DATABASE_NAME
        ).build()

}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BasicInterceptorOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptorOkHttpClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CommentSoldBasicApi

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CommentSoldAuthApi