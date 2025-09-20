# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2024-01-15

### Added
- Initial release of ErrorHandler library
- Core error handling functionality with `ApiError` and `IErrorEntity`
- Error entity registry system for custom error types
- Support for various HTTP error status codes
- Network error handling (timeout, connection issues)
- Form validation error handling
- MVVM architecture integration examples
- Comprehensive sample app with multiple examples
- Retry mechanism for network errors
- Type-safe error handling with Kotlin generics

### Features
- **Error Types Support**: DATA_ERROR, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, TIMEOUT, NO_CONNECTION, INTERNAL_SERVER_ERROR, UNKNOWN_ERROR
- **Custom Error Entities**: Register and handle custom error types
- **Network Error Management**: Comprehensive handling of network-related errors
- **Form Validation**: Built-in support for form validation errors
- **Retry Logic**: Exponential backoff retry mechanism
- **MVVM Ready**: Perfect integration with MVVM architecture
- **Type Safety**: Full type safety with Kotlin generics

### Examples Included
- Simple API error handling
- MVVM architecture integration
- Form validation error handling
- Network error management with retry
- Multiple error type handling

### Dependencies
- Android API 24+
- Kotlin 2.2.10+
- Retrofit 3.0.0+
- OkHttp 5.1.0+
- Jetpack Compose (for sample app)
- Material 3 (for sample app)

---

## Future Releases

### Planned Features
- [ ] Coroutine-based error handling
- [ ] RxJava support
- [ ] Custom error message localization
- [ ] Error analytics and reporting
- [ ] More comprehensive retry strategies
- [ ] Error caching and offline handling
