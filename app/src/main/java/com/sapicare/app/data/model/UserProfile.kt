package com.sapicare.app.data.model

data class UserProfile(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val role: String = "",              // "PENGURUS" / "PETERNAK" / "DINAS"
    val approvalStatus: String = "APPROVED", // "PENDING" / "APPROVED" / "REJECTED"
    val linkedTo: String = "",          // Firebase Auth UID asli
    val isDinas: Boolean = false,       // flag khusus akun Dinas (dibuat dev)
    val createdAt: Long = System.currentTimeMillis(),
    val approvedAt: Long = 0L,
    val approvedBy: String = "",         // uid Dinas yang approve
    val rejectedAt: Long = 0L,
    val rejectedReason: String = "",
    val fcmToken: String = ""
)
