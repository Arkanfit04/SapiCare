package com.sapicare.app.data.model

data class Keluhan(
    val id: String = "",
    val sapiId: String = "",
    val namaSapi: String = "",
    val peternak: String = "",
    val peternakUid: String = "",
    val wilayah: String = "",
    val deskripsiKeluhan: String = "",
    val gejala: List<String> = emptyList(),
    val tanggalKeluhan: String = "",
    val usulanTanggalKunjungan: String = "",
    val status: StatusKeluhan = StatusKeluhan.MENUNGGU,
    val tanggapanPengurus: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class StatusKeluhan {
    MENUNGGU,
    DIJADWALKAN,
    SELESAI
}