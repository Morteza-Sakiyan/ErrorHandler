package com.sepanta.controlkit.errorhandler.utils
/*
 *  File: ErrorEntityRegistry.kt
 *
 *  Created by morteza on 9/9/25.
 */
import com.google.gson.Gson
import com.sepanta.controlkit.errorhandler.ApiError
import com.sepanta.controlkit.errorhandler.ErrorEntityRegistry
import com.sepanta.controlkit.errorhandler.GenericErrorEntity
import com.sepanta.controlkit.errorhandler.IErrorEntity

fun convertErrorBody(
    error: String?,
    code: Int?
): ApiError<out IErrorEntity> {
    return try {
        val gson = Gson()
        var parsedEntity: IErrorEntity? = null

        if (error != null) {
            for (clazz in ErrorEntityRegistry.getAllEntities()) {
                try {
                    parsedEntity = gson.fromJson(error, clazz)
                    if (parsedEntity?.message != null) {
                        break
                    }
                } catch (_: Exception) {
                }
            }
        }

        if (parsedEntity != null) {
            ApiError(
                code = code,
                errorStatus = ApiError.ErrorStatus.DATA_ERROR,
                errorEntity = parsedEntity
            )
        } else {
            ApiError(
                code = code,
                errorStatus = ApiError.ErrorStatus.UNKNOWN_ERROR,
                errorEntity = GenericErrorEntity(
                    message = "Unhandled error",
                    code = code,
                    rawJson = error
                )
            )
        }
    } catch (ex: Exception) {
        ex.printStackTrace()
        ApiError(
            code = code,
            errorStatus = ApiError.ErrorStatus.UNKNOWN_ERROR,
            errorEntity = GenericErrorEntity(
                message = "Parsing error",
                code = code,
                rawJson = error
            )
        )
    }
}


