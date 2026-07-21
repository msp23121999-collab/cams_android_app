package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.example.core.di.AppContainer
import com.example.core.di.DefaultAppContainer
import com.example.core.network.AuthInterceptor
import okhttp3.OkHttpClient

class CamsApplication : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = DefaultAppContainer(this)
    }

    /**
     * Coil does not go through Retrofit, so it would otherwise request images with no
     * Authorization header. Uploaded files (profile photos, documents) are served from
     * the auth-gated /api/v1/files/ endpoint and would come back 401, rendering as
     * blank images. Supplying the loader here attaches the same bearer token — and the
     * same 401-refresh handling — that every API call uses.
     */
    override fun newImageLoader(): ImageLoader {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(container.authManager))
            .build()
        return ImageLoader.Builder(this)
            .okHttpClient(client)
            .crossfade(true)
            .build()
    }

    companion object {
        lateinit var instance: CamsApplication
            private set
    }
}
