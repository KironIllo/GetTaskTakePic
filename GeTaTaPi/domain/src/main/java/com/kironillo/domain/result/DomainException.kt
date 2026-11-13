package com.kironillo.domain.result

class DomainException(
    val domainError: DomainError,
) : RuntimeException()
