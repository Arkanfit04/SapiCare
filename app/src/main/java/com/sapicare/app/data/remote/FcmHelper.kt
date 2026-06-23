package com.sapicare.app.data.remote

import android.content.Context
import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val client = OkHttpClient()
    private val serviceAccountFile = "sapicare-70ec1-firebase-adminsdk-fbsvc-b5323a933a.json"
    private val projectId = "sapicare-70ec1" // sesuaikan dengan project ID kamu

    private fun getAccessToken(): String {
        val stream = context.assets.open(serviceAccountFile)
        val credentials = GoogleCredentials
            .fromStream(stream)
            .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }

    suspend fun sendNotification(
        fcmToken: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ) = withContext(Dispatchers.IO) {
        try {
            val accessToken = getAccessToken()

            val dataJson = JSONObject()
            data.forEach { (k, v) -> dataJson.put(k, v) }

            val payload = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", fcmToken)
                    put("notification", JSONObject().apply {
                        put("title", title)
                        put("body", body)
                    })
                    put("android", JSONObject().apply {
                        put("priority", "high")
                        put("notification", JSONObject().apply {
                            put("sound", "default")
                            put("channel_id", "sapicare_channel")
                        })
                    })
                    if (data.isNotEmpty()) put("data", dataJson)
                })
            }

            val request = Request.Builder()
                .url("https://fcm.googleapis.com/v1/projects/$projectId/messages:send")
                .post(payload.toString().toRequestBody("application/json".toMediaType()))
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .build()

            val response = client.newCall(request).execute()
            Log.d("FCM", "Response: ${response.code} - ${response.body?.string()}")
        } catch (e: Exception) {
            Log.e("FCM", "Gagal kirim FCM: ${e.message}")
        }
    }
}