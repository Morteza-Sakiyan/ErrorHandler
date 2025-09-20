package com.sepanta.controlkit.errorhandler.examples

import android.util.Log
import com.sepanta.controlkit.errorhandler.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.HttpException
import retrofit2.Response
import okhttp3.ResponseBody
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Comprehensive network error handling example
 * This example shows how to handle different types of network errors
 */

// Define network error entities
data class NetworkError(
    override val message: String,
    val errorCode: String,
    val retryAfter: Int? = null
) : IErrorEntity

data class RateLimitError(
    override val message: String,
    val limit: Int,
    val remaining: Int,
    val resetTime: Long
) : IErrorEntity

data class MaintenanceError(
    override val message: String,
    val estimatedDuration: String,
    val maintenanceWindow: String
) : IErrorEntity

data class ApiVersionError(
    override val message: String,
    val currentVersion: String,
    val supportedVersions: List<String>
) : IErrorEntity

// Define data models
data class Post(
    val id: String,
    val title: String,
    val content: String,
    val author: String,
    val createdAt: String
)

data class ApiResponse<T>(
    val data: T,
    val message: String,
    val success: Boolean
)

class NetworkErrorExample {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    fun fetchPostsWithRetry(
        maxRetries: Int = 3,
        callback: (Result<List<Post>>) -> Unit
    ) {
        scope.launch {
            var retryCount = 0
            var lastError: Exception? = null
            
            while (retryCount < maxRetries) {
                try {
                    val result = fetchPosts()
                    callback(Result.success(result))
                    return@launch
                } catch (e: Exception) {
                    lastError = e
                    val apiError = traceErrorException(e)
                    val shouldRetry = shouldRetryError(apiError)
                    
                    if (!shouldRetry || retryCount == maxRetries - 1) {
                        break
                    }
                    
                    val delay = calculateRetryDelay(retryCount, apiError)
                    Log.d("NetworkErrorExample", "Retry $retryCount after ${delay}ms")
                    delay(delay)
                    retryCount++
                }
            }
            
            callback(Result.failure(lastError ?: Exception("Unknown error")))
        }
    }
    
    private suspend fun fetchPosts(): List<Post> {
        // Register error entities
        ErrorEntityRegistry.register(NetworkError::class.java)
        ErrorEntityRegistry.register(RateLimitError::class.java)
        ErrorEntityRegistry.register(MaintenanceError::class.java)
        ErrorEntityRegistry.register(ApiVersionError::class.java)
        
        // Simulate API call
        return simulateApiCall()
    }
    
    private suspend fun simulateApiCall(): List<Post> {
        // Simulate different errors
        val errorType = listOf(
            "success", "timeout", "network", "rate_limit", "maintenance", 
            "version_error", "server_error", "not_found", "unauthorized"
        ).random()
        
        return when (errorType) {
            "success" -> {
                listOf(
                    Post("1", "First Post Title", "First post content", "First Author", "2024-01-01"),
                    Post("2", "Second Post Title", "Second post content", "Second Author", "2024-01-02")
                )
            }
            "timeout" -> {
                throw SocketTimeoutException("Request timeout")
            }
            "network" -> {
                throw IOException("No internet connection")
            }
            "rate_limit" -> {
                val errorJson = """{
                    "message": "Rate limit exceeded",
                    "limit": 100,
                    "remaining": 0,
                    "resetTime": ${System.currentTimeMillis() + 3600000}
                }"""
                throw HttpException(
                    Response.error<List<Post>>(
                        429,
                        ResponseBody.create(
                            "application/json".toMediaType(),
                            errorJson
                        )
                    )
                )
            }
            "maintenance" -> {
                val errorJson = """{
                    "message": "Server is under maintenance",
                    "estimatedDuration": "2 hours",
                    "maintenanceWindow": "2024-01-01 02:00 - 04:00"
                }"""
                throw HttpException(
                    Response.error<List<Post>>(
                        503,
                        ResponseBody.create(
                            "application/json".toMediaType(),
                            errorJson
                        )
                    )
                )
            }
            "version_error" -> {
                val errorJson = """{
                    "message": "API version not supported",
                    "currentVersion": "v1.0",
                    "supportedVersions": ["v2.0", "v2.1"]
                }"""
                throw HttpException(
                    Response.error<List<Post>>(
                        400,
                        ResponseBody.create(
                            "application/json".toMediaType(),
                            errorJson
                        )
                    )
                )
            }
            "server_error" -> {
                throw HttpException(
                    Response.error<List<Post>>(
                        500,
                        ResponseBody.create(
                            "application/json".toMediaType(),
                            """{"message": "Internal server error", "errorCode": "ERR_500"}"""
                        )
                    )
                )
            }
            "not_found" -> {
                throw HttpException(
                    Response.error<List<Post>>(
                        404,
                        ResponseBody.create(
                            "application/json".toMediaType(),
                            """{"message": "Posts not found"}"""
                        )
                    )
                )
            }
            "unauthorized" -> {
                throw HttpException(
                    Response.error<List<Post>>(
                        401,
                        ResponseBody.create(
                            "application/json".toMediaType(),
                            """{"message": "Unauthorized access"}"""
                        )
                    )
                )
            }
            else -> {
                throw Exception("Unknown error")
            }
        }
    }
    
    private fun shouldRetryError(apiError: ApiError<out IErrorEntity>): Boolean {
        return when (apiError.errorStatus) {
            ApiError.ErrorStatus.TIMEOUT,
            ApiError.ErrorStatus.NO_CONNECTION,
            ApiError.ErrorStatus.INTERNAL_SERVER_ERROR -> true
            ApiError.ErrorStatus.DATA_ERROR -> {
                val entity = apiError.errorEntity
                when (entity) {
                    is RateLimitError -> true
                    is NetworkError -> entity.errorCode == "TEMPORARY_ERROR"
                    else -> false
                }
            }
            else -> false
        }
    }
    
    private fun calculateRetryDelay(retryCount: Int, apiError: ApiError<out IErrorEntity>): Long {
        return when (apiError.errorStatus) {
            ApiError.ErrorStatus.DATA_ERROR -> {
                val entity = apiError.errorEntity
                if (entity is RateLimitError) {
                    // For rate limit, use resetTime
                    maxOf(0, entity.resetTime - System.currentTimeMillis())
                } else {
                    // Exponential backoff
                    (1000 * Math.pow(2.0, retryCount.toDouble())).toLong()
                }
            }
            else -> {
                // Exponential backoff for other errors
                (1000 * Math.pow(2.0, retryCount.toDouble())).toLong()
            }
        }
    }
    
    fun handleNetworkError(apiError: ApiError<out IErrorEntity>) {
        when (apiError.errorStatus) {
            ApiError.ErrorStatus.DATA_ERROR -> {
                val entity = apiError.errorEntity
                when (entity) {
                    is RateLimitError -> {
                        Log.e("NetworkErrorExample", "Rate limit exceeded. Reset at: ${entity.resetTime}")
                        showRateLimitError(entity)
                    }
                    is MaintenanceError -> {
                        Log.e("NetworkErrorExample", "Server maintenance: ${entity.estimatedDuration}")
                        showMaintenanceError(entity)
                    }
                    is ApiVersionError -> {
                        Log.e("NetworkErrorExample", "API version error. Supported: ${entity.supportedVersions}")
                        showVersionError(entity)
                    }
                    is NetworkError -> {
                        Log.e("NetworkErrorExample", "Network error: ${entity.errorCode}")
                        showNetworkError(entity)
                    }
                }
            }
            ApiError.ErrorStatus.TIMEOUT -> {
                Log.e("NetworkErrorExample", "Request timeout")
                showTimeoutError()
            }
            ApiError.ErrorStatus.NO_CONNECTION -> {
                Log.e("NetworkErrorExample", "No internet connection")
                showConnectionError()
            }
            ApiError.ErrorStatus.UNAUTHORIZED -> {
                Log.e("NetworkErrorExample", "Unauthorized access")
                showUnauthorizedError()
            }
            ApiError.ErrorStatus.NOT_FOUND -> {
                Log.e("NetworkErrorExample", "Resource not found")
                showNotFoundError()
            }
            ApiError.ErrorStatus.INTERNAL_SERVER_ERROR -> {
                Log.e("NetworkErrorExample", "Internal server error")
                showServerError()
            }
            else -> {
                Log.e("NetworkErrorExample", "Unknown error: ${apiError.getErrorMessage()}")
                showGenericError()
            }
        }
    }
    
    // UI methods (in reality you would update UI here)
    private fun showRateLimitError(error: RateLimitError) {
        Log.i("NetworkErrorExample", "Showing rate limit error. Reset in: ${error.resetTime}")
    }
    
    private fun showMaintenanceError(error: MaintenanceError) {
        Log.i("NetworkErrorExample", "Showing maintenance error. Duration: ${error.estimatedDuration}")
    }
    
    private fun showVersionError(error: ApiVersionError) {
        Log.i("NetworkErrorExample", "Showing version error. Supported versions: ${error.supportedVersions}")
    }
    
    private fun showNetworkError(error: NetworkError) {
        Log.i("NetworkErrorExample", "Showing network error: ${error.errorCode}")
    }
    
    private fun showTimeoutError() {
        Log.i("NetworkErrorExample", "Showing timeout error")
    }
    
    private fun showConnectionError() {
        Log.i("NetworkErrorExample", "Showing connection error")
    }
    
    private fun showUnauthorizedError() {
        Log.i("NetworkErrorExample", "Showing unauthorized error")
    }
    
    private fun showNotFoundError() {
        Log.i("NetworkErrorExample", "Showing not found error")
    }
    
    private fun showServerError() {
        Log.i("NetworkErrorExample", "Showing server error")
    }
    
    private fun showGenericError() {
        Log.i("NetworkErrorExample", "Showing generic error")
    }
    
    fun cleanup() {
        scope.cancel()
    }
}
