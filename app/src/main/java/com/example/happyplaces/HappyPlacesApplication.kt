package com.example.happyplaces // Your actual package name

import android.app.Application
import android.util.Log // For logging
import org.osmdroid.config.Configuration
// If you are using your app's BuildConfig for the package name:
// import com.example.happyplaces.BuildConfig // Use YOUR app's BuildConfig

class HappyPlacesApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Configuration.getInstance().userAgentValue = packageName
        Log.d("HappyPlacesApplication", "OSMDroid User Agent set to: ${Configuration.getInstance().userAgentValue}")

    }
}
