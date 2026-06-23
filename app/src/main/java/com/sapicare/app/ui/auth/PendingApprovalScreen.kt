package com.sapicare.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.sapicare.app.data.model.ApprovalStatus

@Composable
fun PendingApprovalScreen(
    username: String,
    approvalStatus: ApprovalStatus,
    onRefresh: () -> Unit,
    onReapply: () -> Unit,
    onLogout: () -> Unit
) {
    val isRejected = approvalStatus == ApprovalStatus.REJECTED

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                colors = if (isRejected)
                    listOf(Color(0xFF7F0000), Color(0xFFB71C1C))
                else
                    listOf(Color(0xFF1B5E20), Color(0xFF2E7D32))
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Surface(modifier = Modifier.size(80.dp), shape = RoundedCornerShape(50.dp),
                color = Color.White.copy(0.2f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (isRejected) Icons.Default.Cancel else Icons.Default.HourglassEmpty,
                        null, tint = Color.White, modifier = Modifier.size(42.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            Text(
                if (isRejected) "Akun Ditolak" else "Menunggu Persetujuan",
                fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text("Halo, $username", fontSize = 16.sp, color = Color.White.copy(0.85f))
            Spacer(Modifier.height(24.dp))

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(8.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {

                    if (isRejected) {
                        Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp))
                        Text("Akun kamu ditolak oleh Dinas", fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                        Text("Hubungi Dinas Pertanian Kota Cimahi untuk informasi lebih lanjut.",
                            fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
                        Button(
                            onClick = onReapply,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Ajukan Ulang")
                        }
                    } else {
                        Icon(Icons.Default.Pending, null, tint = Color(0xFFFF9800),
                            modifier = Modifier.size(48.dp))
                        Text("Akun kamu sedang menunggu persetujuan dari Dinas Pertanian Kota Cimahi.",
                            fontSize = 14.sp, color = Color(0xFF424242), textAlign = TextAlign.Center)
                        Text("Proses verifikasi biasanya memakan waktu 1-2 hari kerja.",
                            fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)

                        Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFFFF3E0),
                            modifier = Modifier.fillMaxWidth()) {
                            Row(modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Info, null, tint = Color(0xFFFF9800), modifier = Modifier.size(16.dp))
                                Text("Kamu akan otomatis masuk setelah disetujui. Ketuk tombol Cek Status untuk memperbarui.",
                                    fontSize = 12.sp, color = Color(0xFF7B4F00))
                            }
                        }

                        Button(onClick = onRefresh, modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Cek Status", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    OutlinedButton(onClick = onLogout, modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)) {
                        Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Keluar")
                    }
                }
            }
        }
    }
}
