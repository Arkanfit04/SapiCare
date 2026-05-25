package com.sapicare.app.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.sapicare.app.data.model.UserRole
import com.sapicare.app.navigation.BottomTab
import com.sapicare.app.navigation.Screen
import com.sapicare.app.ui.auth.AuthViewModel
import com.sapicare.app.ui.dinas.DetailSapiDinasScreen
import com.sapicare.app.ui.pengurus.detail.DetailSapiPengurusScreen
import com.sapicare.app.ui.pengurus.form.SapiFormScreen
import com.sapicare.app.ui.pengurus.list.SapiDashboardScreen
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

    // Role logic
    val isPengurus = session?.role == UserRole.PENGURUS
    val isPeternak = session?.role == UserRole.PETERNAK
    val isDinas = session?.role == UserRole.DINAS

    // Pengurus dan Peternak bisa tambah sapi
    val canAddSapi = isPengurus || isPeternak
    // Hanya Pengurus yang bisa tambah kunjungan
    val canAddKunjungan = isPengurus

    val innerNavController = rememberNavController()
    val navBackStackEntry by innerNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(BottomTab.Sapi, Icons.Filled.Pets, Icons.Outlined.Pets),
        BottomNavItem(BottomTab.Riwayat, Icons.Filled.History, Icons.Outlined.History)
    )

    val showBottomBar = currentRoute in listOf(BottomTab.Sapi.route, BottomTab.Riwayat.route)

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
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
                                    if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    item.tab.label
                                )
                            },
                            label = {
                                Text(
                                    item.tab.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
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
        NavHost(
            navController = innerNavController,
            startDestination = BottomTab.Sapi.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomTab.Sapi.route) {
                SapiDashboardScreen(
                    canAddSapi = canAddSapi,
                    username = session?.username ?: "",
                    savedAccountsCount = savedAccounts.size,
                    onTambahSapi = { innerNavController.navigate(Screen.TambahSapi.route) },
                    onDetailSapi = { id -> innerNavController.navigate(Screen.DetailSapi.createRoute(id)) },
                    onLogout = onLogout,
                    onSwitchAccount = onSwitchAccount
                )
            }

            composable(BottomTab.Riwayat.route) {
                RiwayatDashboardScreen(
                    canAddKunjungan = canAddKunjungan,
                    onDetailSapi = { id -> innerNavController.navigate(Screen.DetailSapi.createRoute(id)) },
                    onRiwayatSapi = { sapiId, namaSapi ->
                        innerNavController.navigate(Screen.RiwayatSapi.createRoute(sapiId, namaSapi))
                    }
                )
            }

            composable(
                route = Screen.DetailSapi.route,
                arguments = listOf(navArgument("sapiId") { type = NavType.StringType })
            ) { backStackEntry ->
                val sapiId = backStackEntry.arguments!!.getString("sapiId")!!
                if (isDinas) {
                    DetailSapiDinasScreen(
                        sapiId = sapiId,
                        onBack = { innerNavController.popBackStack() },
                        onRiwayat = { id, nama ->
                            innerNavController.navigate(Screen.RiwayatSapi.createRoute(id, nama))
                        }
                    )
                } else {
                    DetailSapiPengurusScreen(
                        sapiId = sapiId,
                        onBack = { innerNavController.popBackStack() },
                        onEdit = { id -> innerNavController.navigate(Screen.EditSapi.createRoute(id)) },
                        onDeleted = { innerNavController.popBackStack() },
                        onRiwayat = { id, nama ->
                            innerNavController.navigate(Screen.RiwayatSapi.createRoute(id, nama))
                        }
                    )
                }
            }

            composable(Screen.TambahSapi.route) {
                SapiFormScreen(
                    sapiId = null,
                    onBack = { innerNavController.popBackStack() },
                    onSaved = { innerNavController.popBackStack() }
                )
            }

            composable(
                route = Screen.EditSapi.route,
                arguments = listOf(navArgument("sapiId") { type = NavType.StringType })
            ) { backStackEntry ->
                SapiFormScreen(
                    sapiId = backStackEntry.arguments?.getString("sapiId"),
                    onBack = { innerNavController.popBackStack() },
                    onSaved = { innerNavController.popBackStack() }
                )
            }

            composable(
                route = Screen.RiwayatSapi.route,
                arguments = listOf(
                    navArgument("sapiId") { type = NavType.StringType },
                    navArgument("namaSapi") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                RiwayatKunjunganScreen(
                    sapiId = backStackEntry.arguments!!.getString("sapiId")!!,
                    namaSapi = backStackEntry.arguments!!.getString("namaSapi")!!,
                    onBack = { innerNavController.popBackStack() }
                )
            }
        }
    }
}
