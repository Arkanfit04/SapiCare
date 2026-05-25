package com.sapicare.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sapicare.app.data.model.SavedAccount
import com.sapicare.app.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSwitcherScreen(
    viewModel: AuthViewModel,
    currentUid: String,
    onSwitchAccount: (UserRole) -> Unit,
    onBack: () -> Unit
) {
    val savedAccounts by viewModel.savedAccountsFlow.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Akun", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32),
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    "Akun Tersimpan",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.Gray
                )
            }

            items(savedAccounts, key = { it.uid }) { account ->
                SavedAccountCard(
                    account = account,
                    isActive = account.uid == currentUid,
                    onSwitch = {
                        viewModel.switchAccount(account) { role ->
                            onSwitchAccount(role)
                        }
                    },
                    onRemove = { viewModel.removeAccount(account.uid) }
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Info, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                        Text(
                            "Untuk menambah akun dengan role berbeda, logout terlebih dahulu lalu login ulang dan pilih role yang diinginkan.",
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SavedAccountCard(
    account: SavedAccount,
    isActive: Boolean,
    onSwitch: () -> Unit,
    onRemove: () -> Unit
) {
    var showRemoveDialog by remember { mutableStateOf(false) }

    if (showRemoveDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveDialog = false },
            title = { Text("Hapus Akun", fontWeight = FontWeight.Bold) },
            text = { Text("Hapus akun ${account.username} (${roleLabel(account.role)}) dari daftar?") },
            confirmButton = {
                Button(
                    onClick = { onRemove(); showRemoveDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Hapus") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRemoveDialog = false }) { Text("Batal") }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!isActive) Modifier.clickable { onSwitch() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) Color(0xFFE8F5E9) else Color.White
        ),
        elevation = CardDefaults.cardElevation(0.dp),
        border = if (isActive) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF2E7D32))
        ) else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar dengan warna sesuai role
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(roleColor(account.role)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    account.username.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        account.username,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = Color(0xFF212121)
                    )
                    if (isActive) {
                        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF2E7D32)) {
                            Text(
                                "Aktif",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                Text(account.email, fontSize = 12.sp, color = Color.Gray)
                Spacer(Modifier.height(2.dp))
                // Role badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = roleBgColor(account.role)
                ) {
                    Text(
                        "${roleIcon(account.role)} ${roleLabel(account.role)}",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 11.sp,
                        color = roleColor(account.role)
                    )
                }
            }

            if (!isActive) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    TextButton(
                        onClick = onSwitch,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Pakai", color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    IconButton(onClick = { showRemoveDialog = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

// Helper functions untuk role
fun roleLabel(role: UserRole) = when (role) {
    UserRole.PENGURUS -> "Pengurus / Dokter"
    UserRole.PETERNAK -> "Peternak"
    UserRole.DINAS -> "Dinas"
}

fun roleIcon(role: UserRole) = when (role) {
    UserRole.PENGURUS -> "👨‍⚕️"
    UserRole.PETERNAK -> "👨‍🌾"
    UserRole.DINAS -> "🏛️"
}

fun roleColor(role: UserRole) = when (role) {
    UserRole.PENGURUS -> Color(0xFF2E7D32)
    UserRole.PETERNAK -> Color(0xFF795548)
    UserRole.DINAS -> Color(0xFF1565C0)
}

fun roleBgColor(role: UserRole) = when (role) {
    UserRole.PENGURUS -> Color(0xFFE8F5E9)
    UserRole.PETERNAK -> Color(0xFFEFEBE9)
    UserRole.DINAS -> Color(0xFFE3F2FD)
}
