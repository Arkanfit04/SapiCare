package com.sapicare.app.data.model

data class JadwalKunjungan(
    val id: String = "",
    val keluhanId: String = "",
    val sapiId: String = "",
    val namaSapi: String = "",
    val peternak: String = "",
    val peternakUid: String = "",
    val wilayah: String = "",
    val tanggalKunjungan: String = "",
    val waktuKunjungan: String = "",
    val catatan: String = "",
    val status: StatusJadwal = StatusJadwal.TERJADWAL,
    val pengurus: String = "",
    val pengurusUid: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

enum class StatusJadwal {
    TERJADWAL,
    SELESAI,
    DIBATALKAN
}