package com.sapicare.app.ui.auth

import android.app.Activity
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.sapicare.app.R
import com.sapicare.app.data.model.UserRole

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (UserRole) -> Unit,
    onNavigateToRegister: () -> Unit,
    autoNavigateTo: String?,
    onAutoNavigate: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDinasLogin by remember { mutableStateOf(false) }

    LaunchedEffect(autoNavigateTo) { autoNavigateTo?.let { onAutoNavigate(it) } }

    if (uiState.showRolePicker) {
        RolePickerScreen(
            username = uiState.googleUsername,
            isLoading = uiState.isLoading,
            error = uiState.error,
            onRoleSelected = { role ->
                viewModel.selectRole(role) { selectedRole ->
                    onLoginSuccess(selectedRole)
                }
            },
            onBack = {
                viewModel.cancelRolePicker()
            }
        )
        return
    }

    if (showDinasLogin) {
        DinasLoginSheet(
            isLoading = uiState.isLoading,
            error = uiState.error,
            onLogin = { email, password ->
                viewModel.loginDinas(email, password) { onLoginSuccess(UserRole.DINAS) }
            },
            onDismiss = { showDinasLogin = false; viewModel.clearError() }
        )
        return
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode != Activity.RESULT_OK) {
            viewModel.setError(
                "Login Google dibatalkan atau tidak dapat diproses. Periksa koneksi internet."
            )
            return@rememberLauncherForActivityResult
        }

        try {
            val account = GoogleSignIn
                .getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)

            account?.idToken?.let {
                viewModel.loginWithGoogle(it, onLoginSuccess)
            }

        } catch (e: ApiException) {
            viewModel.setError(
                "Login Google gagal. Periksa koneksi internet."
            )
        }
    }

    fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail().build()
        val client = GoogleSignIn.getClient(context, gso)
        client.signOut().addOnCompleteListener {
            googleSignInLauncher.launch(client.signInIntent)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(colors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF388E3C)))
    )) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {

            Surface(modifier = Modifier.size(100.dp), shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.2f)) {
                Box(contentAlignment = Alignment.Center) { Text("🐄", fontSize = 52.sp) }
            }
            Spacer(Modifier.height(20.dp))
            Text("SapiCare", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Sistem Manajemen Ternak Sapi\nDinas Pertanian Kota Cimahi",
                fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), textAlign = TextAlign.Center)
            Spacer(Modifier.height(48.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)) {
                Column(modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {

                    Text("Masuk", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1B5E20))

                    AnimatedVisibility(visible = uiState.error != null) {
                        Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(8.dp))
                                Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                            }
                        }
                    }

                    // Tombol Google — untuk Pengurus & Peternak
                    Button(
                        onClick = { signInWithGoogle() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color(0xFF3C4043)),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(2.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFF2E7D32), strokeWidth = 2.dp)
                        } else {
                            Text("G", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4285F4))
                            Spacer(Modifier.width(12.dp))
                            Text("Masuk dengan Google", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE0E0E0))

                    // Tombol Dinas — email & password
                    OutlinedButton(
                        onClick = { showDinasLogin = true },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1565C0)),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = SolidColor(Color(0xFF1565C0))
                        )
                    ) {
                        Icon(Icons.Default.AccountBalance, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Masuk sebagai Dinas", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Dengan masuk, Anda menyetujui penggunaan data untuk keperluan pendataan ternak Kota Cimahi.",
                fontSize = 9.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

@Composable
fun DinasLoginSheet(
    isLoading: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(colors = listOf(Color(0xFF0D47A1), Color(0xFF1565C0), Color(0xFF1976D2)))
    )) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {

            Surface(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(0.2f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AccountBalance, null, tint = Color.White, modifier = Modifier.size(40.dp))
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Login Dinas", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Akun khusus Dinas Pertanian", fontSize = 14.sp, color = Color.White.copy(0.8f))
            Spacer(Modifier.height(32.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(8.dp)) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {

                    AnimatedVisibility(visible = error != null) {
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                                Spacer(Modifier.width(8.dp))
                                Text(error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = email, onValueChange = { email = it },
                        label = { Text("Email Dinas") },
                        leadingIcon = { Icon(Icons.Default.Email, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Black,

                            focusedLabelColor = Color(0xFF2E7D32),
                            unfocusedLabelColor = Color.Gray,
                            disabledLabelColor = Color.Gray,

                            focusedBorderColor = Color(0xFF2E7D32),
                            unfocusedBorderColor = Color.Gray,
                            disabledBorderColor = Color.Gray,

                            focusedLeadingIconColor = Color.Gray,
                            unfocusedLeadingIconColor = Color.Gray,

                            focusedTrailingIconColor = Color(0xFF2E7D32),
                            unfocusedTrailingIconColor = Color.Gray
                        )
                    )

                    OutlinedTextField(
                        value = password, onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(), singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            disabledTextColor = Color.Black,

                            focusedLabelColor = Color(0xFF2E7D32),
                            unfocusedLabelColor = Color.Gray,
                            disabledLabelColor = Color.Gray,

                            focusedBorderColor = Color(0xFF2E7D32),
                            unfocusedBorderColor = Color.Gray,
                            disabledBorderColor = Color.Gray,

                            focusedLeadingIconColor = Color.Gray,
                            unfocusedLeadingIconColor = Color.Gray,

                            focusedTrailingIconColor = Color(0xFF2E7D32),
                            unfocusedTrailingIconColor = Color.Gray
                        )
                    )

                    Button(
                        onClick = { onLogin(email, password) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else { Icon(Icons.AutoMirrored.Filled.Login, null); Spacer(Modifier.width(8.dp)); Text("Masuk", fontWeight = FontWeight.SemiBold, fontSize = 15.sp) }
                    }

                    OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)) { Text("Kembali") }
                }
            }
        }
    }
}
