package com.sepanta.controlkit.errorhandler
/*
 *  File: ErrorEntityRegistry.kt
 *
 *  Created by morteza on 9/9/25.
 */
import com.sepanta.controlkit.errorhandler.utils.convertErrorBody
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException


fun traceErrorException(throwable: Throwable?): ApiError<out IErrorEntity> {

    return when (throwable) {

        is HttpException -> {
            val code = throwable.code()
            val errorBody = throwable.response()?.errorBody()?.string()
            convertErrorBody(errorBody, code)
        }
        is SocketTimeoutException ->
            ApiError(null, ApiError.ErrorStatus.TIMEOUT)


        is IOException -> {
            ApiError(null, ApiError.ErrorStatus.NO_CONNECTION)
        }

        else -> ApiError(
            null,
            ApiError.ErrorStatus.UNKNOWN_ERROR
        )
    }
}