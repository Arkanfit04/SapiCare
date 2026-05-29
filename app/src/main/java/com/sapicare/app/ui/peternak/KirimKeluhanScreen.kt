package com.sapicare.app.ui.peternak

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sapicare.app.data.model.UserSession
import com.sapicare.app.ui.components.DatePickerField
import com.sapicare.app.ui.components.DropdownField
import com.sapicare.app.util.DataOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KirimKeluhanScreen(
    session: UserSession?,
    viewModel: KirimKeluhanViewModel = hiltViewModel()
) {
    val sapiList by viewModel.sapiSaya.collectAsState()
    val formState by viewModel.formState.collectAsState()
    var showForm by remember { mutableStateOf(false) }

    LaunchedEffect(session) { viewModel.init(session) }
    LaunchedEffect(formState.isSent) { if (formState.isSent) { showForm = false; viewModel.resetForm() } }

    if (showForm) {
        KirimKeluhanDialog(
            sapiList = sapiList.map { it.nama to it.id },
            formState = formState,
            onSapiSelected = viewModel::onSapiSelected,
            onDeskripsiChange = viewModel::onDeskripsiChange,
            onToggleGejala = viewModel::toggleGejala,
            onTanggalChange = viewModel::onTanggalChange,
            onUsulanTanggalChange = viewModel::onUsulanTanggalChange,
            onSend = { viewModel.kirimKeluhan(session) },
            onDismiss = { showForm = false; viewModel.resetForm() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kirim Keluhan", fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF795548), titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showForm = true },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Kirim Keluhan Baru") },
                containerColor = Color(0xFF795548), contentColor = Color.White
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📋", fontSize = 56.sp)
                Spacer(Modifier.height(12.dp))
                Text("Kirim keluhan kondisi sapi kamu", fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp, color = Color(0xFF424242))
                Spacer(Modifier.height(4.dp))
                Text("Pengurus / Dokter akan menindaklanjuti", color = Color.Gray, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KirimKeluhanDialog(
    sapiList: List<Pair<String, String>>,
    formState: KirimKeluhanFormState,
    onSapiSelected: (String, String) -> Unit,
    onDeskripsiChange: (String) -> Unit,
    onToggleGejala: (String) -> Unit,
    onTanggalChange: (String) -> Unit,
    onUsulanTanggalChange: (String) -> Unit,
    onSend: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(onDismissRequest = onDismiss, modifier = Modifier.fillMaxWidth()) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text("Kirim Keluhan Sapi", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF4E342E))

                AnimatedVisibility(visible = formState.error != null) {
                    Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.errorContainer) {
                        Text(formState.error ?: "", modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                    }
                }

                // Pilih sapi
                DropdownField(
                    value = formState.namaSapi, onValueChange = { nama ->
                        val id = sapiList.find { it.first == nama }?.second ?: ""
                        onSapiSelected(id, nama)
                    },
                    label = "Pilih Sapi *",
                    options = sapiList.map { it.first },
                    leadingIcon = Icons.Default.Pets
                )

                DatePickerField(value = formState.tanggalKeluhan, onValueChange = onTanggalChange,
                    label = "Tanggal Keluhan")

                OutlinedTextField(
                    value = formState.deskripsiKeluhan, onValueChange = onDeskripsiChange,
                    label = { Text("Deskripsi Keluhan *") },
                    leadingIcon = { Icon(Icons.Default.Description, null) },
                    modifier = Modifier.fillMaxWidth(), maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF795548), focusedLabelColor = Color(0xFF795548)
                    )
                )

                Text("Gejala yang Terlihat", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                DataOptions.gejalaSapi.forEach { gejala ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = gejala in formState.gejala,
                            onCheckedChange = { onToggleGejala(gejala) },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF795548)))
                        Text(gejala, fontSize = 13.sp)
                    }
                }

                DatePickerField(value = formState.usulanTanggalKunjungan, onValueChange = onUsulanTanggalChange,
                    label = "Usul Tanggal Kunjungan (opsional)")

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)) { Text("Batal") }
                    Button(onClick = onSend, modifier = Modifier.weight(1f),
                        enabled = !formState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF795548)),
                        shape = RoundedCornerShape(10.dp)) {
                        if (formState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp),
                            color = Color.White, strokeWidth = 2.dp)
                        else Text("Kirim")
                    }
                }
            }
        }
    }
}


