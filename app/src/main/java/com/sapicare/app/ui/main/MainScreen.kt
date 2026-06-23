package com.sapicare.app.ui.main

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.sapicare.app.data.model.ApprovalStatus
import com.sapicare.app.data.model.UserRole
import com.sapicare.app.navigation.BottomTab
import com.sapicare.app.navigation.Screen
import com.sapicare.app.ui.auth.AuthViewModel
import com.sapicare.app.ui.auth.PendingApprovalScreen
import com.sapicare.app.ui.dinas.DetailSapiDinasScreen
import com.sapicare.app.ui.dinas.JadwalDinasScreen
import com.sapicare.app.ui.dinas.KelolaAkunScreen
import com.sapicare.app.ui.dinas.KeluhanDinasScreen
import com.sapicare.app.ui.notification.NotificationScreen
import com.sapicare.app.ui.notification.NotificationViewModel
import com.sapicare.app.ui.pengurus.detail.DetailSapiPengurusScreen
import com.sapicare.app.ui.pengurus.form.SapiFormScreen
import com.sapicare.app.ui.pengurus.jadwal.JadwalKunjunganScreen
import com.sapicare.app.ui.pengurus.jadwal.TindakLanjutScreen
import com.sapicare.app.ui.pengurus.keluhan.DetailKeluhanScreen
import com.sapicare.app.ui.pengurus.keluhan.KeluhanMasukScreen
import com.sapicare.app.ui.pengurus.list.SapiDashboardScreen
import com.sapicare.app.ui.peternak.KirimKeluhanScreen
import com.sapicare.app.ui.peternak.RiwayatKeluhanPeternakScreen
import com.sapicare.app.ui.peternak.SapiSayaScreen
import com.sapicare.app.ui.riwayat.RiwayatDashboardScreen
import com.sapicare.app.ui.riwayat.RiwayatKunjunganScreen

data class BottomNavItem(
    val tab: BottomTab,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit,
    onSwitchAccount: () -> Unit
) {
    val session by authViewModel.sessionFlow.collectAsState(initial = null)
    val savedAccounts by authViewModel.savedAccountsFlow.collectAsState(initial = emptyList())

    val notificationViewModel: NotificationViewModel = hiltViewModel()
    val unreadCount by notificationViewModel.unreadCount.collectAsState()

    val isPengurus = session?.role == UserRole.PENGURUS
    val isPeternak = session?.role == UserRole.PETERNAK
    val isDinas = session?.role == UserRole.DINAS

    if (session == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // ── APPROVAL GATE untuk Pengurus ─────────────────────────────────────────
    if (isPengurus && session?.approvalStatus != ApprovalStatus.APPROVED) {
        PendingApprovalScreen(
            username = session?.username ?: "",
            approvalStatus = session?.approvalStatus ?: ApprovalStatus.PENDING,

            onRefresh = {
                authViewModel.refreshApprovalStatus { newStatus ->
                    // Kalau sudah approved, session flow akan update otomatis
                }
            },

            onReapply = {
                session?.uid?.let { uid ->
                    authViewModel.reapplyApproval(uid) {
                        authViewModel.refreshApprovalStatus { }
                    }
                }
            },

            onLogout = onLogout
        )

        return
    }

    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems: List<BottomNavItem> = when {
        isPengurus -> listOf(
            BottomNavItem(BottomTab.Sapi, Icons.Filled.Pets, Icons.Outlined.Pets),
            BottomNavItem(BottomTab.KeluhanMasuk, Icons.Filled.Notifications, Icons.Outlined.Notifications),
            BottomNavItem(BottomTab.JadwalPengurus, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
            BottomNavItem(BottomTab.RiwayatPengurus, Icons.Filled.History, Icons.Outlined.History)
        )
        isPeternak -> listOf(
            BottomNavItem(BottomTab.SapiSaya, Icons.Filled.Pets, Icons.Outlined.Pets),
            BottomNavItem(BottomTab.KirimKeluhan, Icons.Filled.ReportProblem, Icons.Outlined.ReportProblem),
            BottomNavItem(BottomTab.RiwayatKeluhanPeternak, Icons.Filled.History, Icons.Outlined.History)
        )
        isDinas -> listOf(
            BottomNavItem(BottomTab.SapiDinas, Icons.Filled.Pets, Icons.Outlined.Pets),
            BottomNavItem(BottomTab.KeluhanDinas, Icons.Filled.Notifications, Icons.Outlined.Notifications),
            BottomNavItem(BottomTab.JadwalDinas, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
            BottomNavItem(BottomTab.RiwayatDinas, Icons.Filled.History, Icons.Outlined.History),
            BottomNavItem(BottomTab.KelolaAkun, Icons.Filled.ManageAccounts, Icons.Outlined.ManageAccounts)
        )
        else -> emptyList()
    }

    val tabRoutes = bottomNavItems.map { it.tab.route }
    val showBottomBar = currentRoute in tabRoutes
    val startDestination = when {
        isPengurus -> BottomTab.Sapi.route
        isPeternak -> BottomTab.SapiSaya.route
        isDinas -> BottomTab.SapiDinas.route
        else -> BottomTab.Sapi.route
    }

    LaunchedEffect(session?.role) {
        Log.d(
            "ROLE_DEBUG",
            "role=${session?.role}"
        )
    }

    Scaffold(
        bottomBar = {
            AnimatedVisibility(visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.navigationBarsPadding()
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.tab.route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                innerNavController.navigate(item.tab.route) {
                                    popUpTo(innerNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected)
                                        item.selectedIcon
                                    else
                                        item.unselectedIcon,
                                    contentDescription = item.tab.label
                                )
                            },
                            label = {
                                Text(
                                    item.tab.label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected)
                                        FontWeight.SemiBold
                                    else
                                        FontWeight.Normal
                                )
                            },

                            alwaysShowLabel = false, // <- TAMBAH INI

                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF2E7D32),
                                selectedTextColor = Color(0xFF2E7D32),
                                indicatorColor = Color(0xFFE8F5E9),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        NavHost(navController = innerNavController, startDestination = startDestination,
            modifier = Modifier.padding(paddingValues)) {

            composable(Screen.Notifications.route) {
                NotificationScreen(
                    onBack = {
                        innerNavController.popBackStack()
                    }
                )
            }

            // ── PENGURUS ───────────────────────────────────────────────────────
            composable(BottomTab.Sapi.route) {
                SapiDashboardScreen(
                    canAddSapi = false,
                    username = session?.username ?: "",
                    savedAccountsCount = savedAccounts.size,
                    unreadCount = unreadCount,
                    onTambahSapi = { innerNavController.navigate(Screen.TambahSapi.route) },
                    onDetailSapi = { id -> innerNavController.navigate(Screen.DetailSapi.createRoute(id)) },
                    onNotificationClick = {
                        innerNavController.navigate(Screen.Notifications.route)
                    },
                    onLogout = onLogout, onSwitchAccount = onSwitchAccount)
            }
            composable(BottomTab.KeluhanMasuk.route) {
                KeluhanMasukScreen(onDetailKeluhan = { id -> innerNavController.navigate(Screen.DetailKeluhan.createRoute(id)) })
            }
            composable(BottomTab.JadwalPengurus.route) {
                JadwalKunjunganScreen(onTindakLanjut = { jadwalId, sapiId ->
                    innerNavController.navigate(Screen.TindakLanjutJadwal.createRoute(jadwalId, sapiId)) })
            }
            composable(BottomTab.RiwayatPengurus.route) {
                RiwayatDashboardScreen(canAddKunjungan = true,
                    onDetailSapi = { id -> innerNavController.navigate(Screen.DetailSapi.createRoute(id)) },
                    onRiwayatSapi = { sapiId, namaSapi ->
                        innerNavController.navigate(Screen.RiwayatSapi.createRoute(sapiId, namaSapi)) })
            }

            // ── PETERNAK ───────────────────────────────────────────────────────
            composable(BottomTab.SapiSaya.route) {
                SapiSayaScreen(
                    session = session,
                    savedAccountsCount = savedAccounts.size,
                    unreadCount = unreadCount,
                    onNotificationClick = {
                        innerNavController.navigate(Screen.Notifications.route)
                    },
                    onTambahSapi = { innerNavController.navigate(Screen.TambahSapi.route) },
                    onDetailSapi = { id -> innerNavController.navigate(Screen.DetailSapi.createRoute(id)) },
                    onLogout = onLogout, onSwitchAccount = onSwitchAccount)
            }
            composable(BottomTab.KirimKeluhan.route) { KirimKeluhanScreen(session = session) }
            composable(BottomTab.RiwayatKeluhanPeternak.route) { RiwayatKeluhanPeternakScreen(session = session) }

            // ── DINAS ──────────────────────────────────────────────────────────
            composable(BottomTab.SapiDinas.route) {
                SapiDashboardScreen(
                    canAddSapi = false,
                    username = session?.username ?: "",
                    unreadCount = unreadCount,
                    savedAccountsCount = savedAccounts.size, onTambahSapi = {},
                    onDetailSapi = { id -> innerNavController.navigate(Screen.DetailSapi.createRoute(id)) },
                    onNotificationClick = {
                        innerNavController.navigate(Screen.Notifications.route)
                    },
                    onLogout = onLogout, onSwitchAccount = onSwitchAccount)
            }
            composable(BottomTab.KeluhanDinas.route) { KeluhanDinasScreen() }
            composable(BottomTab.JadwalDinas.route) { JadwalDinasScreen() }
            composable(BottomTab.RiwayatDinas.route) {
                RiwayatDashboardScreen(canAddKunjungan = false,
                    onDetailSapi = { id -> innerNavController.navigate(Screen.DetailSapi.createRoute(id)) },
                    onRiwayatSapi = { sapiId, namaSapi ->
                        innerNavController.navigate(Screen.RiwayatSapi.createRoute(sapiId, namaSapi)) })
            }
            composable(BottomTab.KelolaAkun.route) {
                KelolaAkunScreen(dinasUid = session?.uid ?: "")
            }

            // ── SHARED ─────────────────────────────────────────────────────────
            composable(route = Screen.DetailSapi.route,
                arguments = listOf(navArgument("sapiId") { type = NavType.StringType })) { backStackEntry ->
                val sapiId = backStackEntry.arguments!!.getString("sapiId")!!
                when {
                    isDinas -> DetailSapiDinasScreen(sapiId = sapiId,
                        onBack = { innerNavController.popBackStack() },
                        onRiwayat = { id, nama -> innerNavController.navigate(Screen.RiwayatSapi.createRoute(id, nama)) })
                    isPeternak -> DetailSapiPengurusScreen(sapiId = sapiId,
                        onBack = { innerNavController.popBackStack() },
                        onEdit = { id -> innerNavController.navigate(Screen.EditSapi.createRoute(id)) },
                        onDeleted = { innerNavController.popBackStack() },
                        onRiwayat = { id, nama -> innerNavController.navigate(Screen.RiwayatSapi.createRoute(id, nama)) },
                        canEditDelete = { sapiOwnerId -> sapiOwnerId == session?.uid })
                    else -> DetailSapiPengurusScreen(sapiId = sapiId,
                        onBack = { innerNavController.popBackStack() },
                        onEdit = { id ->
                            innerNavController.navigate(Screen.EditSapi.createRoute(id)) },
                        onDeleted = { innerNavController.popBackStack() },
                        onRiwayat = { id, nama ->
                            innerNavController.navigate(Screen.RiwayatSapi.createRoute(id, nama)) },
                        canEditDelete = { false })
                }
            }
            composable(Screen.TambahSapi.route) {
                SapiFormScreen(sapiId = null, currentSession = session,
                    onBack = { innerNavController.popBackStack() },
                    onSaved = { innerNavController.popBackStack() })
            }
            composable(route = Screen.EditSapi.route,
                arguments = listOf(navArgument("sapiId") { type = NavType.StringType })) { backStackEntry ->
                SapiFormScreen(sapiId = backStackEntry.arguments?.getString("sapiId"),
                    currentSession = session,
                    onBack = { innerNavController.popBackStack() },
                    onSaved = { innerNavController.popBackStack() })
            }
            composable(route = Screen.RiwayatSapi.route,
                arguments = listOf(
                    navArgument("sapiId") { type = NavType.StringType },
                    navArgument("namaSapi") { type = NavType.StringType }
                )) { backStackEntry ->
                RiwayatKunjunganScreen(
                    sapiId = backStackEntry.arguments!!.getString("sapiId")!!,
                    namaSapi = backStackEntry.arguments!!.getString("namaSapi")!!,
                    onBack = { innerNavController.popBackStack() })
            }
            composable(route = Screen.DetailKeluhan.route,
                arguments = listOf(navArgument("keluhanId") { type = NavType.StringType })) { backStackEntry ->
                DetailKeluhanScreen(keluhanId = backStackEntry.arguments!!.getString("keluhanId")!!,
                    onBack = { innerNavController.popBackStack() },
                    onJadwalkan = { innerNavController.popBackStack() })
            }
            composable(route = Screen.TindakLanjutJadwal.route,
                arguments = listOf(
                    navArgument("jadwalId") { type = NavType.StringType },
                    navArgument("sapiId") { type = NavType.StringType }
                )) { backStackEntry ->
                TindakLanjutScreen(
                    jadwalId = backStackEntry.arguments!!.getString("jadwalId")!!,
                    sapiId = backStackEntry.arguments!!.getString("sapiId")!!,
                    onBack = { innerNavController.popBackStack() },
                    onSelesai = { innerNavController.popBackStack() })
            }
        }
    }
}
