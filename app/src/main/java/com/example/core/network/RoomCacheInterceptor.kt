package com.example.core.network

import android.content.Context
import androidx.room.Room
import com.example.core.database.CamsDatabase
import com.example.core.database.entities.ApiCacheEntity
import com.example.core.database.dao.ApiCacheDao
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

class RoomCacheInterceptor(private val dao: ApiCacheDao) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val url = request.url.toString()
        
        return try {
            val response = chain.proceed(request)
            if (response.isSuccessful && request.method == "GET") {
                val bodyString = response.peekBody(Long.MAX_VALUE).string()
                dao.insert(ApiCacheEntity(url, bodyString, System.currentTimeMillis()))
            }
            response
        } catch (e: Exception) {
            if (request.method == "GET") {
                val cached = dao.get(url)
                if (cached != null) {
                    GlobalNetworkHandler.emitError("You are offline. Showing cached data.")
                    return Response.Builder()
                        .request(request)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(cached.responseBody.toResponseBody("application/json".toMediaTypeOrNull()))
                        .build()
                }
            }
            throw e
        }
    }
}
