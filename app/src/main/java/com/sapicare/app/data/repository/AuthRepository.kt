package com.sapicare.app.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.sapicare.app.data.local.AccountManager
import com.sapicare.app.data.model.ApprovalStatus
import com.sapicare.app.data.model.NotificationItem
import com.sapicare.app.data.model.SavedAccount
import com.sapicare.app.data.model.UserProfile
import com.sapicare.app.data.model.UserRole
import com.sapicare.app.data.model.UserSession
import com.sapicare.app.data.remote.FcmTokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val accountManager: AccountManager,
    private val notificationRepository: NotificationRepository,
    private val fcmTokenManager: FcmTokenManager  // ← TAMBAH
) {
    private val usersCollection = firestore.collection("users")
    private val uidIndexCollection = firestore.collection("uidIndex")

    val savedAccountsFlow = accountManager.savedAccountsFlow
    val activeUidFlow = accountManager.activeUidFlow


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
            role = account.role,
            approvalStatus = account.approvalStatus
        )
    }

    // ─── DAFTARKAN MAPPING authUid -> docId KE uidIndex ──────────────────────
    private suspend fun registerUidIndex(authUid: String, docId: String) {
        try {
            val doc = uidIndexCollection.document(authUid).get().await()
            val currentDocIds = if (doc.exists()) {
                (doc.get("docIds") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            } else {
                emptyList()
            }
            if (docId !in currentDocIds) {
                val updated = currentDocIds + docId
                uidIndexCollection.document(authUid).set(
                    mapOf("authUid" to authUid, "docIds" to updated)
                ).await()
            }
        } catch (e: Exception) {
            Log.e("UID_INDEX", "Gagal daftar uidIndex: ${e.message}")
        }
    }

    // ─── LOGIN DINAS (email & password) ──────────────────────────────────────
    suspend fun loginDinas(email: String, password: String): Result<UserSession> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Login gagal"))

            // Cek di Firestore apakah akun ini memang Dinas
            Log.d("DINAS", "UID = ${user.uid}")

            val doc = usersCollection.document(user.uid).get().await()

            Log.d("DINAS", "DOC EXISTS = ${doc.exists()}")
            Log.d("DINAS", "DATA = ${doc.data}")

            val profile = doc.toObject(UserProfile::class.java)

            Log.d("DINAS", "isDinas raw = ${doc.getBoolean("isDinas")}")
            Log.d("DINAS", "isDinas profile = ${profile?.isDinas}")

            Log.d("DINAS", "PROFILE = $profile")

            val isDinas = doc.getBoolean("isDinas") ?: false

            if (profile == null || !isDinas) {
                auth.signOut()
                return Result.failure(Exception("Akun ini bukan akun Dinas"))
            }

            val dinasDocId = doc.id  // ← pakai document ID Firestore

            val account = SavedAccount(
                uid = dinasDocId,  // ← ganti ke doc.id
                email = profile.email,
                username = profile.username,
                role = UserRole.DINAS,
                approvalStatus = ApprovalStatus.APPROVED
            )
            registerUidIndex(user.uid, user.uid) // Dinas: docId == authUid
            accountManager.saveAccount(account)
            fcmTokenManager.saveToken(dinasDocId)  // ← ganti ke doc.id

            Result.success(
                UserSession(
                    uid = dinasDocId,
                    username = profile.username,
                    email = profile.email, role = UserRole.DINAS,
                    approvalStatus = ApprovalStatus.APPROVED)
            )
        } catch (e: Exception) {
            Result.failure(Exception("Login Dinas gagal: ${e.message}"))
        }
    }

    // ─── LOGIN GOOGLE (Pengurus & Peternak) ───────────────────────────────────
    suspend fun loginWithGoogle(idToken: String): Result<Pair<String, Boolean>> {

        Log.d("LOGIN_GOOGLE", "Repository dipanggil")
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(Exception("Login gagal"))
            val username = user.displayName ?: user.email ?: "Pengguna"
            val existingAccounts = accountManager.savedAccountsFlow.first()
            val hasAccounts = existingAccounts.isNotEmpty()
            Result.success(Pair(username, hasAccounts))
        } catch (e: Exception) {
            Result.failure(Exception("Login Google gagal: ${e.message}"))
        }
    }

    // ─── SIMPAN ROLE (Pengurus = PENDING, Peternak = APPROVED) ───────────────
    suspend fun saveRoleAccount(role: UserRole): Result<UserSession> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("Tidak ada user"))
            val username = user.displayName ?: user.email ?: "Pengguna"
            val email = user.email ?: ""
            val uid = "${user.uid}_${role.name.lowercase()}"

            Log.d("ROLE_SAVE", "saveRoleAccount dipanggil untuk $uid")

            // Pengurus butuh approval, Peternak langsung approved
            val approvalStatus = if (role == UserRole.PENGURUS)
                ApprovalStatus.PENDING else ApprovalStatus.APPROVED

            val userData = mapOf(
                "uid" to uid,
                "username" to username,
                "email" to email,
                "role" to role.name,
                "approvalStatus" to approvalStatus.name,
                "linkedTo" to user.uid,
                "isDinas" to false,
                "createdAt" to System.currentTimeMillis()
            )

            val existing = usersCollection.document(uid).get().await()

            if (existing.exists()) {
                val profile = existing.toObject(UserProfile::class.java)

                val session = UserSession(
                    uid = uid,
                    username = profile?.username ?: username,
                    email = profile?.email ?: email,
                    role = role,
                    approvalStatus = ApprovalStatus.valueOf(
                        profile?.approvalStatus ?: "PENDING"
                    )
                )

                accountManager.saveAccount(
                    SavedAccount(
                        uid = session.uid,
                        email = session.email,
                        username = session.username,
                        role = session.role,
                        approvalStatus = session.approvalStatus
                    )
                )

                // Di saveRoleAccount(), setelah accountManager.saveAccount(account) di bagian existing:
                fcmTokenManager.saveToken(uid)
                registerUidIndex(user.uid, uid)
                return Result.success(session)
            }
            usersCollection.document(uid).set(userData).await()

            registerUidIndex(user.uid, uid)

            // Kirim notif ke semua akun Dinas jika role PENGURUS (PENDING)
            if (role == UserRole.PENGURUS) {
                try {
                    val dinasList = getDinasAccounts()
                    dinasList.forEach { dinas ->
                        notificationRepository.addNotificationWithPush(
                            NotificationItem(
                                targetUid = dinas.uid,
                                title = "Permintaan Approval",
                                message = "$username mendaftar sebagai Pengurus/Dokter Hewan dan menunggu persetujuan.",
                                type = "APPROVAL_REQUEST"
                            )
                        )
                    }
                } catch (e: Exception) {
                    Log.e("AUTH", "Gagal kirim notif ke Dinas: ${e.message}")
                }
            }

            val account = SavedAccount(uid = uid, email = email, username = username,
                role = role, approvalStatus = approvalStatus)
            accountManager.saveAccount(account)
            // Di saveRoleAccount(), setelah accountManager.saveAccount(account) di bagian baru:
            fcmTokenManager.saveToken(uid)

            Result.success(UserSession(uid = uid, username = username, email = email,
                role = role, approvalStatus = approvalStatus))
        } catch (e: Exception) {
            Result.failure(Exception("Gagal simpan role: ${e.message}"))
        }
    }

    // ─── AMBIL SEMUA AKUN DINAS ───────────────────────────────────────────────
    private suspend fun getDinasAccounts(): List<UserProfile> {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("isDinas", true)
                .get()
                .await()

            Log.d("DINAS_NOTIF", "Jumlah dinas ditemukan: ${snapshot.documents.size}")

            snapshot.documents.mapNotNull { doc ->
                // Pakai doc.id sebagai uid karena field uid tidak ada di dokumen Dinas
                doc.toObject(UserProfile::class.java)?.copy(uid = doc.id)
            }
        } catch (e: Exception) {
            Log.e("DINAS_NOTIF", "Error: ${e.message}")
            emptyList()
        }
    }

    // ─── CEK STATUS APPROVAL TERBARU DARI FIRESTORE ──────────────────────────
    suspend fun refreshApprovalStatus(uid: String): ApprovalStatus {
        return try {
            val doc = usersCollection.document(uid).get().await()
            val statusStr = doc.getString("approvalStatus") ?: "PENDING"
            ApprovalStatus.valueOf(statusStr)
        } catch (e: Exception) {
            ApprovalStatus.PENDING
        }
    }

    // ─── UPDATE APPROVAL (dipanggil oleh Dinas) ───────────────────────────────
    suspend fun approveUser(targetUid: String, dinasUid: String): Result<Unit> {
        return try {
            usersCollection.document(targetUid).update(
                mapOf(
                    "approvalStatus" to ApprovalStatus.APPROVED.name,
                    "approvedBy" to dinasUid,
                    "approvedAt" to System.currentTimeMillis()
                )
            ).await()

            // Update juga di AccountManager kalau akun ini ada di device yang sama
            val accounts = accountManager.savedAccountsFlow.firstOrNull() ?: emptyList()
            val account = accounts.find { it.uid == targetUid }
            if (account != null) {
                accountManager.saveAccount(account.copy(approvalStatus = ApprovalStatus.APPROVED))
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal approve: ${e.message}"))
        }
    }

    suspend fun getApprovedPengurus(): List<UserProfile> {
        return try {
            usersCollection
                .whereEqualTo("role", UserRole.PENGURUS.name)
                .whereEqualTo("approvalStatus", ApprovalStatus.APPROVED.name)
                .get()
                .await()
                .documents
                .mapNotNull { doc ->
                    doc.toObject(UserProfile::class.java)?.copy(uid = doc.id)
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun rejectUser(targetUid: String): Result<Unit> {
        return try {
            usersCollection.document(targetUid).update(
                mapOf(
                    "approvalStatus" to ApprovalStatus.REJECTED.name,
                    "rejectedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal reject: ${e.message}"))
        }
    }

    // ─── REAPPLY APPROVAL (dipanggil oleh Dinas) ───────────────────────────────

    suspend fun reapplyApproval(uid: String): Result<Unit> {
        return try {

            usersCollection.document(uid).update(
                "approvalStatus",
                ApprovalStatus.PENDING.name
            ).await()

            val accounts = accountManager.savedAccountsFlow.first()
            val account = accounts.find { it.uid == uid }

            if (account != null) {
                accountManager.saveAccount(
                    account.copy(
                        approvalStatus = ApprovalStatus.PENDING
                    )
                )
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(Exception("Gagal mengajukan ulang"))
        }
    }

    // ─── AMBIL SEMUA USER PROFILES (untuk tab Kelola Akun di Dinas) ──────────
    suspend fun getAllUserProfiles(): Result<List<UserProfile>> {
        return try {
            val snapshot = usersCollection
                .whereEqualTo("isDinas", false)
                .get().await()
            val list = snapshot.documents.mapNotNull { it.toObject(UserProfile::class.java) }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal ambil data user: ${e.message}"))
        }
    }

    suspend fun switchAccount(account: SavedAccount): Result<UserSession> {
        return try {

            accountManager.saveAccount(account)

            accountManager.setActiveAccount(account.uid)

            Result.success(
                UserSession(
                    uid = account.uid,
                    username = account.username,
                    email = account.email,
                    role = account.role,
                    approvalStatus = account.approvalStatus
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Gagal switch akun"))
        }
    }

    suspend fun removeAccount(uid: String) {
        accountManager.removeAccount(uid)
    }
    suspend fun logout() {

        val activeUid = activeUidFlow.firstOrNull()

        if (!activeUid.isNullOrEmpty()) {
            fcmTokenManager.clearToken(activeUid)
        }

        auth.signOut()
        accountManager.clearAll()
    }
    suspend fun logoutAll() {

        val activeUid = activeUidFlow.firstOrNull()

        if (!activeUid.isNullOrEmpty()) {
            fcmTokenManager.clearToken(activeUid)
        }

        auth.signOut()
        accountManager.clearAll()
    }
}
