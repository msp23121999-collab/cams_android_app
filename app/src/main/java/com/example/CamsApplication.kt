package com.example

import android.app.Application
import com.example.core.di.AppContainer
import com.example.core.di.DefaultAppContainer

class CamsApplication : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        instance = this
        container = DefaultAppContainer(this)
    }

    companion object {
        lateinit var instance: CamsApplication
            private set
    }
}
