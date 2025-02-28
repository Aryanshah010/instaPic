package com.example.instapic.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class ImageUtils(private val activity: FragmentActivity) {
    private var imageSelectedCallback: ((Uri) -> Unit)? = null
    private lateinit var imagePickerLauncher: ActivityResultLauncher<Intent>
    private var permissionLauncher: ActivityResultLauncher<String>? = null

    fun registerActivity(callback: (Uri) -> Unit) {
        imageSelectedCallback = callback
        imagePickerLauncher = activity.registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    imageSelectedCallback?.invoke(uri)
                }
            }
        }

        permissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                openGallery()
            } else {
                Log.e("ImageUtils", "Permission denied for gallery access")
            }
        }
    }

    fun launchGallery(context: Context) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher?.launch(permission)
        } else {
            openGallery()
        }
    }

    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            imagePickerLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e("ImageUtils", "Error opening gallery: ${e.message}")
        }
    }
}