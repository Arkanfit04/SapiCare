package com.sapicare.app.data.model

data class SavedAccount(
    val uid: String = "",
    val email: String = "",
    val username: String = "",
    val role: UserRole = UserRole.PENGURUS,
    val photoUrl: String = ""
)
