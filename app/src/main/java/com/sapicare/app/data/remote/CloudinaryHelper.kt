package com.sapicare.app.data.remote

import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class CloudinaryHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        // ⚠️ Ganti dengan credential Cloudinary kamu!
        const val CLOUD_NAME = "dapvpfg0y"
        const val API_KEY = "468213152999328"
        const val API_SECRET = "3kQ-hfv83hZP1kIX8kGUDRAASWg"

        const val UPLOAD_PRESET = "sapicare_unsigned" // buat unsigned preset di Cloudinary dashboard

        private var isInitialized = false

        fun init(context: Context) {
            if (!isInitialized) {
                val config = mapOf(
                    "cloud_name" to CLOUD_NAME,
                    "api_key" to API_KEY,
                    "api_secret" to API_SECRET
                )
                MediaManager.init(context, config)
                isInitialized = true
            }
        }
    }

    suspend fun uploadImage(uri: Uri): String = suspendCancellableCoroutine { continuation ->
        MediaManager.get()
            .upload(uri)
            .option("folder", "sapicare/sapi")
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}

                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) {
                        continuation.resume(url)
                    } else {
                        continuation.resumeWithException(Exception("URL tidak ditemukan"))
                    }
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }
            })
            .dispatch(context)
    }
}
