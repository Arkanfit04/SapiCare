package com.sapicare.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sapicare.app.data.model.UserRole

data class RoleItem(
    val role: UserRole,
    val icon: String,
    val title: String,
    val desc: String
)
@Composable
fun RolePickerScreen(
    username: String,
    isLoading: Boolean,
    error: String?,
    onRoleSelected: (UserRole) -> Unit,
    onBack: () -> Unit

) {
    var selectedRole by remember { mutableStateOf<UserRole?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF388E3C))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color.White
                    )
                }
            }
            Spacer(Modifier.height(48.dp))
            Text("🐄", fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Halo, $username!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                "Pilih peran kamu di SapiCare",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(Modifier.height(32.dp))

            // Card pilih role
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Pilih Role",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1B5E20)
                    )

                    // Error
                    if (error != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                error,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Role options
                    listOf(
                        RoleItem(
                            UserRole.PENGURUS,
                            "👨‍⚕️",
                            "Pengurus / Dokter Hewan",
                            "Dapat menambah data sapi dan mencatat riwayat kunjungan"
                        ),

                        RoleItem(
                            UserRole.PETERNAK,
                            "👨‍🌾",
                            "Peternak",
                            "Dapat menambah dan mengelola data sapi"
                        )
                    ).forEach { (role, icon, title, desc) ->
                        RoleOption(
                            icon = icon,
                            title = title,
                            description = desc,
                            isSelected = selectedRole == role,
                            onClick = { selectedRole = role }
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick = { selectedRole?.let { onRoleSelected(it) } },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = selectedRole != null && !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Mulai Gunakan SapiCare",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                    }
                }
            }
        }
    }
}

@Composable
fun RoleOption(
    icon: String,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF2E7D32) else Color(0xFFE0E0E0)
    val bgColor = if (isSelected) Color(0xFFE8F5E9) else Color.White

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(icon, fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (isSelected) Color(0xFF1B5E20) else Color(0xFF212121)
                )
                Text(
                    description,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
            }
            if (isSelected) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
