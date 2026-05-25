package com.sapicare.app.ui.pengurus.form

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sapicare.app.ui.components.DatePickerField
import com.sapicare.app.ui.components.DropdownField
import com.sapicare.app.ui.components.SectionTitle
import com.sapicare.app.util.DataOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SapiFormScreen(
    sapiId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: SapiFormViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isEdit = sapiId != null

    LaunchedEffect(sapiId) { if (sapiId != null) viewModel.loadSapi(sapiId) }
    LaunchedEffect(state.isSaved) { if (state.isSaved) onSaved() }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.onFotoSelected(it) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) "Edit Data Sapi" else "Tambah Sapi Baru",
                        fontWeight = FontWeight.Bold
                    )
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
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->

        if (state.isLoading && isEdit && state.nama.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF2E7D32))
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Foto ──────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle("Foto Sapi (Opsional)")
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(12.dp))
                            .clickable { imagePickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            state.fotoUriLocal != null -> {
                                AsyncImage(model = state.fotoUriLocal, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                                    IconButton(onClick = { viewModel.onFotoRemoved() }, modifier = Modifier.padding(6.dp)) {
                                        Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(0.5f)) {
                                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                                        }
                                    }
                                }
                            }
                            state.fotoUrl.isNotEmpty() -> {
                                AsyncImage(model = state.fotoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopEnd) {
                                    IconButton(onClick = { viewModel.onFotoRemoved() }, modifier = Modifier.padding(6.dp)) {
                                        Surface(shape = RoundedCornerShape(50), color = Color.Black.copy(0.5f)) {
                                            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(4.dp))
                                        }
                                    }
                                }
                            }
                            else -> {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.AddPhotoAlternate, null, modifier = Modifier.size(40.dp), tint = Color.LightGray)
                                    Spacer(Modifier.height(6.dp))
                                    Text("Ketuk untuk pilih foto", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }
                        if (state.isUploadingFoto) {
                            Surface(modifier = Modifier.fillMaxSize(), color = Color.Black.copy(0.5f)) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // ── Informasi Dasar ───────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionTitle("Informasi Dasar")

                    OutlinedTextField(
                        value = state.nama,
                        onValueChange = viewModel::onNamaChange,
                        label = { Text("Nama / ID Sapi *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32), focusedLabelColor = Color(0xFF2E7D32))
                    )

                    DropdownField(value = state.jenisSapi, onValueChange = viewModel::onJenisSapiChange, label = "Jenis Sapi *", options = DataOptions.jenisSapi, leadingIcon = Icons.Default.Category)
                    DropdownField(value = state.jenisKelamin, onValueChange = viewModel::onJenisKelaminChange, label = "Jenis Kelamin *", options = DataOptions.jenisKelamin, leadingIcon = Icons.Default.Transgender)
                    DatePickerField(value = state.tanggalLahir, onValueChange = viewModel::onTanggalLahirChange, label = "Tanggal Lahir *")

                    if (state.umur.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Cake, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                            Text("Umur: ", color = Color.Gray, fontSize = 13.sp)
                            Text(state.umur, fontWeight = FontWeight.SemiBold, color = Color(0xFF2E7D32), fontSize = 13.sp)
                        }
                    }
                }
            }

            // ── Pemilik & Lokasi ──────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SectionTitle("Pemilik & Lokasi")

                    OutlinedTextField(
                        value = state.namaPemilik,
                        onValueChange = viewModel::onNamaPemilikChange,
                        label = { Text("Nama Pemilik *") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32), focusedLabelColor = Color(0xFF2E7D32))
                    )

                    DropdownField(value = state.wilayah, onValueChange = viewModel::onWilayahChange, label = "Wilayah *", options = DataOptions.wilayahCimahi, leadingIcon = Icons.Default.LocationOn)

                    OutlinedTextField(
                        value = state.keterangan,
                        onValueChange = viewModel::onKeteranganChange,
                        label = { Text("Keterangan") },
                        leadingIcon = { Icon(Icons.Default.Notes, null) },
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32), focusedLabelColor = Color(0xFF2E7D32))
                    )
                }
            }

            // ── Jenis Perawatan ───────────────────────────────────
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle("Jenis Perawatan")

                    var selectedPerawatan by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            DropdownField(
                                value = selectedPerawatan,
                                onValueChange = { selectedPerawatan = it },
                                label = "Pilih Perawatan",
                                options = DataOptions.jenisPerawatan.filter { it !in state.jenisPerawatan }
                            )
                        }
                        Button(
                            onClick = {
                                if (selectedPerawatan.isNotEmpty()) {
                                    viewModel.addPerawatan(selectedPerawatan)
                                    selectedPerawatan = ""
                                }
                            },
                            enabled = selectedPerawatan.isNotEmpty(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                        ) { Icon(Icons.Default.Add, null) }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = state.newPerawatan,
                            onValueChange = viewModel::onNewPerawatanChange,
                            label = { Text("Perawatan lainnya...") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF2E7D32), focusedLabelColor = Color(0xFF2E7D32))
                        )
                        OutlinedButton(
                            onClick = viewModel::addPerawatanCustom,
                            enabled = state.newPerawatan.isNotBlank(),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp)
                        ) { Icon(Icons.Default.Add, null, tint = Color(0xFF2E7D32)) }
                    }

                    if (state.jenisPerawatan.isEmpty()) {
                        Text("Belum ada jenis perawatan", color = Color.Gray, fontSize = 13.sp)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            state.jenisPerawatan.forEach { perawatan ->
                                PerawatanChip(label = perawatan, onRemove = { viewModel.removePerawatan(perawatan) })
                            }
                        }
                    }
                }
            }

            // ── Error + Tombol Simpan ─────────────────────────────
            AnimatedVisibility(visible = state.error != null) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(state.error ?: "", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }
            }

            Button(
                onClick = viewModel::saveSapi,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !state.isLoading && !state.isUploadingFoto,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading || state.isUploadingFoto) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isEdit) "Simpan Perubahan" else "Simpan Data Sapi",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun PerawatanChip(label: String, onRemove: () -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFE8F5E9)) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(Icons.Default.MedicalServices, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
            Text(label, color = Color(0xFF1B5E20), fontWeight = FontWeight.Medium, fontSize = 13.sp)
            IconButton(onClick = onRemove, modifier = Modifier.size(18.dp)) {
                Icon(Icons.Default.Close, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
            }
        }
    }
}
