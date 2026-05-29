package com.sapicare.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sapicare.app.data.model.JadwalKunjungan
import com.sapicare.app.data.model.StatusJadwal
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JadwalRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("jadwal_kunjungan")

    fun getAllJadwal(): Flow<List<JadwalKunjungan>> = callbackFlow {
        val listener = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(JadwalKunjungan::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    fun getJadwalByPengurus(pengurusUid: String): Flow<List<JadwalKunjungan>> = callbackFlow {
        val listener = collection
            .whereEqualTo("pengurusUid", pengurusUid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(JadwalKunjungan::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addJadwal(jadwal: JadwalKunjungan): String {
        val doc = collection.add(jadwal).await()
        return doc.id
    }

    suspend fun updateStatusJadwal(id: String, status: StatusJadwal) {
        collection.document(id).update("status", status.name).await()
    }

    suspend fun deleteJadwal(id: String) {
        collection.document(id).delete().await()
    }
}
