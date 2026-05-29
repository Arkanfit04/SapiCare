package com.sapicare.app.ui.pengurus.form

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
import com.sapicare.app.data.model.UserSession
import com.sapicare.app.ui.components.DatePickerField
import com.sapicare.app.ui.components.DropdownField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SapiFormScreen(
    sapiId: String?,
    currentSession: UserSession?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: SapiFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isEdit = sapiId != null

    LaunchedEffect(sapiId) {
        if (sapiId != null) viewModel.loadSapi(sapiId)
        else viewModel.initNew(currentSession)
    }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onSaved() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEdit) "Edit Data Sapi" else "Tambah Sapi",
                        fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32), titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (uiState.isLoading && isEdit) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2E7D32))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.error != null) {
                Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(uiState.error ?: "", modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }

            FormCard {
                SectionHeader("Identitas Sapi")
                FormTextField(value = uiState.nama, onValueChange = viewModel::onNamaChange,
                    label = "Nama Sapi *", icon = Icons.Default.Badge)
                DropdownField(value = uiState.jenisSapi, onValueChange = viewModel::onJenisSapiChange,
                    label = "Jenis Sapi *",
                    options = com.sapicare.app.util.DataOptions.jenisSapi,
                    leadingIcon = Icons.Default.Category)
                DropdownField(value = uiState.jenisKelamin, onValueChange = viewModel::onJenisKelaminChange,
                    label = "Jenis Kelamin *",
                    options = com.sapicare.app.util.DataOptions.jenisKelamin,
                    leadingIcon = Icons.Default.Transgender)
                DatePickerField(value = uiState.tanggalLahir, onValueChange = viewModel::onTanggalLahirChange,
                    label = "Tanggal Lahir")
            }

            FormCard {
                SectionHeader("Kepemilikan & Lokasi")
                FormTextField(value = uiState.namaPemilik, onValueChange = viewModel::onNamaPemilikChange,
                    label = "Nama Pemilik *", icon = Icons.Default.Person)
                DropdownField(value = uiState.wilayah, onValueChange = viewModel::onWilayahChange,
                    label = "Wilayah *",
                    options = com.sapicare.app.util.DataOptions.wilayahCimahi,
                    leadingIcon = Icons.Default.LocationOn)
            }

            FormCard {
                SectionHeader("Berat Badan")
                FormTextField(
                    value = if (uiState.beratBadan == 0.0) "" else uiState.beratBadan.toString(),
                    onValueChange = { v -> viewModel.onBeratBadanChange(v.toDoubleOrNull() ?: 0.0) },
                    label = "Berat Badan (kg)",
                    icon = Icons.Default.Scale
                )
            }

            FormCard {
                SectionHeader("Status & Keterangan")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Sehat", "Dalam Perawatan").forEach { status ->
                        val isSelected = uiState.status == status
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onStatusChange(status) },
                            label = { Text(status, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = if (status == "Sehat") Color(0xFFE8F5E9) else Color(0xFFFFF3E0),
                                selectedLabelColor = if (status == "Sehat") Color(0xFF2E7D32) else Color(0xFFE65100)
                            )
                        )
                    }
                }
                FormTextField(value = uiState.keterangan, onValueChange = viewModel::onKeteranganChange,
                    label = "Keterangan", icon = Icons.Default.Notes, maxLines = 3)
            }

            FormCard {
                SectionHeader("Jenis Perawatan")
                com.sapicare.app.util.DataOptions.jenisPerawatan.forEach { p ->
                    val isSelected = p in uiState.jenisPerawatan
                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isSelected,
                            onCheckedChange = { viewModel.togglePerawatan(p) },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2E7D32)))
                        Text(p, fontSize = 14.sp, color = Color(0xFF212121))
                    }
                }
            }

            Button(
                onClick = { viewModel.saveSapi() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(if (isEdit) "Simpan Perubahan" else "Tambah Sapi",
                        fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FormCard(content: @Composable ColumnScope.() -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF2E7D32))
}

@Composable
private fun FormTextField(value: String, onValueChange: (String) -> Unit, label: String,
                          icon: androidx.compose.ui.graphics.vector.ImageVector, maxLines: Int = 1) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null) },
        modifier = Modifier.fillMaxWidth(),
        maxLines = maxLines,
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF2E7D32), focusedLabelColor = Color(0xFF2E7D32)
        )
    )
}
