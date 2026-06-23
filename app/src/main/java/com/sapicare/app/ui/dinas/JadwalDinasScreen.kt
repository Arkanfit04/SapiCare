package com.sapicare.app.ui.dinas

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
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.JadwalKunjungan
import com.sapicare.app.data.model.StatusJadwal
import com.sapicare.app.data.repository.JadwalRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class JadwalDinasViewModel @Inject constructor(
    jadwalRepository: JadwalRepository
) : androidx.lifecycle.ViewModel() {
    val jadwalList: StateFlow<List<JadwalKunjungan>> = jadwalRepository.getAllJadwal()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JadwalDinasScreen(viewModel: JadwalDinasViewModel = hiltViewModel()) {
    val jadwalList by viewModel.jadwalList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Jadwal Kunjungan", fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1565C0), titleContentColor = Color.White))
        }, containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (jadwalList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📅", fontSize = 56.sp); Spacer(Modifier.height(12.dp))
                    Text("Belum ada jadwal", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF424242))
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Text("${jadwalList.size} jadwal", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp)) }
                items(jadwalList, key = { it.id }) { jadwal -> JadwalDinasCard(jadwal) }
            }
        }
    }
}

@Composable
fun JadwalDinasCard(jadwal: JadwalKunjungan) {
    val statusColor = when (jadwal.status) {
        StatusJadwal.TERJADWAL -> Color(0xFF1565C0)
        StatusJadwal.SELESAI -> Color(0xFF2E7D32)
        StatusJadwal.DIBATALKAN -> Color.Gray
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    jadwal.namaSapi,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp)
                Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(0.15f)) {
                    Text(jadwal.status.name.lowercase().replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                        fontSize = 11.sp, color = statusColor, fontWeight = FontWeight.SemiBold)
                }
            }
            Text("${jadwal.peternak} • ${jadwal.wilayah}", fontSize = 12.sp, color = Color.Gray)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF1565C0), modifier = Modifier.size(14.dp))
                    Text(jadwal.tanggalKunjungan, fontSize = 13.sp, color = Color(0xFF1565C0))
                }
                Text("oleh ${jadwal.pengurus}", fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
