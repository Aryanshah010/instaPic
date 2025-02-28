package com.example.instapic.utils

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import android.util.Log
import com.example.instapic.BuildConfig

class CloudinaryHelper {
    companion object {
        fun init(context: Context) {
            try {
                val config = hashMapOf<String, String>(
                    "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                    "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                    "api_secret" to BuildConfig.CLOUDINARY_API_SECRET
                )
                MediaManager.init(context, config)
            } catch (e: Exception) {
                Log.e("CloudinaryHelper", "Failed to initialize Cloudinary: ${e.message}")
            }
        }

        fun uploadImage(imageUri: Uri, callback: (String?, String?) -> Unit) {
            try {
                val mediaManager = MediaManager.get()
                mediaManager.upload(imageUri)
                    .unsigned("ml_default")
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {}

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            val imageUrl = resultData["url"] as String
                            callback(imageUrl, null)
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            callback(null, error.description)
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {}
                    })
                    .dispatch()
            } catch (e: Exception) {
                callback(null, "Cloudinary not initialized or upload failed: ${e.message}")
            }
        }
    }
}