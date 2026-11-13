package com.kironillo.data.remote

import com.kironillo.data.remote.connectivity.NetworkConnectivity
import com.kironillo.domain.result.DomainError
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class NetworkErrorHandler(
    private val connectivity: NetworkConnectivity,
) {

    fun mapError(exception: Throwable): DomainError {
        return when (exception) {
            is SocketTimeoutException -> DomainError.Timeout

            is HttpException -> when (exception.code()) {
                401, 403 -> DomainError.Unauthorized
                404 -> DomainError.NotFound
                400, 422 -> DomainError.Validation
                else -> DomainError.Unknown
            }

            is IOException -> {
                if (connectivity.isConnected()) {
                    DomainError.Unknown
                } else {
                    DomainError.Network
                }
            }

            else -> DomainError.Unknown
        }
    }
}
