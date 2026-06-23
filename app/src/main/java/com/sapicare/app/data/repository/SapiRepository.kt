package com.sapicare.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sapicare.app.data.model.Sapi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SapiRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("sapi")

    fun getAllSapi(): Flow<List<Sapi>> = callbackFlow {
        val listener = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e(
                        "SapiRepository",
                        "Firestore error",
                        error
                    )

                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Sapi::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /** Hanya sapi milik peternak tertentu (filter by ownerId) */
    fun getSapiByOwner(ownerId: String): Flow<List<Sapi>> = callbackFlow {
        val listener = collection
            .whereEqualTo("ownerId", ownerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e(
                        "SapiRepository",
                        "Firestore error",
                        error
                    )

                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Sapi::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /** Sapi per wilayah (untuk peternak lihat sapi di wilayahnya) */
    fun getSapiByWilayah(wilayah: String): Flow<List<Sapi>> = callbackFlow {
        val listener = collection
            .whereEqualTo("wilayah", wilayah)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e(
                        "SapiRepository",
                        "Firestore error",
                        error
                    )

                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Sapi::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun searchSapi(query: String): Flow<List<Sapi>> = callbackFlow {
        val listener = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e(
                        "SapiRepository",
                        "Firestore error",
                        error
                    )
                    Log.e("SAPI_FLOW", "Permission denied", error)
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Sapi::class.java)?.copy(id = doc.id)
                }?.filter { sapi ->
                    sapi.nama.contains(query, ignoreCase = true) ||
                            sapi.wilayah.contains(query, ignoreCase = true)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun searchSapiByOwner(ownerId: String, query: String): Flow<List<Sapi>> = callbackFlow {
        val listener = collection
            .whereEqualTo("ownerId", ownerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e(
                        "SapiRepository",
                        "Firestore error",
                        error
                    )

                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Sapi::class.java)?.copy(id = doc.id)
                }?.filter { sapi ->
                    sapi.nama.contains(query, ignoreCase = true) ||
                            sapi.wilayah.contains(query, ignoreCase = true)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getSapiById(id: String): Sapi? {
        return try {
            val doc = collection.document(id).get().await()
            doc.toObject(Sapi::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun insertSapi(sapi: Sapi): String {
        val doc = collection.add(sapi).await()
        return doc.id
    }

    suspend fun updateSapi(sapi: Sapi) {
        collection.document(sapi.id).set(sapi).await()
    }

    suspend fun deleteSapi(id: String) {
        collection.document(id).delete().await()
    }
}
