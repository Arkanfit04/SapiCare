package com.sapicare.app.data.model

data class RiwayatKunjungan(
    val id: String = "",
    val sapiId: String = "",
    val jadwalId: String = "",
    val keluhanId: String = "",
    val tanggal: String = "",
    val kondisiSapi: String = "",
    val diagnosis: String = "",
    val tindakan: String = "",
    val obat: String = "",
    val catatan: String = "",
    val namaPetugas: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
