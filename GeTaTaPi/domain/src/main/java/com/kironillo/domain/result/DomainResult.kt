package com.kironillo.domain.result

sealed class DomainResult<out T> {
    data class Success<T>(
        val data: T,
    ) : DomainResult<T>()

    data class Error(
        val error: DomainError,
    ) : DomainResult<Nothing>()
}

sealed class DomainError {
    data object Network : DomainError()
    data object Unauthorized : DomainError()
    data object NotFound : DomainError()
    data object Validation : DomainError()
    data object Unknown : DomainError()
    data object Timeout : DomainError()
}
