package com.kironillo.data.remote.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context.CONNECTIVITY_SERVICE
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidNetworkConnectivity @Inject constructor(
    @ApplicationContext private val context: Context,
) : NetworkConnectivity {

    override fun isConnected(): Boolean {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET,
        ) && capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_VALIDATED,
        )
    }
}
