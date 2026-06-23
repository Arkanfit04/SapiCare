package com.sapicare.app.ui.pengurus.jadwal

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
import com.sapicare.app.data.model.JadwalKunjungan
import com.sapicare.app.data.model.StatusJadwal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalKunjunganScreen(
    onTindakLanjut: (String, String) -> Unit,
    viewModel: JadwalKunjunganViewModel = hiltViewModel()
) {
    val jadwalList by viewModel.jadwalList.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Jadwal Kunjungan", fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2E7D32), titleContentColor = Color.White)
            )
        }, containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null, StatusJadwal.TERJADWAL, StatusJadwal.SELESAI, StatusJadwal.DIBATALKAN).forEach { status ->
                    val label = when (status) {
                        null -> "Semua"; StatusJadwal.TERJADWAL -> "Terjadwal"
                        StatusJadwal.SELESAI -> "Selesai"; StatusJadwal.DIBATALKAN -> "Batal"
                    }
                    FilterChip(selected = filterStatus == status, onClick = { viewModel.setFilter(status) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF2E7D32),
                            selectedLabelColor = Color.White,

                            containerColor = Color.White,
                            labelColor = Color.Black
                        )
                    )
                }
            }

            if (jadwalList.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📅", fontSize = 56.sp); Spacer(Modifier.height(12.dp))
                        Text("Belum ada jadwal kunjungan", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF424242))
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { Text("${jadwalList.size} jadwal", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp)) }
                    items(jadwalList, key = { it.id }) { jadwal ->
                        JadwalCard(jadwal = jadwal,
                            onTindakLanjut = { onTindakLanjut(jadwal.id, jadwal.sapiId) },
                            onBatalkan = {
                                viewModel.batalkanJadwal(jadwal)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun JadwalCard(jadwal: JadwalKunjungan, onTindakLanjut: () -> Unit, onBatalkan: () -> Unit) {
    val statusColor = when (jadwal.status) {
        StatusJadwal.TERJADWAL -> Color(0xFF1565C0)
        StatusJadwal.SELESAI -> Color(0xFF2E7D32)
        StatusJadwal.DIBATALKAN -> Color.Gray
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        jadwal.namaSapi,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        "${jadwal.peternak} • ${jadwal.wilayah}",
                        fontSize = 12.sp,
                        color = Color.Gray)
                }
                Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(0.15f)) {
                    Text(jadwal.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                }
            }
            HorizontalDivider(color = Color(0xFFF0F0F0))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF1565C0), modifier = Modifier.size(14.dp))
                    Text(jadwal.tanggalKunjungan, fontSize = 13.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Medium)
                }
                if (jadwal.waktuKunjungan.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Schedule, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                        Text(jadwal.waktuKunjungan, fontSize = 13.sp, color = Color.Gray)
                    }
                }
            }
            if (jadwal.catatan.isNotEmpty()) {
                Text(jadwal.catatan, fontSize = 12.sp, color = Color.Gray, maxLines = 2)
            }
            if (jadwal.status == StatusJadwal.TERJADWAL) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onBatalkan, modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                        shape = RoundedCornerShape(8.dp)) { Text("Batalkan", fontSize = 12.sp) }
                    Button(onClick = onTindakLanjut, modifier = Modifier.weight(2f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp)) {
                        Icon(Icons.Default.MedicalServices, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Tindak Lanjut", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
