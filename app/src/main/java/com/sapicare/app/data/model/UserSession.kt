package com.sapicare.app.data.model

enum class UserRole {
    PENGURUS, PETERNAK, DINAS
}

enum class ApprovalStatus {
    PENDING,    // Baru daftar, belum di-approve
    APPROVED,   // Sudah di-approve Dinas
    REJECTED    // Ditolak Dinas
}

data class UserSession(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val role: UserRole = UserRole.PENGURUS,
    val approvalStatus: ApprovalStatus = ApprovalStatus.APPROVED
)
