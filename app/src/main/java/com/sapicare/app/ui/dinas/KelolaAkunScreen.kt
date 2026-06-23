package com.sapicare.app.ui.dinas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sapicare.app.data.model.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KelolaAkunScreen(
    dinasUid: String,
    viewModel: KelolaAkunViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val filterRole by viewModel.filterRole.collectAsState()

    LaunchedEffect(Unit) { viewModel.load() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Kelola Akun", fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1565C0), titleContentColor = Color.White))
        }, containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Statistik
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AkunStatCard(Modifier.weight(1f), "👨‍⚕️", "Dokter",
                    uiState.profiles.count { it.role == "PENGURUS" && it.approvalStatus == "APPROVED" }.toString(),
                    Color(0xFF2E7D32))
                AkunStatCard(Modifier.weight(1f), "⏳", "Pending",
                    uiState.profiles.count { it.approvalStatus == "PENDING" }.toString(),
                    Color(0xFFFF9800))
                AkunStatCard(Modifier.weight(1f), "👨‍🌾", "Peternak",
                    uiState.profiles.count { it.role == "PETERNAK" }.toString(),
                    Color(0xFF795548))
            }

            // Filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(listOf(null, "PENGURUS", "PETERNAK")) { role ->
                    val label = when (role) { null -> "Semua"; "PENGURUS" -> "Dokter"; else -> "Peternak" }
                    FilterChip(
                        selected = filterRole == role,
                        onClick = { viewModel.setFilter(role) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF1565C0),
                            selectedLabelColor = Color.White,

                            containerColor = Color.White,
                            labelColor = Color.Black
                        )
                    )
                }
            }
            Spacer(Modifier.height(8.dp))

            when {
                uiState.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF1565C0))
                }
                uiState.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
                }
                uiState.filtered.isEmpty() -> Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👤", fontSize = 48.sp); Spacer(Modifier.height(8.dp))
                        Text("Belum ada akun terdaftar", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filtered, key = { it.uid }) { profile ->
                        UserProfileCard(
                            profile = profile,
                            onApprove = { viewModel.approve(profile.uid, dinasUid) },
                            onReject = { viewModel.reject(profile.uid) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AkunStatCard(modifier: Modifier, icon: String, label: String, value: String, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 18.sp); Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
            Text(label, fontSize = 9.sp, color = Color.Gray)
        }
    }
}

@Composable
fun UserProfileCard(profile: UserProfile, onApprove: () -> Unit, onReject: () -> Unit) {
    val isPending = profile.approvalStatus == "PENDING"
    val isApproved = profile.approvalStatus == "APPROVED"
    val isRejected = profile.approvalStatus == "REJECTED"
    val roleLabel = if (profile.role == "PENGURUS") "Dokter Hewan" else "Peternak"
    val roleIcon = if (profile.role == "PENGURUS") "👨‍⚕️" else "👨‍🌾"
    val roleColor = if (profile.role == "PENGURUS") Color(0xFF2E7D32) else Color(0xFF795548)

    var showConfirmApprove by remember { mutableStateOf(false) }
    var showConfirmReject by remember { mutableStateOf(false) }

    if (showConfirmApprove) {
        AlertDialog(
            onDismissRequest = { showConfirmApprove = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32)) },
            title = { Text("Approve Akun", fontWeight = FontWeight.Bold) },
            text = { Text("Setujui akun ${profile.username} sebagai $roleLabel?") },
            confirmButton = {
                Button(onClick = { onApprove(); showConfirmApprove = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))) { Text("Setujui") }
            },
            dismissButton = { OutlinedButton(onClick = { showConfirmApprove = false }) { Text("Batal") } }
        )
    }
    if (showConfirmReject) {
        AlertDialog(
            onDismissRequest = { showConfirmReject = false },
            icon = { Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Tolak Akun", fontWeight = FontWeight.Bold) },
            text = { Text("Tolak akun ${profile.username}? Akun tidak akan bisa menggunakan aplikasi.") },
            confirmButton = {
                Button(onClick = { onReject(); showConfirmReject = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Tolak") }
            },
            dismissButton = { OutlinedButton(onClick = { showConfirmReject = false }) { Text("Batal") } }
        )
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPending) Color(0xFFFFFDE7) else Color.White),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(shape = RoundedCornerShape(50), color = roleColor, modifier = Modifier.size(44.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(profile.username.take(1).uppercase(), color = Color.White,
                            fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        profile.username,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp)
                    Text(
                        profile.email,
                        fontSize = 12.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("$roleIcon $roleLabel", fontSize = 12.sp, color = roleColor, fontWeight = FontWeight.Medium)
                    }
                }
                // Status badge
                Surface(shape = RoundedCornerShape(20.dp), color = when {
                    isPending -> Color(0xFFFF9800).copy(0.15f)
                    isApproved -> Color(0xFF2E7D32).copy(0.15f)
                    else -> Color.Gray.copy(0.15f)
                }) {
                    Text(when {
                        isPending -> "Pending"; isApproved -> "Approved"; else -> "Ditolak"
                    }, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
                        color = when { isPending -> Color(0xFFFF9800); isApproved -> Color(0xFF2E7D32); else -> Color.Gray })
                }
            }

            // Tombol approve/reject — hanya untuk akun Pengurus yang pending
            if (isPending && profile.role == "PENGURUS") {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { showConfirmReject = true }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp)); Text("Tolak", fontSize = 13.sp)
                    }
                    Button(onClick = { showConfirmApprove = true }, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp)); Text("Approve", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
