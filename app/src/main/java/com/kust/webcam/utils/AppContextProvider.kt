package com.kust.webcam.utils

import android.app.Application
import android.content.Context

/**
 * 提供全局应用上下文的工具类
 */
class AppContextProvider : Application() {
    companion object {
        private lateinit var appContext: Context

        fun init(context: Context) {
            appContext = context.applicationContext
        }

        fun getContext(): Context {
            return appContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        init(this)
    }
} 