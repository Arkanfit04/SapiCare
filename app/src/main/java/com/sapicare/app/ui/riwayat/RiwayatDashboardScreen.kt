package com.sapicare.app.ui.riwayat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.sapicare.app.data.model.RiwayatKunjungan
import com.sapicare.app.data.model.Sapi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatDashboardScreen(
    canAddKunjungan: Boolean,
    onDetailSapi: (String) -> Unit,
    onRiwayatSapi: (String, String) -> Unit,
    viewModel: RiwayatDashboardViewModel = hiltViewModel()
) {
    val items by viewModel.riwayatWithSapi.collectAsState()
    val sapiList by viewModel.sapiList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showTambahDialog by remember { mutableStateOf(false) }

    if (showTambahDialog) {
        PilihSapiDialog(
            sapiList = sapiList,
            onSapiSelected = { sapi ->
                showTambahDialog = false
                onRiwayatSapi(sapi.id, sapi.nama)
            },
            onDismiss = { showTambahDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Riwayat Kunjungan", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (canAddKunjungan) {
                ExtendedFloatingActionButton(
                    onClick = { showTambahDialog = true },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Tambah Kunjungan") },
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White
                )
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchChange,
                placeholder = { Text("Cari nama sapi atau diagnosis...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchChange("") }) {
                            Icon(Icons.Default.Clear, null, tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,

                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,

                    focusedLeadingIconColor = Color.Gray,
                    unfocusedLeadingIconColor = Color.Gray,

                    focusedTrailingIconColor = Color.Gray,
                    unfocusedTrailingIconColor = Color.Gray,

                    focusedBorderColor = Color(0xFF2E7D32),
                    unfocusedBorderColor = Color.LightGray,

                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,

                    cursorColor = Color(0xFF2E7D32)
                )
            )

            if (items.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📋", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (searchQuery.isEmpty()) "Belum ada riwayat kunjungan" else "Tidak ditemukan",
                            fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF424242)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (searchQuery.isEmpty() && canAddKunjungan) "Ketuk tombol + untuk tambah kunjungan"
                            else "Coba kata kunci lain",
                            color = Color.Gray, fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("${items.size} kunjungan", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    items(items, key = { it.first.id }) { (riwayat, sapi) ->
                        RiwayatWithSapiCard(
                            riwayat = riwayat,
                            sapi = sapi,
                            onClick = { onRiwayatSapi(sapi.id, sapi.nama) },
                            onSapiClick = { onDetailSapi(sapi.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PilihSapiDialog(sapiList: List<Sapi>, onSapiSelected: (Sapi) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, modifier = Modifier.fillMaxWidth()) {
        Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Pilih Sapi", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1B5E20))
                Text("Pilih sapi untuk ditambahkan riwayat kunjungannya", fontSize = 13.sp, color = Color.Gray)

                if (sapiList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        Text("Belum ada data sapi", color = Color.Gray)
                    }
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sapiList, key = { it.id }) { sapi ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onSapiSelected(sapi) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                        if (sapi.fotoUrl.isNotEmpty()) {
                                            AsyncImage(model = sapi.fotoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                                        } else {
                                            Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                                                Box(contentAlignment = Alignment.Center) { Text("🐄", fontSize = 20.sp) }
                                            }
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(sapi.nama, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        Text("${sapi.jenisSapi} • ${sapi.wilayah}", fontSize = 12.sp, color = Color.Gray, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    Surface(shape = RoundedCornerShape(6.dp), color = if (sapi.status == "Sehat") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)) {
                                        Text(sapi.status, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 10.sp, color = if (sapi.status == "Sehat") Color(0xFF2E7D32) else Color(0xFFE65100))
                                    }
                                }
                            }
                        }
                    }
                }
                OutlinedButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp)) { Text("Batal") }
            }
        }
    }
}

@Composable
fun RiwayatWithSapiCard(
    riwayat: RiwayatKunjungan,
    sapi: Sapi, onClick: () -> Unit,
    onSapiClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clickable { onSapiClick() }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    if (sapi.fotoUrl.isNotEmpty()) {
                        AsyncImage(model = sapi.fotoUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                    } else {
                        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)) {
                            Box(contentAlignment = Alignment.Center) { Text("🐄", fontSize = 20.sp) }
                        }
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        sapi.nama,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        "${sapi.jenisSapi} • ${sapi.wilayah}",
                        fontSize = 11.sp, color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                }
                Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFE8F5E9)) {
                    Text(riwayat.tanggal, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Medium)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF0F0F0))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                KunjunganInfo(Modifier.weight(1f), "Diagnosis", riwayat.diagnosis, Color(0xFF1565C0))
                KunjunganInfo(Modifier.weight(1f), "Tindakan", riwayat.tindakan, Color(0xFF2E7D32))
            }
            Spacer(Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Text(riwayat.namaPetugas, fontSize = 11.sp, color = Color.Gray)
                }
                if (riwayat.obat.isNotEmpty() && riwayat.obat != "Tidak Ada") {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Medication, null, tint = Color(0xFFE65100), modifier = Modifier.size(12.dp))
                        Text(riwayat.obat, fontSize = 11.sp, color = Color(0xFFE65100), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
fun KunjunganInfo(modifier: Modifier = Modifier, label: String, value: String, color: Color) {
    Column(modifier = modifier) {
        Text(label, fontSize = 10.sp, color = Color.Gray)
        Spacer(Modifier.height(2.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}
