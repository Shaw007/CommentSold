package com.srmstudios.commentsold.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import kotlinx.coroutines.flow.first

// A convenience method based on LiveData to handle
// fetching of data from the network and then saving it in the local database
// the method returns a LiveData returned by Room database
// and whenever the underlying data changes in the local database
// we wil be notified
// liveData { } builder by default runs on a background thread

inline fun <ResultType, RequestType> networkBoundResource(
    crossinline query: () -> LiveData<ResultType>,
    crossinline fetch: suspend () -> RequestType,
    crossinline saveFetchResult: suspend (RequestType) -> Unit,
    crossinline shouldFetch: (ResultType?) -> Boolean = { true }
) = liveData {

    // get the data already present in the database
    // live data only triggers when an observable is attached to it
    // here we are converting the live data to a flow to get the data
    val data = query().asFlow().first()

    val liveData: LiveData<Resource<ResultType>> = if (shouldFetch(data)) {
        emit(Resource.Loading(data))

        try {
            saveFetchResult(fetch())
            query().map { Resource.Success(it) }
        } catch (throwable: Throwable) {
            query().map { Resource.Error(throwable, it) }
        }
    } else {
        query().map { Resource.Success(it) }
    }

    emitSource(liveData)
}