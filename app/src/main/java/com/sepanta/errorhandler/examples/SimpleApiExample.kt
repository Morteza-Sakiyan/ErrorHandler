package com.sepanta.errorhandler.examples

import android.util.Log
import com.sepanta.controlkit.errorhandler.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Simple example for API error handling
 * This example shows how to use the ErrorHandler library
 */

// Define error entities
data class UserNotFoundError(
    override val message: String,
    val userId: String,
    val timestamp: String
) : IErrorEntity

data class ServerError(
    override val message: String,
    val errorCode: String,
    val details: String
) : IErrorEntity

// Define API Service
interface UserApiService {
    @GET("users/{id}")
    fun getUser(@Path("id") userId: String): Call<User>
}

data class User(
    val id: String,
    val name: String,
    val email: String
)

class SimpleApiExample {
    
    fun fetchUser(userId: String, callback: (Result<User>) -> Unit) {
        // Register error entities
        ErrorEntityRegistry.register(UserNotFoundError::class.java)
        ErrorEntityRegistry.register(ServerError::class.java)
        
        // Simulate API call (in reality you would use Retrofit)
        simulateApiCall(userId) { result ->
            result.fold(
                onSuccess = { user ->
                    callback(Result.success(user))
                },
                onFailure = { throwable ->
                    val apiError = traceErrorException(throwable)
                    handleError(apiError)
                    callback(Result.failure(throwable))
                }
            )
        }
    }
    
    private fun simulateApiCall(userId: String, callback: (Result<User>) -> Unit) {
        // Simulate different errors
        when (userId) {
            "404" -> {
                val error = HttpException(
                    Response.error<User>(
                        404,
                        ResponseBody.create(
                            "application/json".toMediaType(),
                            """{"message": "User not found", "userId": "$userId", "timestamp": "${System.currentTimeMillis()}"}"""
                        )
                    )
                )
                callback(Result.failure(error))
            }
            "500" -> {
                val error = HttpException(
                    Response.error<User>(
                        500,
                        ResponseBody.create(
                            "application/json".toMediaType(),
                            """{"message": "Internal server error", "errorCode": "ERR_500", "details": "Database connection failed"}"""
                        )
                    )
                )
                callback(Result.failure(error))
            }
            "timeout" -> {
                callback(Result.failure(SocketTimeoutException("Request timeout")))
            }
            "network" -> {
                callback(Result.failure(IOException("No internet connection")))
            }
            else -> {
                // Success
                callback(Result.success(User(userId, "John Doe", "john@example.com")))
            }
        }
    }
    
    private fun handleError(apiError: ApiError<out IErrorEntity>) {
        Log.d("SimpleApiExample", "Error occurred: ${apiError.getErrorMessage()}")
        
        when (apiError.errorStatus) {
            ApiError.ErrorStatus.DATA_ERROR -> {
                val entity = apiError.errorEntity
                when (entity) {
                    is UserNotFoundError -> {
                        Log.e("SimpleApiExample", "User not found: ${entity.userId}")
                        // Show error message to user
                        showUserNotFoundError(entity)
                    }
                    is ServerError -> {
                        Log.e("SimpleApiExample", "Server error: ${entity.errorCode} - ${entity.details}")
                        // Show server error to user
                        showServerError(entity)
                    }
                }
            }
            ApiError.ErrorStatus.TIMEOUT -> {
                Log.e("SimpleApiExample", "Request timeout")
                showTimeoutError()
            }
            ApiError.ErrorStatus.NO_CONNECTION -> {
                Log.e("SimpleApiExample", "No internet connection")
                showNetworkError()
            }
            ApiError.ErrorStatus.NOT_FOUND -> {
                Log.e("SimpleApiExample", "Resource not found")
                showNotFoundError()
            }
            else -> {
                Log.e("SimpleApiExample", "Unknown error: ${apiError.getErrorMessage()}")
                showGenericError()
            }
        }
    }
    
    private fun showUserNotFoundError(error: UserNotFoundError) {
        // In reality you would update UI here
        Log.i("SimpleApiExample", "Showing user not found error for user: ${error.userId}")
    }
    
    private fun showServerError(error: ServerError) {
        Log.i("SimpleApiExample", "Showing server error: ${error.errorCode}")
    }
    
    private fun showTimeoutError() {
        Log.i("SimpleApiExample", "Showing timeout error")
    }
    
    private fun showNetworkError() {
        Log.i("SimpleApiExample", "Showing network error")
    }
    
    private fun showNotFoundError() {
        Log.i("SimpleApiExample", "Showing not found error")
    }
    
    private fun showGenericError() {
        Log.i("SimpleApiExample", "Showing generic error")
    }
}
