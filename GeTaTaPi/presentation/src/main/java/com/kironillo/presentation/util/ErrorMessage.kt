package com.kironillo.presentation.util

import com.kironillo.domain.result.DomainError
import com.kironillo.domain.result.DomainException

private const val DEFAULT_ERROR_MESSAGE = "Не удалось выполнить запрос. Попробуйте ещё раз."

fun DomainError.toUserMessage(): String =
    when (this) {
        DomainError.Network -> "Нет подключения к интернету. Проверьте сеть и попробуйте снова."
        DomainError.Server -> "Сервер временно недоступен (ошибка 5xx). Попробуйте ещё раз чуть позже."
        DomainError.Timeout -> "Сервер не отвечает. Проверьте соединение и повторите попытку."
        DomainError.Unauthorized -> "Сессия истекла. Войдите в аккаунт заново."
        DomainError.NotFound -> "Данные не найдены. Обновите экран и попробуйте снова."
        DomainError.Validation -> "Сервер отклонил запрос. Проверьте введённые данные."
        DomainError.Unknown -> DEFAULT_ERROR_MESSAGE
    }

fun Throwable.toUserMessage(fallback: String = DEFAULT_ERROR_MESSAGE): String =
    when (this) {
        is DomainException -> domainError.toUserMessage()
        else -> message?.takeIf { it.isNotBlank() } ?: fallback
    }
