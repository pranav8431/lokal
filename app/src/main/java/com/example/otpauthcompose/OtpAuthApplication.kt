package com.example.otpauthcompose

import android.app.Application
import timber.log.Timber

/**
 * Custom Application class for app-wide initialization.
 * 
 * Responsibilities:
 * - Initialize Timber logging in debug builds
 * - Any other app-level initialization
 * 
 * This class is registered in AndroidManifest.xml.
 */
class OtpAuthApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Timber for logging
        // Only plant DebugTree in debug builds
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
            Timber.d("Timber initialized for OtpAuthCompose app")
        }
    }
}
