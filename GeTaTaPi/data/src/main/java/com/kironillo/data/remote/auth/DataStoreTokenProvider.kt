package com.kironillo.data.remote.auth

import com.kironillo.data.local.datastore.AuthSessionDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DataStoreTokenProvider @Inject constructor(
    private val sessionDataStore: AuthSessionDataStore,
) : TokenProvider {

    override fun getAccessToken(): String? =
        runBlocking {
            withContext(Dispatchers.IO) {
                sessionDataStore.getAccessToken()
            }
        }
}
