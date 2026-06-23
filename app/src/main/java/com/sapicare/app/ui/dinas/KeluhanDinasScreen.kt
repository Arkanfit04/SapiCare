package com.sapicare.app.ui.dinas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.Keluhan
import com.sapicare.app.data.model.StatusKeluhan
import com.sapicare.app.data.repository.KeluhanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class KeluhanDinasViewModel @Inject constructor(
    keluhanRepository: KeluhanRepository
) : androidx.lifecycle.ViewModel() {
    val keluhanList: StateFlow<List<Keluhan>> = keluhanRepository.getAllKeluhan()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KeluhanDinasScreen(viewModel: KeluhanDinasViewModel = hiltViewModel()) {
    val keluhanList by viewModel.keluhanList.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Keluhan Peternak", fontWeight = FontWeight.Bold) },
                windowInsets = WindowInsets(0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1565C0), titleContentColor = Color.White))
        }, containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        if (keluhanList.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding).padding(32.dp), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = 56.sp); Spacer(Modifier.height(12.dp))
                    Text("Belum ada keluhan", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color(0xFF424242))
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item { Text("${keluhanList.size} keluhan", fontSize = 12.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 4.dp)) }
                items(keluhanList, key = { it.id }) { keluhan -> KeluhanDinasCard(keluhan) }
            }
        }
    }
}

@Composable
fun KeluhanDinasCard(keluhan: Keluhan) {
    val statusColor = when (keluhan.status) {
        StatusKeluhan.MENUNGGU -> Color(0xFFFF9800)
        StatusKeluhan.DIJADWALKAN -> Color(0xFF1565C0)
        StatusKeluhan.SELESAI -> Color(0xFF2E7D32)
    }
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.White), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    keluhan.namaSapi,
                    color = Color.Black,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = statusColor.copy(0.15f)
                ) {
                    Text(
                        keluhan.status.name.lowercase()
                            .replaceFirstChar { it.uppercase() },
                        modifier = Modifier.padding(horizontal = 10.dp,
                            vertical = 3.dp),
                        fontSize = 11.sp,
                        color = statusColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Text(
                "${keluhan.peternak} • ${keluhan.wilayah}",
                fontSize = 12.sp,
                color = Color.Gray
            )
            Text(
                keluhan.deskripsiKeluhan,
                fontSize = 13.sp,
                color = Color(0xFF424242),
                maxLines = 2
            )
        }
    }
}
