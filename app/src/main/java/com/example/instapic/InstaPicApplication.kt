package com.example.instapic

import android.app.Application
import com.example.instapic.utils.CloudinaryHelper

class InstaPicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            CloudinaryHelper.init(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}