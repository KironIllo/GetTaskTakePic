package com.kironillo.data.remote.logging

import okhttp3.logging.HttpLoggingInterceptor

fun createNetworkLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
}
