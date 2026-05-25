package com.sapicare.app.data.model

enum class UserRole {
    PENGURUS, PETERNAK, DINAS
}

data class UserSession(
    val uid: String = "",
    val username: String = "",
    val email: String = "",
    val role: UserRole = UserRole.PENGURUS
)
