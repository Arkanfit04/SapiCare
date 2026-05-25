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
}

sealed class BottomTab(val route: String, val label: String) {
    object Sapi : BottomTab("tab_sapi", "Data Sapi")
    object Riwayat : BottomTab("tab_riwayat", "Riwayat")
}
