package com.sapicare.app.ui.peternak

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sapicare.app.data.model.Keluhan
import com.sapicare.app.data.model.StatusKeluhan
import com.sapicare.app.data.model.UserSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatKeluhanPeternakScreen(
    session: UserSession?,
    viewModel: RiwayatKeluhanPeternakViewModel = hiltViewModel()
) {
    val keluhanList by viewModel.keluhanList.collectAsState()
    LaunchedEffect(session) { viewModel.init(session) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Keluhan", fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF795548), titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (keluhanList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 56.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Belum ada riwayat keluhan", fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp, color = Color(0xFF424242))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Text("${keluhanList.size} keluhan", fontSize = 12.sp, color = Color.Gray,
                    modifier = Modifier.padding(bottom = 4.dp)) }
                items(keluhanList, key = { it.id }) { keluhan ->
                    KeluhanPeternakCard(keluhan)
                }
            }
        }
    }
}

@Composable
fun KeluhanPeternakCard(keluhan: Keluhan) {
    val statusColor = when (keluhan.status) {
        StatusKeluhan.MENUNGGU -> Color(0xFFFF9800)
        StatusKeluhan.DIJADWALKAN -> Color(0xFF1565C0)
        StatusKeluhan.SELESAI -> Color(0xFF2E7D32)
    }
    val statusLabel = when (keluhan.status) {
        StatusKeluhan.MENUNGGU -> "Menunggu"
        StatusKeluhan.DIJADWALKAN -> "Dijadwalkan"
        StatusKeluhan.SELESAI -> "Selesai"
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(keluhan.namaSapi, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF4E342E))
                Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(0.15f)) {
                    Text(statusLabel, modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                }
            }
            Text(keluhan.tanggalKeluhan, fontSize = 12.sp, color = Color.Gray)
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Text(keluhan.deskripsiKeluhan, fontSize = 13.sp, color = Color(0xFF212121), maxLines = 3)
            if (keluhan.gejala.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Warning, null, tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                    Text(keluhan.gejala.joinToString(", "), fontSize = 12.sp, color = Color.Gray)
                }
            }
            if (keluhan.tanggapanPengurus.isNotEmpty()) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFE3F2FD),
                    modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.MedicalServices, null, tint = Color(0xFF1565C0), modifier = Modifier.size(14.dp))
                        Text("Pengurus: ${keluhan.tanggapanPengurus}", fontSize = 12.sp, color = Color(0xFF1565C0))
                    }
                }
            }
        }
    }
}
