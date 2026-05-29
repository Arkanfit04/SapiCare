package com.sapicare.app.ui.pengurus.jadwal

import androidx.compose.animation.AnimatedVisibility
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
import com.sapicare.app.ui.components.DropdownField
import com.sapicare.app.util.DataOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TindakLanjutScreen(
    jadwalId: String,
    sapiId: String,
    onBack: () -> Unit,
    onSelesai: () -> Unit,
    viewModel: TindakLanjutViewModel = hiltViewModel()
) {
    val formState by viewModel.formState.collectAsState()

    LaunchedEffect(jadwalId, sapiId) { viewModel.init(jadwalId, sapiId) }
    LaunchedEffect(formState.isSaved) { if (formState.isSaved) onSelesai() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tindak Lanjut Kunjungan", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White) } },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2E7D32), titleContentColor = Color.White)
            )
        }, containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)
            .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {

            AnimatedVisibility(visible = formState.error != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(formState.error ?: "", modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }

            // Sapi info
            if (formState.namaSapi.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(Color(0xFFE8F5E9)), elevation = CardDefaults.cardElevation(0.dp)) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("🐄", fontSize = 32.sp)
                        Column {
                            Text(formState.namaSapi, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text("Tgl Kunjungan: ${formState.tanggalKunjungan}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(0.dp)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Hasil Pemeriksaan", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF2E7D32))

                    OutlinedTextField(value = formState.kondisiSapi, onValueChange = viewModel::onKondisiChange,
                        label = { Text("Kondisi / Keluhan Sapi *") },
                        leadingIcon = { Icon(Icons.Default.MonitorHeart, null) },
                        modifier = Modifier.fillMaxWidth(), maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32)))

                    DropdownField(value = formState.diagnosis, onValueChange = viewModel::onDiagnosisChange,
                        label = "Diagnosis *", options = DataOptions.diagnosisSapi, leadingIcon = Icons.Default.MedicalInformation)
                    DropdownField(value = formState.tindakan, onValueChange = viewModel::onTindakanChange,
                        label = "Tindakan *", options = DataOptions.tindakanSapi, leadingIcon = Icons.Default.MedicalServices)
                    DropdownField(value = formState.obat, onValueChange = viewModel::onObatChange,
                        label = "Obat yang Diberikan", options = DataOptions.obatSapi, leadingIcon = Icons.Default.Medication)

                    OutlinedTextField(value = formState.catatan, onValueChange = viewModel::onCatatanChange,
                        label = { Text("Catatan Tambahan") },
                        leadingIcon = { Icon(Icons.Default.Notes, null) },
                        modifier = Modifier.fillMaxWidth(), maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32)))

                    Text("Update Status Sapi", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Sehat", "Dalam Perawatan").forEach { status ->
                            FilterChip(selected = formState.statusSapi == status,
                                onClick = { viewModel.onStatusSapiChange(status) },
                                label = { Text(status, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (status == "Sehat") Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                    selectedLabelColor = if (status == "Sehat") Color(0xFF2E7D32) else Color(0xFFE65100)
                                ))
                        }
                    }
                }
            }

            Button(onClick = { viewModel.simpan() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !formState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp)) {
                if (formState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                else { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Simpan & Selesaikan", fontWeight = FontWeight.SemiBold) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
