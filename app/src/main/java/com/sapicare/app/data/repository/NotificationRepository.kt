package com.sapicare.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.sapicare.app.data.model.NotificationItem
import com.sapicare.app.data.remote.FcmHelper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val fcmHelper: FcmHelper
) {

    private val collection = firestore.collection("notifications")

    suspend fun addNotification(notification: NotificationItem) {
        collection.add(
            mapOf(
                "targetUid" to notification.targetUid,
                "title" to notification.title,
                "message" to notification.message,
                "type" to notification.type,
                "read" to notification.read,
                "createdAt" to notification.createdAt
            )
        ).await()
    }

    // ← TAMBAH DI SINI, tepat di bawah addNotification
    suspend fun addNotificationWithPush(notification: NotificationItem) {
        // Simpan in-app notif dulu
        addNotification(notification)

        // Ambil FCM token target user lalu kirim push
        try {
            val doc = firestore.collection("users")
                .document(notification.targetUid)
                .get()
                .await()
            val fcmToken = doc.getString("fcmToken") ?: return
            if (fcmToken.isNotEmpty()) {
                fcmHelper.sendNotification(
                    fcmToken = fcmToken,
                    title = notification.title,
                    body = notification.message,
                    data = mapOf("type" to notification.type)
                )
            }
        } catch (e: Exception) {
            Log.e("NOTIF", "Gagal kirim push: ${e.message}")
        }
    }

    fun getNotifications(uid: String): Flow<List<NotificationItem>> =
        callbackFlow {

            val listener = collection
                .whereEqualTo("targetUid", uid)
                .addSnapshotListener { snapshot, error ->

                    Log.d(
                        "NOTIF_SNAPSHOT",
                        "uid=$uid, fromCache=${snapshot?.metadata?.isFromCache}"
                    )

                    if (error != null) {

                        Log.e(
                            "NOTIF_SNAPSHOT",
                            "Firestore error",
                            error
                        )

                        trySend(emptyList())

                        return@addSnapshotListener
                    }

                    if (snapshot == null) {
                        return@addSnapshotListener
                    }

                    // Hindari notif hantu dari cache Firestore
//                    if (snapshot.metadata.isFromCache) {
//                        return@addSnapshotListener
//                    }

                    val list = snapshot.documents
                        .mapNotNull { doc ->

                            Log.d(
                                "RAW_DOC",
                                doc.data.toString()
                            )

                            doc.toObject(NotificationItem::class.java)
                                ?.copy(id = doc.id)
                        }
                        .sortedByDescending { it.createdAt }

                    Log.d(
                        "NOTIF_COUNT",
                        "uid=$uid, total=${list.size}"
                    )

                    trySend(list)
                }

            awaitClose {
                listener.remove()
            }
        }

    suspend fun markAsRead(id: String) {
        try {
            collection.document(id)
                .update("read", true)
                .await()

            Log.d(
                "NOTIF",
                "Berhasil update $id"
            )

        } catch (e: Exception) {
            Log.e(
                "NOTIF",
                "Gagal update $id",
                e
            )
        }
    }
}