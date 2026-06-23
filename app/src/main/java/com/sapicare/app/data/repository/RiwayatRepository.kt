package com.sapicare.app.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.sapicare.app.data.model.RiwayatKunjungan
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RiwayatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val collection = firestore.collection("riwayat_kunjungan")

    // Semua riwayat (untuk dashboard)
    fun getAllRiwayat(): Flow<List<RiwayatKunjungan>> = callbackFlow {
        val listener = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(
                        "Firestore",
                        "Snapshot error",
                        error
                    )

                    trySend(emptyList())

                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RiwayatKunjungan::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    // Riwayat per sapi (untuk detail sapi)
    fun getRiwayatBySapiId(sapiId: String): Flow<List<RiwayatKunjungan>> = callbackFlow {
        val listener = collection
            .whereEqualTo("sapiId", sapiId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(
                        "Firestore",
                        "Snapshot error",
                        error
                    )
                    Log.e("RIWAYAT_FLOW", "Permission denied", error)
                    trySend(emptyList())

                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(RiwayatKunjungan::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addRiwayat(riwayat: RiwayatKunjungan): String {
        val doc = collection.add(riwayat).await()
        return doc.id
    }

    suspend fun updateRiwayat(riwayat: RiwayatKunjungan) {
        collection.document(riwayat.id).set(riwayat).await()
    }

    suspend fun deleteRiwayat(id: String) {
        collection.document(id).delete().await()
    }
}
