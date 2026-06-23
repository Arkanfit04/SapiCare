package com.sapicare.app.data.model

data class NotificationItem(
    val id: String = "",
    val targetUid: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val referenceId: String = "",
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)