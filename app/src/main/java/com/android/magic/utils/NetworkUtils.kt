package com.android.magic.utils

sealed class ApiResult<out T : Any> {
    data class Success<out T : Any>(val data: T) : ApiResult<T>()
    data class NetworkError(val e: Exception) : ApiResult<Nothing>()
}

inline fun <T : Any> apiResult(block: () -> T): ApiResult<T> = try {
    ApiResult.Success(block())
} catch (e: Exception) {
    ApiResult.NetworkError(e)
}
