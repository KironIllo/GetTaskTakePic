
package com.kironillo.data.remote

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface StorageApi {

    @Headers("x-upsert: false")
    @POST("object/submissions/{path}")
    suspend fun uploadPhoto(
        @Path(value = "path", encoded = true) path: String,
        @Header("Content-Type") contentType: String,
        @Body body: RequestBody,
    ): Response<ResponseBody>
}
