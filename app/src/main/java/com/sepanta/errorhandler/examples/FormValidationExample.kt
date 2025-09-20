package com.sepanta.errorhandler.examples

import android.util.Patterns
import com.sepanta.controlkit.errorhandler.*
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.HttpException
import retrofit2.Response
import okhttp3.ResponseBody

/**
 * Form validation error handling example
 * This example shows how to handle validation errors
 */

// Define validation error entities
data class FormValidationError(
    override val message: String,
    val field: String,
    val value: String?,
    val code: String
) : IErrorEntity

data class FieldValidationError(
    override val message: String,
    val field: String,
    val errors: List<String>
) : IErrorEntity

data class BusinessRuleError(
    override val message: String,
    val rule: String,
    val context: Map<String, String>
) : IErrorEntity

// Define form model
data class ContactForm(
    val name: String,
    val email: String,
    val phone: String,
    val message: String,
    val age: Int?
)

data class Contact(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val message: String
)

class FormValidationExample {
    
    fun submitContactForm(form: ContactForm, callback: (Result<Contact>) -> Unit) {
        // Register error entities
        ErrorEntityRegistry.register(FormValidationError::class.java)
        ErrorEntityRegistry.register(FieldValidationError::class.java)
        ErrorEntityRegistry.register(BusinessRuleError::class.java)
        
        // Simulate form submission
        simulateFormSubmission(form) { result ->
            result.fold(
                onSuccess = { contact ->
                    callback(Result.success(contact))
                },
                onFailure = { throwable ->
                    val apiError = traceErrorException(throwable)
                    val validationResult = handleValidationError(apiError)
                    callback(Result.failure(ValidationException(validationResult)))
                }
            )
        }
    }
    
    private fun simulateFormSubmission(form: ContactForm, callback: (Result<Contact>) -> Unit) {
        // Simulate different validation errors
        when {
            form.name.isEmpty() -> {
                val error = createFieldValidationError("name", listOf("Name cannot be empty"))
                callback(Result.failure(error))
            }
            form.email.isEmpty() -> {
                val error = createFieldValidationError("email", listOf("Email cannot be empty"))
                callback(Result.failure(error))
            }
            !isValidEmail(form.email) -> {
                val error = createFormValidationError("Invalid email format", "email", form.email, "INVALID_EMAIL")
                callback(Result.failure(error))
            }
            form.phone.isEmpty() -> {
                val error = createFieldValidationError("phone", listOf("Phone number cannot be empty"))
                callback(Result.failure(error))
            }
            !isValidPhone(form.phone) -> {
                val error = createFormValidationError("Invalid phone number format", "phone", form.phone, "INVALID_PHONE")
                callback(Result.failure(error))
            }
            form.message.isEmpty() -> {
                val error = createFieldValidationError("message", listOf("Message cannot be empty"))
                callback(Result.failure(error))
            }
            form.message.length < 10 -> {
                val error = createFormValidationError("Message must be at least 10 characters", "message", form.message, "MIN_LENGTH")
                callback(Result.failure(error))
            }
            form.age != null && form.age < 18 -> {
                val error = createBusinessRuleError(
                    "Age must be at least 18 years",
                    "MIN_AGE",
                    mapOf("current_age" to form.age.toString(), "min_age" to "18")
                )
                callback(Result.failure(error))
            }
            form.email == "duplicate@example.com" -> {
                val error = createBusinessRuleError(
                    "This email is already registered",
                    "DUPLICATE_EMAIL",
                    mapOf("email" to form.email)
                )
                callback(Result.failure(error))
            }
            else -> {
                // Success
                val contact = Contact(
                    id = "1",
                    name = form.name,
                    email = form.email,
                    phone = form.phone,
                    message = form.message
                )
                callback(Result.success(contact))
            }
        }
    }
    
    private fun handleValidationError(apiError: ApiError<out IErrorEntity>): ValidationResult {
        return when (apiError.errorStatus) {
            ApiError.ErrorStatus.DATA_ERROR -> {
                val entity = apiError.errorEntity
                when (entity) {
                    is FormValidationError -> {
                        ValidationResult.FieldError(
                            field = entity.field,
                            message = entity.message,
                            code = entity.code,
                            value = entity.value
                        )
                    }
                    is FieldValidationError -> {
                        ValidationResult.MultipleFieldErrors(
                            field = entity.field,
                            errors = entity.errors
                        )
                    }
                    is BusinessRuleError -> {
                        ValidationResult.BusinessRuleError(
                            rule = entity.rule,
                            message = entity.message,
                            context = entity.context
                        )
                    }
                    else -> {
                        ValidationResult.GenericError(apiError.getErrorMessage() ?: "خطای validation")
                    }
                }
            }
            else -> {
                ValidationResult.GenericError(apiError.getErrorMessage() ?: "خطای غیرمنتظره")
            }
        }
    }
    
    // Helper methods
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    private fun isValidPhone(phone: String): Boolean {
        return phone.matches(Regex("^09\\d{9}$"))
    }
    
    private fun createFormValidationError(message: String, field: String, value: String?, code: String): HttpException {
        val errorJson = """{"message": "$message", "field": "$field", "value": "$value", "code": "$code"}"""
        return HttpException(
            Response.error<Contact>(
                422,
                ResponseBody.create(
                    "application/json".toMediaType(),
                    errorJson
                )
            )
        )
    }
    
    private fun createFieldValidationError(field: String, errors: List<String>): HttpException {
        val errorsJson = errors.joinToString("\",\"", "\"", "\"")
        val errorJson = """{"message": "خطا در فیلد $field", "field": "$field", "errors": [$errorsJson]}"""
        return HttpException(
            Response.error<Contact>(
                422,
                ResponseBody.create(
                    "application/json".toMediaType(),
                    errorJson
                )
            )
        )
    }
    
    private fun createBusinessRuleError(message: String, rule: String, context: Map<String, String>): HttpException {
        val contextJson = context.entries.joinToString(",") { "\"${it.key}\": \"${it.value}\"" }
        val errorJson = """{"message": "$message", "rule": "$rule", "context": {$contextJson}}"""
        return HttpException(
            Response.error<Contact>(
                422,
                ResponseBody.create(
                    "application/json".toMediaType(),
                    errorJson
                )
            )
        )
    }
}

// Define validation results
sealed class ValidationResult {
    data class FieldError(
        val field: String,
        val message: String,
        val code: String,
        val value: String?
    ) : ValidationResult()
    
    data class MultipleFieldErrors(
        val field: String,
        val errors: List<String>
    ) : ValidationResult()
    
    data class BusinessRuleError(
        val rule: String,
        val message: String,
        val context: Map<String, String>
    ) : ValidationResult()
    
    data class GenericError(val message: String) : ValidationResult()
}

class ValidationException(val result: ValidationResult) : Exception(result.toString())

// Extension function for displaying validation results
fun ValidationResult.displayMessage(): String {
    return when (this) {
        is ValidationResult.FieldError -> "$field: $message"
        is ValidationResult.MultipleFieldErrors -> "$field: ${errors.joinToString(", ")}"
        is ValidationResult.BusinessRuleError -> message
        is ValidationResult.GenericError -> message
    }
}

fun ValidationResult.getFieldName(): String? {
    return when (this) {
        is ValidationResult.FieldError -> field
        is ValidationResult.MultipleFieldErrors -> field
        else -> null
    }
}
