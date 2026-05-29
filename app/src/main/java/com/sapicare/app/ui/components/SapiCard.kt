package com.sapicare.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.sapicare.app.data.model.Sapi

@Composable
fun SapiCard(sapi: Sapi, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center) {
                if (sapi.fotoUrl.isNotEmpty()) {
                    AsyncImage(model = sapi.fotoUrl, contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFE8F5E9),
                        shape = RoundedCornerShape(10.dp)) {
                        Box(contentAlignment = Alignment.Center) { Text("🐄", fontSize = 28.sp) }
                    }
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(sapi.nama, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF212121))
                Text("${sapi.jenisSapi} • ${sapi.jenisKelamin}", fontSize = 12.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Text(sapi.wilayah, fontSize = 11.sp, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(shape = RoundedCornerShape(6.dp),
                    color = if (sapi.status == "Sehat") Color(0xFFE8F5E9) else Color(0xFFFFF3E0)) {
                    Text(sapi.status,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        color = if (sapi.status == "Sehat") Color(0xFF2E7D32) else Color(0xFFE65100),
                        fontWeight = FontWeight.Medium)
                }
                if (sapi.beratBadan > 0) {
                    Text("${sapi.beratBadan.toInt()} kg", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}
