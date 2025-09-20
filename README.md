# ErrorHandler Library

[![Release](https://jitpack.io/v/Morteza-Sakiyan/ErrorHandlerExample.svg)](https://jitpack.io/#Morteza-Sakiyan/ErrorHandlerExample)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-blue.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A comprehensive error handling library for Android applications that provides a clean and flexible way to manage API errors, network issues, and validation problems.

## Features

- 🚀 **Simple Integration** - Easy to integrate with existing projects
- 🎯 **Type Safe** - Full type safety with Kotlin generics
- 🔄 **Retry Mechanism** - Built-in retry logic for network errors
- 📱 **MVVM Ready** - Perfect for MVVM architecture
- 🌐 **Network Error Handling** - Comprehensive network error management
- ✅ **Form Validation** - Built-in form validation error handling
- 🎨 **Customizable** - Register custom error entities
- 📊 **Multiple Error Types** - Support for various error scenarios

## Installation

Add the JitPack repository to your project's `build.gradle`:

```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

Add the dependency to your app's `build.gradle`:

```gradle
dependencies {
    implementation 'com.github.your-username:ErrorHandlerExample:version'
}
```

## Quick Start

### 1. Register Error Entities

```kotlin
// Register your custom error entities
ErrorEntityRegistry.register(MyErrorEntity::class.java)
```

### 2. Handle API Errors

```kotlin
try {
    val result = apiCall()
} catch (e: Exception) {
    val apiError = traceErrorException(e)
    handleError(apiError)
}
```

### 3. Check Error Types

```kotlin
when (apiError.errorStatus) {
    ApiError.ErrorStatus.DATA_ERROR -> {
        // Handle data error
    }
    ApiError.ErrorStatus.NO_CONNECTION -> {
        // Handle connection error
    }
    ApiError.ErrorStatus.TIMEOUT -> {
        // Handle timeout error
    }
    // ... other error types
}
```

## Usage Examples

### Simple API Error Handling

```kotlin
class MyApiService {
    fun fetchUser(userId: String, callback: (Result<User>) -> Unit) {
        // Register error entities
        ErrorEntityRegistry.register(UserNotFoundError::class.java)
        ErrorEntityRegistry.register(ServerError::class.java)
        
        api.getUser(userId).enqueue(object : Callback<User> {
            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    callback(Result.success(response.body()!!))
                } else {
                    val error = traceErrorException(HttpException(response))
                    handleError(error)
                    callback(Result.failure(Exception(error.getErrorMessage())))
                }
            }
            
            override fun onFailure(call: Call<User>, t: Throwable) {
                val error = traceErrorException(t)
                handleError(error)
                callback(Result.failure(t))
            }
        })
    }
}
```

### MVVM Integration

```kotlin
class UserViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    
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
}
```

### Form Validation

```kotlin
class FormValidationExample {
    fun submitForm(form: ContactForm, callback: (Result<Contact>) -> Unit) {
        // Register validation error entities
        ErrorEntityRegistry.register(FormValidationError::class.java)
        ErrorEntityRegistry.register(FieldValidationError::class.java)
        
        // Submit form and handle validation errors
        submitFormData(form) { result ->
            result.fold(
                onSuccess = { contact -> callback(Result.success(contact)) },
                onFailure = { error -> 
                    val apiError = traceErrorException(error)
                    val validationResult = handleValidationError(apiError)
                    callback(Result.failure(ValidationException(validationResult)))
                }
            )
        }
    }
}
```

## Error Types

The library supports various error types:

- **DATA_ERROR** - Custom data validation errors
- **UNAUTHORIZED** - Authentication errors (401)
- **FORBIDDEN** - Permission errors (403)
- **NOT_FOUND** - Resource not found (404)
- **TIMEOUT** - Request timeout
- **NO_CONNECTION** - Network connection issues
- **INTERNAL_SERVER_ERROR** - Server errors (500)
- **UNKNOWN_ERROR** - Unhandled errors

## Custom Error Entities

Create your own error entities by implementing `IErrorEntity`:

```kotlin
data class MyCustomError(
    override val message: String,
    val errorCode: String,
    val details: Map<String, String>
) : IErrorEntity

// Register it
ErrorEntityRegistry.register(MyCustomError::class.java)
```

## Requirements

- Android API 24+
- Kotlin 2.2.10+
- Retrofit 3.0.0+
- OkHttp 5.1.0+

## Sample App

This repository includes a comprehensive sample app demonstrating:

- Simple API error handling
- MVVM architecture integration
- Form validation error handling
- Network error management with retry
- Multiple error type handling

Run the sample app to see the library in action!

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

If you find this library helpful, please give it a ⭐ on GitHub!

---

**Made with ❤️ for the Android community**