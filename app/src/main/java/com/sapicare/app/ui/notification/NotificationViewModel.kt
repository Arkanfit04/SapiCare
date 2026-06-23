package com.sapicare.app.ui.notification

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sapicare.app.data.model.NotificationItem
import com.sapicare.app.data.repository.AuthRepository
import com.sapicare.app.data.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val notifications: StateFlow<List<NotificationItem>> =
        authRepository.sessionFlow
            .flatMapLatest { session ->

                Log.d(
                    "NOTIF_UID",
                    session?.uid ?: "NULL"
                )

                if (session == null) {
                    flowOf(emptyList())
                } else {
                    notificationRepository.getNotifications(session.uid)
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                emptyList()
            )

    val unreadCount: StateFlow<Int> =
        notifications
            .map { list ->
                list.count { !it.read }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                0
            )

    fun markAsRead(id: String) {
        viewModelScope.launch {
            try {
                notificationRepository.markAsRead(id)
                Log.d("NOTIF", "Berhasil update $id")
            } catch (e: Exception) {
                Log.e("NOTIF", "Gagal update", e)
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notifications.value
                .filter { !it.read }
                .forEach {
                    notificationRepository.markAsRead(it.id)
                }
        }
    }
}