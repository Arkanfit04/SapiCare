package com.sapicare.app.ui.pengurus.keluhan

import androidx.compose.foundation.clickable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeluhanMasukScreen(
    onDetailKeluhan: (String) -> Unit,
    viewModel: KeluhanMasukViewModel = hiltViewModel()
) {
    val keluhanList by viewModel.keluhanList.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Keluhan Masuk", fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32), titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filter chips
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null, StatusKeluhan.MENUNGGU, StatusKeluhan.DIJADWALKAN, StatusKeluhan.SELESAI)
                    .forEach { status ->
                        val label = when (status) {
                            null -> "Semua"
                            StatusKeluhan.MENUNGGU -> "Menunggu"
                            StatusKeluhan.DIJADWALKAN -> "Dijadwalkan"
                            StatusKeluhan.SELESAI -> "Selesai"
                        }
                        FilterChip(
                            selected = filterStatus == status,
                            onClick = { viewModel.setFilter(status) },
                            label = { Text(label, fontSize = 9.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2E7D32),
                                selectedLabelColor = Color.White,

                                containerColor = Color.White,
                                labelColor = Color.Black
                            )
                        )
                    }
            }

            if (keluhanList.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text("Belum ada keluhan masuk", fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp, color = Color(0xFF424242))
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Text("${keluhanList.size} keluhan", fontSize = 12.sp, color = Color.Gray,
                        modifier = Modifier.padding(bottom = 4.dp)) }
                    items(keluhanList, key = { it.id }) { keluhan ->
                        KeluhanMasukCard(keluhan = keluhan, onClick = { onDetailKeluhan(keluhan.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun KeluhanMasukCard(keluhan: Keluhan, onClick: () -> Unit) {
    val statusColor = when (keluhan.status) {
        StatusKeluhan.MENUNGGU -> Color(0xFFFF9800)
        StatusKeluhan.DIJADWALKAN -> Color(0xFF1565C0)
        StatusKeluhan.SELESAI -> Color(0xFF2E7D32)
    }
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        keluhan.namaSapi,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        "dari ${keluhan.peternak} • ${keluhan.wilayah}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(0.15f)) {
                    Text(keluhan.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                }
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Text(keluhan.deskripsiKeluhan, fontSize = 13.sp, color = Color(0xFF424242), maxLines = 2)
            if (keluhan.gejala.isNotEmpty()) {
                Text("Gejala: ${keluhan.gejala.take(3).joinToString(", ")}${if (keluhan.gejala.size > 3) "..." else ""}",
                    fontSize = 12.sp, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Default.CalendarMonth, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                Text(keluhan.tanggalKeluhan, fontSize = 11.sp, color = Color.Gray)
                if (keluhan.usulanTanggalKunjungan.isNotEmpty()) {
                    Text("• Usul kunjungan: ${keluhan.usulanTanggalKunjungan}", fontSize = 11.sp, color = Color(0xFF1565C0))
                }
            }
        }
    }
}
