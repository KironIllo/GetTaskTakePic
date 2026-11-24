package com.kironillo.domain.validation

/**
 * Pure domain validation for the sign-in and registration form.
 * No Android or UI dependencies: safe to cover with local unit tests.
 */
object AuthFormValidator {
    data class Errors(
        val email: String? = null,
        val password: String? = null,
        val confirmPassword: String? = null,
    ) {
        val isValid: Boolean
            get() = email == null && password == null && confirmPassword == null

        val firstError: String?
            get() = email ?: password ?: confirmPassword
    }

    fun validate(
        email: String,
        password: String,
        confirmPassword: String = "",
        isRegistration: Boolean,
    ): Errors {
        val normalizedEmail = email.trim()
        val emailError = when {
            normalizedEmail.isBlank() -> "Введите email"
            !EMAIL_REGEX.matches(normalizedEmail) -> "Введите корректный email"
            else -> null
        }
        val passwordError = when {
            password.isBlank() -> "Введите пароль"
            password.length < MIN_PASSWORD_LENGTH ->
                "Пароль должен содержать минимум $MIN_PASSWORD_LENGTH символов"
            else -> null
        }
        val confirmationError = if (!isRegistration) {
            null
        } else when {
            confirmPassword.isBlank() -> "Повторите пароль"
            password != confirmPassword -> "Пароли не совпадают"
            else -> null
        }

        return Errors(
            email = emailError,
            password = passwordError,
            confirmPassword = confirmationError,
        )
    }

    private const val MIN_PASSWORD_LENGTH = 6
    private val EMAIL_REGEX = Regex("^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9-]+(?:\\.[A-Za-z0-9-]+)+$")
}
