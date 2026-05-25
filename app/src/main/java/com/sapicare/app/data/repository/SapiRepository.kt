package com.sapicare.app.data.repository

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
                    close(error)
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
                    close(error)
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
