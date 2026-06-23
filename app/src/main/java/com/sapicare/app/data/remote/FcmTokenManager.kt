package com.sapicare.app.data.remote

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmTokenManager @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun saveToken(uid: String) {
        try {
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d("FCM_TOKEN", "Token: $token")
            firestore.collection("users")
                .document(uid)
                .update("fcmToken", token)
                .await()
            Log.d("FCM_TOKEN", "Token tersimpan untuk uid: $uid")
        } catch (e: Exception) {
            Log.e("FCM_TOKEN", "Gagal simpan token: ${e.message}")
        }
    }

    suspend fun clearToken(uid: String) {
        try {

            firestore.collection("users")
                .document(uid)
                .update("fcmToken", "")
                .await()

            Log.d("FCM_TOKEN", "Token dilepas dari user $uid")

        } catch (e: Exception) {
            Log.e("FCM_TOKEN", "Gagal clear token: ${e.message}")
        }
    }
}