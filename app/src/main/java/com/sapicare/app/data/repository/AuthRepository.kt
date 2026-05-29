package com.sapicare.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.sapicare.app.data.local.AccountManager
import com.sapicare.app.data.model.SavedAccount
import com.sapicare.app.data.model.UserRole
import com.sapicare.app.data.model.UserSession
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val accountManager: AccountManager
) {
    private val usersCollection = firestore.collection("users")

    val savedAccountsFlow = accountManager.savedAccountsFlow
    val activeUidFlow = accountManager.activeUidFlow

    // Session aktif dibaca dari AccountManager — bukan Firebase Auth
    val sessionFlow: Flow<UserSession?> = combine(
        accountManager.savedAccountsFlow,
        accountManager.activeUidFlow
    ) { accounts, activeUid ->
        if (activeUid.isNullOrEmpty()) return@combine null
        val account = accounts.find { it.uid == activeUid } ?: return@combine null
        UserSession(
            uid = account.uid,
            username = account.username,
            email = account.email,
            role = account.role
        )
    }

    /**
     * Login Google — FIX: gunakan flow.first() bukan GlobalScope + delay
     * Return: Pair(username, hasAccounts)
     */
    suspend fun loginWithGoogle(idToken: String): Result<Pair<String, Boolean>> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(Exception("Login gagal"))

            val username = user.displayName ?: user.email ?: "Pengguna"

            // FIX: pakai .first() untuk baca sekali — tidak ada race condition
            val existingAccounts = accountManager.savedAccountsFlow.first()
            val hasAccounts = existingAccounts.isNotEmpty()

            Result.success(Pair(username, hasAccounts))
        } catch (e: Exception) {
            Result.failure(Exception("Login Google gagal: ${e.message}"))
        }
    }

    // Setelah pilih role — simpan akun
    suspend fun saveRoleAccount(role: UserRole): Result<UserSession> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Tidak ada user"))
            val username = user.displayName ?: user.email ?: "Pengguna"
            val email = user.email ?: ""
            val uid = "${user.uid}_${role.name.lowercase()}"

            // Simpan ke Firestore
            val userData = mapOf(
                "uid" to uid,
                "username" to username,
                "email" to email,
                "role" to role.name,
                "linkedTo" to user.uid,
                "createdAt" to System.currentTimeMillis()
            )
            usersCollection.document(uid).set(userData).await()

            // Simpan ke AccountManager
            val account = SavedAccount(
                uid = uid,
                email = email,
                username = username,
                role = role)
            accountManager.saveAccount(account)

            Result.success(
                UserSession(
                    uid = uid,
                    username = username,
                    email = email,
                    role = role)
            )
        } catch (e: Exception) {
            Result.failure(Exception("Gagal simpan role: ${e.message}"))
        }
    }

    // Switch ke akun lain
    suspend fun switchAccount(account: SavedAccount): Result<UserSession> {
        return try {
            accountManager.setActiveAccount(account.uid)
            Result.success(
                UserSession(
                    uid = account.uid,
                    username = account.username,
                    email = account.email,
                    role = account.role)
            )
        } catch (e: Exception) {
            Result.failure(Exception("Gagal switch akun"))
        }
    }

    suspend fun removeAccount(uid: String) {
        accountManager.removeAccount(uid)
    }
    fun logout() {
        auth.signOut()
    }
    suspend fun logoutAll() {
        auth.signOut(); accountManager.clearAll()
    }
}
