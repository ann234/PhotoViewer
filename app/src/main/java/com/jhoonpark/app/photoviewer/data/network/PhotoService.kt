package com.jhoonpark.app.photoviewer.data.network

import com.jhoonpark.app.photoviewer.data.entity.PhotoDataEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class PhotoService @Inject constructor(
    private val client: OkHttpClient,
) {
    companion object {
        const val TAG = "PhotoService"
    }

    private suspend inline fun<reified R> execute(
        scope: CoroutineScope = CoroutineScope(Dispatchers.IO),
        url: String,
    ): R {
        return coroutineScope {
            val request = Request.Builder()
                .url(url)
                .build()
            val call = client.newCall(request)
            try {
                async<R>(scope.coroutineContext) {
                    val response: Response = call.execute()
                    val responseBody = response.body?.string()

                    // JSON 데이터를 List<PhotoData>로 변환
                    Json.decodeFromString(responseBody ?: "")
                }.await()
            } catch(e: Exception) {
                call.cancel()
                throw e
            }
        }
    }

    suspend fun fetchPhotoData(scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): List<PhotoDataEntity>
    = execute(scope = scope, url = "https://jsonplaceholder.typicode.com/photos")
}