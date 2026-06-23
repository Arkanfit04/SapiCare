package com.sapicare.app.ui.peternak

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.sapicare.app.data.model.UserSession
import com.sapicare.app.ui.components.SapiCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SapiSayaScreen(
    session: UserSession?,
    savedAccountsCount: Int,
    unreadCount: Int,
    onNotificationClick: () -> Unit,
    onTambahSapi: () -> Unit,
    onDetailSapi: (String) -> Unit,
    onLogout: () -> Unit,
    onSwitchAccount: () -> Unit,
    viewModel: SapiSayaViewModel = hiltViewModel()
) {
    val sapiSaya by viewModel.sapiSaya.collectAsState()
    val sapiWilayah by viewModel.sapiWilayah.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val showSapiSaya by viewModel.showSapiSaya.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    LaunchedEffect(session) { viewModel.init(session) }

    val displayList = if (showSapiSaya) {
        if (searchQuery.isBlank()) sapiSaya
        else sapiSaya.filter {
            it.nama.contains(searchQuery, true) || it.wilayah.contains(searchQuery, true)
        }
    } else {
        if (searchQuery.isBlank()) sapiWilayah
        else sapiWilayah.filter {
            it.nama.contains(searchQuery, true) || it.wilayah.contains(searchQuery, true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text("Sapi Saya", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        if (session?.username?.isNotEmpty() == true) {
                            Text("Halo, ${session.username} 👋", fontSize = 12.sp, color = Color.White.copy(0.85f))
                        }
                    }
                },
                windowInsets = WindowInsets(0),
                actions = {

                    BadgedBox(
                        badge = {
                            if (unreadCount > 0) {
                                Badge(
                                    modifier = Modifier.offset(
                                        x = (-6).dp,
                                        y = 6.dp
                                    )
                                )
                            }
                        }
                    ) {
                        IconButton(
                            onClick = onNotificationClick
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            BadgedBox(badge = {
                                if (savedAccountsCount > 1) Badge { Text(savedAccountsCount.toString()) }
                            }) {
                                Icon(Icons.Default.ManageAccounts, null, tint = Color.White)
                            }
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("Profile") },
                                onClick = { showMenu = false; onSwitchAccount() },
                                leadingIcon = { Icon(Icons.Default.SwitchAccount, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Keluar", color = MaterialTheme.colorScheme.error) },
                                onClick = { showMenu = false; onLogout() },
                                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF795548), titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onTambahSapi,
                containerColor = Color(0xFF795548), contentColor = Color.White) {
                Icon(Icons.Default.Add, "Tambah Sapi")
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = searchQuery, onValueChange = viewModel::onSearchChange,
                placeholder = { Text("Cari nama sapi atau wilayah...") },
                leadingIcon = {
                    Icon(Icons.Default.Search,
                        null,
                        tint = Color.Gray) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchChange("") }) {
                            Icon(
                                Icons.Default.Clear,
                                null,
                                tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                singleLine = true, shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,

                    focusedPlaceholderColor = Color.Gray,
                    unfocusedPlaceholderColor = Color.Gray,

                    focusedLeadingIconColor = Color(0xFF795548),
                    unfocusedLeadingIconColor = Color.Gray,

                    focusedTrailingIconColor = Color(0xFF795548),
                    unfocusedTrailingIconColor = Color.Gray,

                    focusedBorderColor = Color(0xFF795548),
                    unfocusedBorderColor = Color(0xFFE0E0E0),

                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            // Filter toggle: Sapi Saya / Semua di Wilayah
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = showSapiSaya,
                    onClick = { viewModel.setShowSapiSaya(true) },
                    label = { Text("Sapi Saya (${sapiSaya.size})", fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF795548),
                        selectedLabelColor = Color.White,

                        containerColor = Color.White,
                        labelColor = Color.Black
                    )
                )
                FilterChip(
                    selected = !showSapiSaya,
                    onClick = { viewModel.setShowSapiSaya(false) },
                    label = {
                        Text("Semua Wilayah (${sapiWilayah.size})",
                            fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF795548),
                        selectedLabelColor = Color.White,

                        containerColor = Color.White,
                        labelColor = Color.Black
                    )
                )
            }

            Spacer(Modifier.height(8.dp))

            if (displayList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🐄", fontSize = 56.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (showSapiSaya) "Belum ada sapi milik kamu"
                            else "Belum ada sapi di wilayah ini",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color(0xFF424242)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("Ketuk tombol + untuk menambahkan",
                            color = Color.Gray, fontSize = 13.sp)
                    }
                }
            } else {

                LaunchedEffect(displayList) {
                    Log.d(
                        "SAPI_UI",
                        "showSapiSaya=$showSapiSaya, total=${displayList.size}"
                    )

                    displayList.forEach {
                        Log.d(
                            "SAPI_UI",
                            "${it.nama} | ${it.wilayah} | ${it.ownerId}"
                        )
                    }
                }

                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("${displayList.size} sapi",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(bottom = 4.dp))
                    }
                    items(displayList, key = { it.id }) { sapi ->
                        SapiCard(sapi = sapi, onClick = { onDetailSapi(sapi.id) })
                    }
                }
            }
        }
    }
}
