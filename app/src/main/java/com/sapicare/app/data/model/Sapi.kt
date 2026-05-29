package com.sapicare.app.data.model

data class Sapi(
    val id: String = "",
    val nama: String = "",
    val jenisSapi: String = "",
    val jenisKelamin: String = "",
    val tanggalLahir: String = "",
    val namaPemilik: String = "",
    val wilayah: String = "",
    val keterangan: String = "",
    val jenisPerawatan: List<String> = emptyList(),
    val fotoUrl: String = "",
    val status: String = "Sehat",        // "Sehat" / "Dalam Perawatan"
    val beratBadan: Double = 0.0,
    val ownerId: String = "",
    val createdBy: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
