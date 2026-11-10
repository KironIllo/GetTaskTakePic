package com.kironillo.data.remote.auth

fun interface TokenProvider {
    fun getAccessToken(): String?
}
