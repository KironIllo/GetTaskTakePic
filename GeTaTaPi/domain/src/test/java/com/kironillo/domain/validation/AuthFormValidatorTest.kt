package com.kironillo.domain.validation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthFormValidatorTest {
    @Test
    fun `valid login form has no errors`() {
        val result = AuthFormValidator.validate(
            email = "person@example.com",
            password = "secret1",
            isRegistration = false,
        )
        assertTrue(result.isValid)
        assertNull(result.firstError)
    }

    @Test
    fun `email is required`() {
        val result = AuthFormValidator.validate("", "secret1", isRegistration = false)
        assertEquals("Введите email", result.email)
    }

    @Test
    fun `email must have a valid format`() {
        val result = AuthFormValidator.validate("person@", "secret1", isRegistration = false)
        assertEquals("Введите корректный email", result.email)
    }

    @Test
    fun `password must contain at least six characters`() {
        val result = AuthFormValidator.validate(
            "person@example.com", "12345", isRegistration = false,
        )
        assertEquals("Пароль должен содержать минимум 6 символов", result.password)
    }

    @Test
    fun `registration requires matching password confirmation`() {
        val result = AuthFormValidator.validate(
            "person@example.com", "secret1", "secret2", isRegistration = true,
        )
        assertFalse(result.isValid)
        assertEquals("Пароли не совпадают", result.confirmPassword)
    }

    @Test
    fun `registration accepts matching password confirmation`() {
        val result = AuthFormValidator.validate(
            "person@example.com", "secret1", "secret1", isRegistration = true,
        )
        assertTrue(result.isValid)
    }

    @Test
    fun `login does not validate registration confirmation`() {
        val result = AuthFormValidator.validate(
            "person@example.com", "secret1", "different", isRegistration = false,
        )
        assertTrue(result.isValid)
    }
}
