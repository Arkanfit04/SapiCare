package com.sapicare.app.ui.pengurus.list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
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
import com.sapicare.app.ui.components.SapiCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SapiDashboardScreen(
    canAddSapi: Boolean,
    username: String,
    savedAccountsCount: Int,
    onTambahSapi: () -> Unit,
    onDetailSapi: (String) -> Unit,
    onLogout: () -> Unit,
    onSwitchAccount: () -> Unit,
    viewModel: SapiViewModel = hiltViewModel()
) {
    val sapiList by viewModel.sapiList.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text("Data Sapi", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        if (username.isNotEmpty()) {
                            Text(
                                "Halo, $username 👋",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                },
                windowInsets = WindowInsets(0),
                actions = {
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            BadgedBox(
                                badge = {
                                    if (savedAccountsCount > 1) {
                                        Badge { Text(savedAccountsCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ManageAccounts, null, tint = Color.White)
                            }
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Kelola Akun") },
                                onClick = { showMenu = false; onSwitchAccount() },
                                leadingIcon = { Icon(Icons.Default.SwitchAccount, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Keluar", color = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; onLogout() },
                                leadingIcon = {
                                    Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error)
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2E7D32),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            if (canAddSapi) {
                FloatingActionButton(
                    onClick = onTambahSapi,
                    containerColor = Color(0xFF2E7D32),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, "Tambah Sapi")
                }
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Stats
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(Modifier.weight(1f), "🐄", "Total", sapiList.size.toString(), Color(0xFF2E7D32))
                StatCard(Modifier.weight(1f), "🏥", "Perawatan", sapiList.count { it.status == "Dalam Perawatan" }.toString(), Color(0xFF1565C0))
                StatCard(Modifier.weight(1f), "✅", "Sehat", sapiList.count { it.status == "Sehat" }.toString(), Color(0xFF2E7D32))
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Cari nama sapi atau wilayah...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, null, tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2E7D32),
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(Modifier.height(8.dp))

            if (sapiList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🐄", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (searchQuery.isEmpty()) "Belum ada data sapi" else "Sapi tidak ditemukan",
                            fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF424242)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (searchQuery.isEmpty() && canAddSapi) "Ketuk tombol + untuk menambahkan"
                            else if (searchQuery.isNotEmpty()) "Coba kata kunci lain"
                            else "Data akan muncul setelah pengurus menambahkan",
                            color = Color.Gray, fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("${sapiList.size} sapi", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp))
                    }
                    items(sapiList, key = { it.id }) { sapi ->
                        SapiCard(sapi = sapi, onClick = { onDetailSapi(sapi.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, icon: String, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
            Text(label, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
