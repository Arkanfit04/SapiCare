package com.sapicare.app

import android.app.Application
import com.sapicare.app.data.remote.CloudinaryHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class SapiCareApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        CloudinaryHelper.init(this)
    }
}
