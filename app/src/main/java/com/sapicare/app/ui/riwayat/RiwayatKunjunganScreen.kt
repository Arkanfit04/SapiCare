package com.sapicare.app.ui.riwayat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.sapicare.app.data.model.RiwayatKunjungan
import com.sapicare.app.data.model.UserRole
import com.sapicare.app.ui.components.DatePickerField
import com.sapicare.app.ui.components.DropdownField
import com.sapicare.app.util.DataOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatKunjunganScreen(
    sapiId: String,
    namaSapi: String,
    onBack: () -> Unit,
    viewModel: RiwayatViewModel = hiltViewModel()
) {
    val riwayatList by viewModel.riwayatList.collectAsState()
    val session by viewModel.sessionFlow.collectAsState(initial = null)
    val canAddKunjungan = session?.role == UserRole.PENGURUS
    var showFormDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sapiId) { viewModel.setSapiId(sapiId) }

    if (showFormDialog) {
        TambahRiwayatDialog(
            sapiId = sapiId,
            viewModel = viewModel,
            onDismiss = {
                showFormDialog = false
                viewModel.resetForm()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Riwayat Kunjungan", fontWeight = FontWeight.Bold)
                        Text(namaSapi, fontSize = 12.sp, color = Color.White.copy(0.85f))
                    }
                },
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
        floatingActionButton = {
            if (canAddKunjungan) {
                FloatingActionButton(
                    onClick = { showFormDialog = true },
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, null)
                }
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (riwayatList.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 56.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Belum ada riwayat kunjungan",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color(0xFF424242)
                    )
                    if (canAddKunjungan) {
                        Spacer(Modifier.height(4.dp))
                        Text("Ketuk tombol + untuk menambahkan", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text("${riwayatList.size} kunjungan", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                }
                items(riwayatList, key = { it.id }) { riwayat ->
                    RiwayatCard(
                        riwayat = riwayat,
                        canDelete = canAddKunjungan,
                        onDelete = { viewModel.deleteRiwayat(riwayat.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun RiwayatCard(riwayat: RiwayatKunjungan, canDelete: Boolean, onDelete: () -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Hapus Riwayat", fontWeight = FontWeight.Bold) },
            text = { Text("Riwayat kunjungan tanggal ${riwayat.tanggal} akan dihapus. Yakin?") },
            confirmButton = {
                Button(
                    onClick = { onDelete(); showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Hapus") }
            },
            dismissButton = { OutlinedButton(onClick = { showDeleteDialog = false }) { Text("Batal") } }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE8F5E9)) {
                        Text(
                            riwayat.tanggal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("oleh ${riwayat.namaPetugas}", fontSize = 11.sp, color = Color.Gray)
                }
                if (canDelete) {
                    IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.DeleteOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF0F0F0))

            RiwayatDetailRow("Kondisi", riwayat.kondisiSapi, Icons.Default.MonitorHeart)
            RiwayatDetailRow("Diagnosis", riwayat.diagnosis, Icons.Default.MedicalInformation)
            RiwayatDetailRow("Tindakan", riwayat.tindakan, Icons.Default.MedicalServices)
            if (riwayat.obat.isNotEmpty() && riwayat.obat != "Tidak Ada") {
                RiwayatDetailRow("Obat", riwayat.obat, Icons.Default.Medication)
            }
            if (riwayat.catatan.isNotEmpty()) {
                RiwayatDetailRow("Catatan", riwayat.catatan, Icons.Default.Notes)
            }
        }
    }
}

@Composable
fun RiwayatDetailRow(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(15.dp).padding(top = 2.dp))
        Column {
            Text(label, fontSize = 10.sp, color = Color.Gray)
            Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFF212121))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TambahRiwayatDialog(sapiId: String, viewModel: RiwayatViewModel, onDismiss: () -> Unit) {
    val formState by viewModel.formState.collectAsState()

    LaunchedEffect(formState.isSaved) {
        if (formState.isSaved) onDismiss()
    }

    AlertDialog(onDismissRequest = onDismiss, modifier = Modifier.fillMaxWidth()) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Tambah Kunjungan", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1B5E20))

                AnimatedVisibility(visible = formState.error != null) {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer) {
                        Text(formState.error ?: "", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }

                DatePickerField(value = formState.tanggal, onValueChange = viewModel::onTanggalChange, label = "Tanggal Kunjungan")

                OutlinedTextField(
                    value = formState.kondisiSapi,
                    onValueChange = viewModel::onKondisiChange,
                    label = { Text("Kondisi / Keluhan Sapi *") },
                    leadingIcon = { Icon(Icons.Default.MonitorHeart, null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32), focusedLabelColor = Color(0xFF2E7D32))
                )

                DropdownField(value = formState.diagnosis, onValueChange = viewModel::onDiagnosisChange, label = "Diagnosis *", options = DataOptions.diagnosisSapi, leadingIcon = Icons.Default.MedicalInformation)
                DropdownField(value = formState.tindakan, onValueChange = viewModel::onTindakanChange, label = "Tindakan *", options = DataOptions.tindakanSapi, leadingIcon = Icons.Default.MedicalServices)
                DropdownField(value = formState.obat, onValueChange = viewModel::onObatChange, label = "Obat yang Diberikan", options = DataOptions.obatSapi, leadingIcon = Icons.Default.Medication)

                OutlinedTextField(
                    value = formState.catatan,
                    onValueChange = viewModel::onCatatanChange,
                    label = { Text("Catatan Tambahan") },
                    leadingIcon = { Icon(Icons.Default.Notes, null) },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32), focusedLabelColor = Color(0xFF2E7D32))
                )

                Text("Update Status Sapi", fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFF424242))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Sehat", "Dalam Perawatan").forEach { status ->
                        val isSelected = formState.statusSapi == status
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onStatusSapiChange(status) },
                            label = { Text(status, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (status == "Sehat") Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                selectedLabelColor = if (status == "Sehat") Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) { Text("Batal") }
                    Button(
                        onClick = { viewModel.saveRiwayat(sapiId) },
                        modifier = Modifier.weight(1f),
                        enabled = !formState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (formState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }
}
