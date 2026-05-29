package com.sapicare.app.ui.pengurus.keluhan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.sapicare.app.data.model.StatusKeluhan
import com.sapicare.app.ui.components.DatePickerField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailKeluhanScreen(
    keluhanId: String,
    onBack: () -> Unit,
    onJadwalkan: () -> Unit,
    viewModel: DetailKeluhanViewModel = hiltViewModel()
) {
    val keluhan by viewModel.keluhan.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var showJadwalDialog by remember { mutableStateOf(false) }
    var tanggalJadwal by remember { mutableStateOf("") }
    var waktuJadwal by remember { mutableStateOf("09:00") }
    var catatanJadwal by remember { mutableStateOf("") }

    LaunchedEffect(keluhanId) { viewModel.load(keluhanId) }
    LaunchedEffect(uiState.isJadwalSaved) { if (uiState.isJadwalSaved) onJadwalkan() }

    if (showJadwalDialog && keluhan != null) {
        AlertDialog(onDismissRequest = { showJadwalDialog = false }) {
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(Color.White)) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Jadwalkan Kunjungan", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1B5E20))
                    DatePickerField(value = tanggalJadwal.ifEmpty { keluhan!!.usulanTanggalKunjungan },
                        onValueChange = { tanggalJadwal = it }, label = "Tanggal Kunjungan *")
                    OutlinedTextField(value = waktuJadwal, onValueChange = { waktuJadwal = it },
                        label = { Text("Waktu (misal 09:00)") },
                        leadingIcon = { Icon(Icons.Default.Schedule, null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32)))
                    OutlinedTextField(value = catatanJadwal, onValueChange = { catatanJadwal = it },
                        label = { Text("Catatan untuk Peternak") },
                        leadingIcon = { Icon(Icons.Default.Notes, null) },
                        modifier = Modifier.fillMaxWidth(), maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32)))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { showJadwalDialog = false }, modifier = Modifier.weight(1f)) { Text("Batal") }
                        Button(
                            onClick = {
                                viewModel.jadwalkan(keluhan!!, tanggalJadwal, waktuJadwal, catatanJadwal)
                                showJadwalDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                        ) { Text("Simpan") }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detail Keluhan", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2E7D32), titleContentColor = Color.White)
            )
        }, containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator(color = Color(0xFF2E7D32)) }
            keluhan == null -> Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Data tidak ditemukan") }
            else -> {
                val k = keluhan!!
                Column(modifier = Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState()).padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {

                    // Header
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(Color(0xFF2E7D32))) {
                        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(k.namaSapi, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White)
                            Text("dari ${k.peternak} • ${k.wilayah}", fontSize = 13.sp, color = Color.White.copy(0.85f))
                            Text(k.tanggalKeluhan, fontSize = 12.sp, color = Color.White.copy(0.7f))
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(0.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Deskripsi Keluhan", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                            Text(k.deskripsiKeluhan, fontSize = 14.sp, color = Color(0xFF212121))
                            if (k.gejala.isNotEmpty()) {
                                HorizontalDivider(color = Color(0xFFF0F0F0))
                                Text("Gejala", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                                k.gejala.forEach { g ->
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Default.Warning, null, tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                                        Text(g, fontSize = 13.sp)
                                    }
                                }
                            }
                            if (k.usulanTanggalKunjungan.isNotEmpty()) {
                                HorizontalDivider(color = Color(0xFFF0F0F0))
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.CalendarMonth, null, tint = Color(0xFF1565C0), modifier = Modifier.size(14.dp))
                                    Text("Usul kunjungan: ${k.usulanTanggalKunjungan}", fontSize = 13.sp, color = Color(0xFF1565C0))
                                }
                            }
                        }
                    }

                    if (k.status == StatusKeluhan.MENUNGGU) {
                        Button(
                            onClick = { tanggalJadwal = k.usulanTanggalKunjungan; showJadwalDialog = true },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Jadwalkan Kunjungan", fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
