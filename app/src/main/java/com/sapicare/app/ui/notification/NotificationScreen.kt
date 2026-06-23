package com.sapicare.app.ui.notification

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sapicare.app.data.model.NotificationItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    onBack: () -> Unit,
    viewModel: NotificationViewModel = hiltViewModel()
) {
    val notifications by viewModel.notifications.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.markAllAsRead()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Notifikasi",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = Color.White
                        )
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

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔔", fontSize = 56.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Belum ada notifikasi",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    notifications,
                    key = { it.id }
                ) { notification ->

                    NotificationCard(
                        notification = notification,
                        onClick = {
                            if (!notification.read) {
                                viewModel.markAsRead(notification.id)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationItem,
    onClick: () -> Unit
) {

    // Tentukan ikon & warna berdasarkan tipe notif
    val (icon, iconTint) = when (notification.type) {
        "KELUHAN"          -> Icons.Default.Notifications to Color(0xFF2E7D32)
        "KUNJUNGAN"        -> Icons.Default.CalendarMonth  to Color(0xFF1565C0)
        "APPROVAL_REQUEST" -> Icons.Default.PersonAdd       to Color(0xFFFF9800)
        else               -> Icons.Default.Notifications  to Color(0xFF2E7D32)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                if (!notification.read)
                    Color(0xFFE8F5E9)
                else
                    Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                tint = Color(0xFF2E7D32)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = notification.title,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.heightIn(2.dp))

                Text(
                    text = formatNotificationTime(notification.createdAt),
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

private fun formatNotificationTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 ->
            "Baru saja"

        diff < 3_600_000 ->
            "${diff / 60_000} menit lalu"

        diff < 86_400_000 ->
            "${diff / 3_600_000} jam lalu"

        diff < 7 * 86_400_000 ->
            "${diff / 86_400_000} hari lalu"

        else ->
            SimpleDateFormat(
                "dd MMM yyyy",
                Locale("id", "ID")
            ).format(Date(timestamp))
    }
}