package com.kironillo.data.remote

import com.kironillo.data.remote.connectivity.NetworkConnectivity
import com.kironillo.domain.result.DomainError
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class NetworkErrorHandlerTest {

    @Test
    fun `maps timeout error`() {
        assertEquals(
            DomainError.Timeout,
            handler().mapError(SocketTimeoutException()),
        )
    }

    @Test
    fun `maps unauthorized error`() {
        assertEquals(
            DomainError.Unauthorized,
            handler().mapError(httpException(401)),
        )
    }

    @Test
    fun `maps not found error`() {
        assertEquals(
            DomainError.NotFound,
            handler().mapError(httpException(404)),
        )
    }

    @Test
    fun `maps validation error`() {
        assertEquals(
            DomainError.Validation,
            handler().mapError(httpException(422)),
        )
    }

    @Test
    fun `maps no connection error`() {
        assertEquals(
            DomainError.Network,
            handler(isConnected = false).mapError(IOException()),
        )
    }

    @Test
    fun `maps unknown error`() {
        assertEquals(
            DomainError.Unknown,
            handler().mapError(IllegalStateException()),
        )
    }

    private fun handler(
        isConnected: Boolean = true,
    ) = NetworkErrorHandler(
        object : NetworkConnectivity {
            override fun isConnected() = isConnected
        },
    )

    private fun httpException(code: Int): HttpException {
        val body = "Error".toResponseBody("text/plain".toMediaType())
        return HttpException(Response.error<Any>(code, body))
    }
}
