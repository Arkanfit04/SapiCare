package com.sapicare.app.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Main : Screen("main")
    object AccountSwitcher : Screen("account_switcher")

    object TambahSapi : Screen("tambah_sapi")
    object EditSapi : Screen("edit_sapi/{sapiId}") {
        fun createRoute(sapiId: String) = "edit_sapi/$sapiId"
    }
    object DetailSapi : Screen("detail_sapi/{sapiId}") {
        fun createRoute(sapiId: String) = "detail_sapi/$sapiId"
    }
    object RiwayatSapi : Screen("riwayat_sapi/{sapiId}/{namaSapi}") {
        fun createRoute(sapiId: String, namaSapi: String) =
            "riwayat_sapi/$sapiId/${namaSapi.replace("/", "-")}"
    }
    object DetailKeluhan : Screen("detail_keluhan/{keluhanId}") {
        fun createRoute(keluhanId: String) = "detail_keluhan/$keluhanId"
    }
    object DetailJadwal : Screen("detail_jadwal/{jadwalId}") {
        fun createRoute(jadwalId: String) = "detail_jadwal/$jadwalId"
    }
    object TindakLanjutJadwal : Screen("tindak_lanjut/{jadwalId}/{sapiId}") {
        fun createRoute(jadwalId: String, sapiId: String) = "tindak_lanjut/$jadwalId/$sapiId"
    }
}

// Bottom tabs per role
sealed class BottomTab(val route: String, val label: String) {
    // Pengurus
    object Sapi : BottomTab("tab_sapi", "Data Sapi")
    object KeluhanMasuk : BottomTab("tab_keluhan_masuk", "Keluhan")
    object JadwalPengurus : BottomTab("tab_jadwal_pengurus", "Jadwal")
    object RiwayatPengurus : BottomTab("tab_riwayat_pengurus", "Riwayat")

    // Peternak
    object SapiSaya : BottomTab("tab_sapi_saya", "Sapi Saya")
    object KirimKeluhan : BottomTab("tab_kirim_keluhan", "Keluhan")
    object RiwayatKeluhanPeternak : BottomTab("tab_riwayat_keluhan", "Riwayat")

    // Dinas
    object SapiDinas : BottomTab("tab_sapi_dinas", "Data Sapi")
    object KeluhanDinas : BottomTab("tab_keluhan_dinas", "Keluhan")
    object JadwalDinas : BottomTab("tab_jadwal_dinas", "Jadwal")
    object RiwayatDinas : BottomTab("tab_riwayat_dinas", "Riwayat")
}
