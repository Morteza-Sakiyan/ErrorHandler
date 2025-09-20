package com.sepanta.controlkit.errorhandler.examples.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sepanta.controlkit.errorhandler.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType

/**
 * MVVM example using ErrorHandler
 * This example shows how to use the library in ViewModel
 */

// Define error entities
data class LoginError(
    override val message: String,
    val field: String,
    val code: String
) : IErrorEntity

data class AuthError(
    override val message: String,
    val token: String?,
    val expiresAt: String?
) : IErrorEntity

data class ValidationError(
    override val message: String,
    val field: String,
    val value: String?
) : IErrorEntity

// Define UI State
sealed class UserUiState {
    object Loading : UserUiState()
    data class Success(val user: User) : UserUiState()
    data class Error(val message: String, val errorType: ErrorType) : UserUiState()
}

enum class ErrorType {
    NETWORK,
    VALIDATION,
    AUTHENTICATION,
    SERVER,
    UNKNOWN
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val isAuthenticated: Boolean = false
)

class UserViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    private val userRepository = UserRepository()

    init {
        // Register error entities
        ErrorEntityRegistry.register(LoginError::class.java)
        ErrorEntityRegistry.register(AuthError::class.java)
        ErrorEntityRegistry.register(ValidationError::class.java)
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading

            try {
                val result = userRepository.login(email, password)
                _uiState.value = UserUiState.Success(result)
            } catch (e: Exception) {
                val apiError = traceErrorException(e)
                val errorState = handleApiError(apiError)
                _uiState.value = errorState
            }
        }
    }

    fun registerUser(name: String, email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading

            try {
                val result = userRepository.register(name, email, password)
                _uiState.value = UserUiState.Success(result)
            } catch (e: Exception) {
                val apiError = traceErrorException(e)
                val errorState = handleApiError(apiError)
                _uiState.value = errorState
            }
        }
    }

    fun getUserProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = UserUiState.Loading

            try {
                val result = userRepository.getUserProfile(userId)
                _uiState.value = UserUiState.Success(result)
            } catch (e: Exception) {
                val apiError = traceErrorException(e)
                val errorState = handleApiError(apiError)
                _uiState.value = errorState
            }
        }
    }

    private fun handleApiError(apiError: ApiError<out IErrorEntity>): UserUiState.Error {
        return when (apiError.errorStatus) {
            ApiError.ErrorStatus.DATA_ERROR -> {
                val entity = apiError.errorEntity
                when (entity) {
                    is LoginError -> {
                        UserUiState.Error(
                            message = "Login error: ${entity.message}",
                            errorType = ErrorType.AUTHENTICATION
                        )
                    }

                    is AuthError -> {
                        UserUiState.Error(
                            message = "Authentication error: ${entity.message}",
                            errorType = ErrorType.AUTHENTICATION
                        )
                    }

                    is ValidationError -> {
                        UserUiState.Error(
                            message = "Validation error: ${entity.message}",
                            errorType = ErrorType.VALIDATION
                        )
                    }

                    else -> {
                        UserUiState.Error(
                            message = apiError.getErrorMessage() ?: "Unknown error",
                            errorType = ErrorType.UNKNOWN
                        )
                    }
                }
            }

            ApiError.ErrorStatus.UNAUTHORIZED -> {
                UserUiState.Error(
                    message = "Unauthorized access. Please login again.",
                    errorType = ErrorType.AUTHENTICATION
                )
            }

            ApiError.ErrorStatus.FORBIDDEN -> {
                UserUiState.Error(
                    message = "You don't have permission for this operation.",
                    errorType = ErrorType.AUTHENTICATION
                )
            }

            ApiError.ErrorStatus.NOT_FOUND -> {
                UserUiState.Error(
                    message = "User not found.",
                    errorType = ErrorType.VALIDATION
                )
            }

            ApiError.ErrorStatus.TIMEOUT -> {
                UserUiState.Error(
                    message = "Request timeout. Please try again.",
                    errorType = ErrorType.NETWORK
                )
            }

            ApiError.ErrorStatus.NO_CONNECTION -> {
                UserUiState.Error(
                    message = "No internet connection. Please check your connection.",
                    errorType = ErrorType.NETWORK
                )
            }

            ApiError.ErrorStatus.INTERNAL_SERVER_ERROR -> {
                UserUiState.Error(
                    message = "Server error. Please try again later.",
                    errorType = ErrorType.SERVER
                )
            }

            else -> {
                UserUiState.Error(
                    message = apiError.getErrorMessage() ?: "Unexpected error occurred.",
                    errorType = ErrorType.UNKNOWN
                )
            }
        }
    }
}

// Repository for simulating API calls
class UserRepository {

    suspend fun login(email: String, password: String): User {
        // Simulate API call
        return when {
            email.isEmpty() -> throw createValidationException("email", "Email cannot be empty")
            password.isEmpty() -> throw createValidationException(
                "password",
                "Password cannot be empty"
            )

            email == "invalid@example.com" -> throw createLoginException("Invalid email or password")
            email == "blocked@example.com" -> throw createAuthException("Account is blocked")
            else -> User("1", "Test User", email, true)
        }
    }

    suspend fun register(name: String, email: String, password: String): User {
        return when {
            name.isEmpty() -> throw createValidationException("name", "Name cannot be empty")
            email.isEmpty() -> throw createValidationException("email", "Email cannot be empty")
            password.length < 6 -> throw createValidationException(
                "password",
                "Password must be at least 6 characters"
            )

            email == "exists@example.com" -> throw createValidationException(
                "email",
                "This email is already registered"
            )

            else -> User("2", name, email, true)
        }
    }

    suspend fun getUserProfile(userId: String): User {
        return when (userId) {
            "404" -> throw createNotFoundException("User not found")
            "500" -> throw createServerException("Server error")
            "timeout" -> throw java.net.SocketTimeoutException("Request timeout")
            "network" -> throw java.io.IOException("No internet connection")
            else -> User(userId, "Sample User", "user@example.com", true)
        }
    }

    private fun createValidationException(field: String, message: String): retrofit2.HttpException {
        val errorJson = """{"message": "$message", "field": "$field", "value": null}"""
        return retrofit2.HttpException(
            retrofit2.Response.error<User>(
                422,
                okhttp3.ResponseBody.create(
                    "application/json".toMediaType(),
                    errorJson
                )
            )
        )
    }

    private fun createLoginException(message: String): retrofit2.HttpException {
        val errorJson =
            """{"message": "$message", "field": "credentials", "code": "INVALID_CREDENTIALS"}"""
        return retrofit2.HttpException(
            retrofit2.Response.error<User>(
                401,
                okhttp3.ResponseBody.create(
                    "application/json".toMediaType(),
                    errorJson
                )
            )
        )
    }

    private fun createAuthException(message: String): retrofit2.HttpException {
        val errorJson = """{"message": "$message", "token": null, "expiresAt": null}"""
        return retrofit2.HttpException(
            retrofit2.Response.error<User>(
                403,
                okhttp3.ResponseBody.create(
                    "application/json".toMediaType(),
                    errorJson
                )
            )
        )
    }

    private fun createNotFoundException(message: String): retrofit2.HttpException {
        return retrofit2.HttpException(
            retrofit2.Response.error<User>(
                404,
                okhttp3.ResponseBody.create(
                    "application/json".toMediaType(),
                    """{"message": "$message"}"""
                )
            )
        )
    }

    private fun createServerException(message: String): retrofit2.HttpException {
        return retrofit2.HttpException(
            retrofit2.Response.error<User>(
                500,
                okhttp3.ResponseBody.create(
                    "application/json".toMediaType(),
                    """{"message": "$message"}"""
                )
            )
        )
    }
}
