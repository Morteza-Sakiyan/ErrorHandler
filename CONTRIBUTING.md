# Contributing to ErrorHandler Library

Thank you for your interest in contributing to the ErrorHandler library! This document provides guidelines and information for contributors.

## How to Contribute

### Reporting Issues

Before creating an issue, please:

1. Check if the issue already exists
2. Use the latest version of the library
3. Provide a clear description of the problem
4. Include steps to reproduce the issue
5. Add relevant code snippets or error logs

### Submitting Pull Requests

1. **Fork the repository**
2. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes** following the coding standards
4. **Add tests** for new functionality
5. **Update documentation** if needed
6. **Commit your changes** with clear commit messages
7. **Push to your fork** and create a Pull Request

## Development Setup

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK with API level 24+
- Git

### Setup Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/ErrorHandlerExample.git
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Run the sample app to verify everything works

## Coding Standards

### Kotlin Style

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions small and focused

### Code Formatting

- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use trailing commas in multi-line declarations
- Prefer `val` over `var` when possible

### Example Code Style

```kotlin
/**
 * Handles API errors and converts them to appropriate error entities.
 * 
 * @param throwable The exception to handle
 * @return ApiError with appropriate error status and entity
 */
fun traceErrorException(throwable: Throwable?): ApiError<out IErrorEntity> {
    return when (throwable) {
        is HttpException -> {
            val code = throwable.code()
            val errorBody = throwable.response()?.errorBody()?.string()
            convertErrorBody(errorBody, code)
        }
        is SocketTimeoutException -> {
            ApiError(null, ApiError.ErrorStatus.TIMEOUT)
        }
        is IOException -> {
            ApiError(null, ApiError.ErrorStatus.NO_CONNECTION)
        }
        else -> {
            ApiError(null, ApiError.ErrorStatus.UNKNOWN_ERROR)
        }
    }
}
```

## Testing

### Unit Tests

- Write unit tests for all new functionality
- Aim for high test coverage
- Use descriptive test names
- Test both success and failure scenarios

### Example Test

```kotlin
@Test
fun `traceErrorException should return TIMEOUT status for SocketTimeoutException`() {
    // Given
    val timeoutException = SocketTimeoutException("Request timeout")
    
    // When
    val result = traceErrorException(timeoutException)
    
    // Then
    assertEquals(ApiError.ErrorStatus.TIMEOUT, result.errorStatus)
    assertNull(result.code)
}
```

## Documentation

### API Documentation

- Add KDoc comments for all public APIs
- Include parameter descriptions
- Provide usage examples
- Document any exceptions that might be thrown

### README Updates

- Update README.md for new features
- Add usage examples
- Update installation instructions if needed
- Keep the changelog updated

## Release Process

### Versioning

We follow [Semantic Versioning](https://semver.org/):

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Release Checklist

- [ ] Update version in `build.gradle.kts`
- [ ] Update CHANGELOG.md
- [ ] Update README.md if needed
- [ ] Run all tests
- [ ] Create release tag
- [ ] Publish to JitPack

## Community Guidelines

### Code of Conduct

- Be respectful and inclusive
- Provide constructive feedback
- Help others learn and grow
- Follow the [Contributor Covenant Code of Conduct](https://www.contributor-covenant.org/)

### Communication

- Use clear and concise language
- Provide context for your suggestions
- Be patient with responses
- Ask questions if something is unclear

## Getting Help

If you need help:

1. Check the [README.md](README.md) for documentation
2. Look at existing issues and discussions
3. Create a new issue with the "question" label
4. Join our community discussions

## Recognition

Contributors will be recognized in:

- CONTRIBUTORS.md file
- Release notes
- Project documentation

Thank you for contributing to the ErrorHandler library! 🚀
