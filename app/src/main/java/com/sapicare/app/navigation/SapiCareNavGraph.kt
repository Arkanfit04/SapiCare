package com.sapicare.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.*
import com.sapicare.app.data.model.UserRole
import com.sapicare.app.ui.auth.AccountSwitcherScreen
import com.sapicare.app.ui.auth.AuthViewModel
import com.sapicare.app.ui.auth.LoginScreen
import com.sapicare.app.ui.main.MainScreen

@Composable
fun SapiCareNavGraph() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val session by authViewModel.sessionFlow.collectAsState(initial = null)
    var isAuthChecked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(800)
        isAuthChecked = true
    }

    if (!isAuthChecked) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF2E7D32)) {
            Box(contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🐄", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("SapiCare", color = Color.White, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(color = Color.White)
                }
            }
        }
        return
    }

    val startDestination = when (session?.role) {
        UserRole.PENGURUS, UserRole.DINAS, UserRole.PETERNAK -> Screen.Main.route
        else -> Screen.Login.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { _ ->
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onNavigateToRegister = {},
                autoNavigateTo = null,
                onAutoNavigate = {}
            )
        }

        composable(Screen.Main.route) {
            MainScreen(
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.logoutAll()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSwitchAccount = {
                    navController.navigate(Screen.AccountSwitcher.route)
                }
            )
        }

        composable(Screen.AccountSwitcher.route) {
            val currentSession by authViewModel.sessionFlow.collectAsState(initial = null)
            AccountSwitcherScreen(
                viewModel = authViewModel,
                currentUid = currentSession?.uid ?: "",
                onSwitchAccount = { _ ->
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Main.route) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
