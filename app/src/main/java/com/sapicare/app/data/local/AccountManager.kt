package com.sapicare.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sapicare.app.data.model.SavedAccount
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

val Context.accountDataStore: DataStore<Preferences> by preferencesDataStore(name = "saved_accounts")

@Singleton
class AccountManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    companion object {
        val KEY_ACCOUNTS = stringPreferencesKey("accounts")
        val KEY_ACTIVE_UID = stringPreferencesKey("active_uid")
    }

    val savedAccountsFlow: Flow<List<SavedAccount>> = context.accountDataStore.data.map { prefs ->
        val json = prefs[KEY_ACCOUNTS] ?: return@map emptyList()
        val type = object : TypeToken<List<SavedAccount>>() {}.type
        gson.fromJson(json, type) ?: emptyList()
    }

    val activeUidFlow: Flow<String?> = context.accountDataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_UID]
    }

    suspend fun saveAccount(account: SavedAccount) {
        context.accountDataStore.edit { prefs ->
            val current = getAccountsFromPrefs(prefs).toMutableList()
            // Update kalau sudah ada, tambah kalau belum
            val index = current.indexOfFirst { it.uid == account.uid }
            if (index >= 0) current[index] = account
            else current.add(account)
            prefs[KEY_ACCOUNTS] = gson.toJson(current)
            prefs[KEY_ACTIVE_UID] = account.uid
        }
    }

    suspend fun setActiveAccount(uid: String) {
        context.accountDataStore.edit { prefs ->
            prefs[KEY_ACTIVE_UID] = uid
        }
    }

    suspend fun removeAccount(uid: String) {
        context.accountDataStore.edit { prefs ->
            val current = getAccountsFromPrefs(prefs).toMutableList()
            current.removeAll { it.uid == uid }
            prefs[KEY_ACCOUNTS] = gson.toJson(current)

            // Kalau yang dihapus adalah akun aktif, set ke akun pertama
            val activeUid = prefs[KEY_ACTIVE_UID]
            if (activeUid == uid) {
                prefs[KEY_ACTIVE_UID] = current.firstOrNull()?.uid ?: ""
            }
        }
    }

    suspend fun clearAll() {
        context.accountDataStore.edit { it.clear() }
    }

    private fun getAccountsFromPrefs(prefs: Preferences): List<SavedAccount> {
        val json = prefs[KEY_ACCOUNTS] ?: return emptyList()
        val type = object : TypeToken<List<SavedAccount>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}
