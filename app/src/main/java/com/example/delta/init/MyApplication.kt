package com.example.delta.init


import android.app.Application
import android.content.Context
import android.content.res.Configuration

class MyApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(wrapFontScale(base))
    }
    companion object {
        private var appContext: Context? = null
    }


    override fun onCreate() {
        super.onCreate()
        appContext = this

    }


}

fun wrapFontScale(context: Context): Context {
    val config = context.resources.configuration
    val newConfig = Configuration(config)

    val clamped = newConfig.fontScale.coerceIn(0.9f, 1.15f)
    newConfig.fontScale = clamped

    return context.createConfigurationContext(newConfig)
}
