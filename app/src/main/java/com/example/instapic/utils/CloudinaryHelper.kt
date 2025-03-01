package com.example.instapic.utils

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.instapic.BuildConfig

class CloudinaryHelper {
    companion object {
        private var isInitialized = false
        private const val UPLOAD_PRESET = "ml_default" // Use "ml_default" or your preset name
        private const val tag = "CloudinaryHelper"

        fun init(context: Context) {
            if (!isInitialized) {
                try {
                    val config = hashMapOf(
                        "cloud_name" to BuildConfig.CLOUDINARY_CLOUD_NAME,
                        "api_key" to BuildConfig.CLOUDINARY_API_KEY,
                        "api_secret" to BuildConfig.CLOUDINARY_API_SECRET,
                        "secure" to true
                    )
                    MediaManager.init(context, config)
                    isInitialized = true
                    Log.d(tag, "Cloudinary initialized successfully")
                } catch (e: Exception) {
                    Log.e(tag, "Failed to initialize Cloudinary: ${e.message}")
                    e.printStackTrace()
                }
            }
        }

        fun uploadImage(uri: Uri, callback: (String?, String?) -> Unit) {
            if (!isInitialized) {
                Log.e(tag, "Cloudinary not initialized")
                callback(null, "Cloudinary not initialized")
                return
            }

            try {
                Log.d(tag, "Starting upload with preset: $UPLOAD_PRESET")
                MediaManager.get()
                    .upload(uri)
                    .unsigned(UPLOAD_PRESET)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {
                            Log.d(tag, "Upload started with requestId: $requestId")
                        }

                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                            val progress = (bytes * 100 / totalBytes).toInt()
                            Log.d(tag, "Upload progress: $progress%")
                        }

                        override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                            Log.d(tag, "Upload successful. Result: $resultData")
                            val imageUrl = resultData["secure_url"] as? String
                                ?: resultData["url"] as? String
                            if (imageUrl != null) {
                                callback(imageUrl, null)
                            } else {
                                callback(null, "No URL in response")
                            }
                        }

                        override fun onError(requestId: String, error: ErrorInfo) {
                            Log.e(tag, "Upload error: ${error.description}")
                            callback(null, error.description)
                        }

                        override fun onReschedule(requestId: String, error: ErrorInfo) {
                            Log.d(tag, "Upload rescheduled: ${error.description}")
                        }
                    })
                    .dispatch()
            } catch (e: Exception) {
                Log.e(tag, "Upload failed: ${e.message}")
                e.printStackTrace()
                callback(null, "Upload failed: ${e.message}")
            }
        }
    }
}