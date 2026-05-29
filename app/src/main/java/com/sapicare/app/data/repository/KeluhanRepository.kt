package com.sapicare.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sapicare.app.data.model.Keluhan
import com.sapicare.app.data.model.StatusKeluhan
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KeluhanRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("keluhan")

    /** Semua keluhan — untuk Pengurus & Dinas */
    fun getAllKeluhan(): Flow<List<Keluhan>> = callbackFlow {
        val listener = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Keluhan::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    /** Keluhan milik peternak tertentu */
    fun getKeluhanByPeternak(peternakUid: String): Flow<List<Keluhan>> = callbackFlow {
        val listener = collection
            .whereEqualTo("peternakUid", peternakUid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Keluhan::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addKeluhan(keluhan: Keluhan): String {
        val doc = collection.add(keluhan).await()
        return doc.id
    }

    suspend fun updateStatusKeluhan(id: String, status: StatusKeluhan, tanggapan: String = "") {
        val updates = mutableMapOf<String, Any>("status" to status.name)
        if (tanggapan.isNotEmpty()) updates["tanggapanPengurus"] = tanggapan
        collection.document(id).update(updates).await()
    }

    suspend fun deleteKeluhan(id: String) {
        collection.document(id).delete().await()
    }
}
