package com.sapicare.app.ui.pengurus.detail

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sapicare.app.ui.components.SectionTitle
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailSapiPengurusScreen(
    sapiId: String,
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleted: () -> Unit,
    onRiwayat: (String, String) -> Unit,
    /** canEditDelete(ownerId) — true kalau boleh edit/hapus */
    canEditDelete: (String) -> Boolean = { true },
    viewModel: DetailSapiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sapiId) { viewModel.loadSapi(sapiId) }
    LaunchedEffect(uiState.isDeleted) { if (uiState.isDeleted) onDeleted() }

    val allowEdit = uiState.sapi?.let { canEditDelete(it.ownerId) } ?: false

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hapus Data Sapi", fontWeight = FontWeight.Bold) },
            text = { Text("Data sapi \"${uiState.sapi?.nama}\" akan dihapus permanen. Yakin?") },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteSapi(); showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Hapus") }
            },
            dismissButton = { OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Batal") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.sapi?.nama ?: "Detail Sapi", fontWeight = FontWeight.Bold) },
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
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2E7D32))
            }
            uiState.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.error ?: "", color = MaterialTheme.colorScheme.error)
            }
            uiState.sapi != null -> {
                val sapi = uiState.sapi!!
                val umur = hitungUmur(sapi.tanggalLahir)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(110.dp).clip(RoundedCornerShape(55.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (sapi.fotoUrl.isNotEmpty()) {
                                    AsyncImage(model = sapi.fotoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                                } else {
                                    Surface(modifier = Modifier.fillMaxSize(),
                                        color = Color.White.copy(0.2f),
                                        shape = RoundedCornerShape(55.dp))
                                    {
                                        Box(contentAlignment = Alignment.Center) { Text("🐄", fontSize = 52.sp) }
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                sapi.nama,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.White)
                            Text(
                                "${sapi.jenisSapi} • ${sapi.jenisKelamin}",
                                fontSize = 13.sp,
                                color = Color.White.copy(0.8f))
                            Spacer(Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (sapi.status == "Sehat") Color(0xFF4CAF50) else Color(
                                        0xFFFF9800
                                    )
                                ) {
                                    Text(
                                        sapi.status,
                                        modifier = Modifier.padding(
                                            horizontal = 12.dp,
                                            vertical = 4.dp
                                        ),
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (sapi.beratBadan > 0) {
                                    Surface(shape = RoundedCornerShape(20.dp), color = Color.White.copy(0.2f)) {
                                        Text("${sapi.beratBadan.toInt()} kg",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            color = Color.White, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Info
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp))
                        {
                            SectionTitle("Informasi Sapi")
                            DetailRow(Icons.Default.Badge, "Nama / ID", sapi.nama)
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            DetailRow(Icons.Default.Category, "Jenis Sapi", sapi.jenisSapi)
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            DetailRow(Icons.Default.Transgender, "Jenis Kelamin", sapi.jenisKelamin)
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            DetailRow(Icons.Default.CalendarMonth, "Tanggal Lahir", sapi.tanggalLahir)

                            if (umur.isNotEmpty()) {
                                HorizontalDivider(color = Color(0xFFF0F0F0))
                                DetailRow(Icons.Default.Cake, "Umur", umur)
                            }

                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            DetailRow(Icons.Default.Person, "Pemilik", sapi.namaPemilik)
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            DetailRow(Icons.Default.LocationOn, "Wilayah", sapi.wilayah)
                            if (sapi.keterangan.isNotEmpty()) {
                                HorizontalDivider(color = Color(0xFFF0F0F0))
                                DetailRow(Icons.Default.Notes, "Keterangan", sapi.keterangan)
                            }
                        }
                    }

                    // Perawatan
                    if (sapi.jenisPerawatan.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                SectionTitle("Jenis Perawatan")
                                sapi.jenisPerawatan.forEachIndexed { index, perawatan ->
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Surface(shape = RoundedCornerShape(50), color = Color(0xFF2E7D32), modifier = Modifier.size(24.dp)) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("${index + 1}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                        Text(perawatan, fontSize = 14.sp, color = Color(0xFF212121))
                                    }
                                    if (index < sapi.jenisPerawatan.size - 1) HorizontalDivider(color = Color(0xFFF0F0F0))
                                }
                            }
                        }
                    }

                    // Tombol Riwayat
                    Button(
                        onClick = { onRiwayat(sapi.id, sapi.nama) },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.History, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Riwayat Kunjungan", fontWeight = FontWeight.SemiBold)
                    }

                    // Edit & Hapus — hanya tampil kalau boleh
                    if (allowEdit) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showDeleteDialog = true },
                                modifier = Modifier.weight(1f).height(50.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Hapus", fontWeight = FontWeight.SemiBold)
                            }
                            Button(
                                onClick = { onEdit(sapi.id) },
                                modifier = Modifier.weight(1f).height(50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF2E7D32
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Edit", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun hitungUmur(tanggalLahir: String): String {
    if (tanggalLahir.isBlank()) return ""
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("id", "ID"))
        val lahir = sdf.parse(tanggalLahir) ?: return ""
        val sekarang = Calendar.getInstance()
        val lahirCal = Calendar.getInstance().apply { time = lahir }
        var tahun = sekarang.get(Calendar.YEAR) - lahirCal.get(Calendar.YEAR)
        var bulan = sekarang.get(Calendar.MONTH) - lahirCal.get(Calendar.MONTH)
        if (bulan < 0) { tahun--; bulan += 12 }
        when {
            tahun > 0 && bulan > 0 -> "$tahun tahun $bulan bulan"
            tahun > 0 -> "$tahun tahun"
            bulan > 0 -> "$bulan bulan"
            else -> "< 1 bulan"
        }
    } catch (e: Exception) { "" }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp).padding(top = 2.dp))
        Column {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF212121))
        }
    }
}
