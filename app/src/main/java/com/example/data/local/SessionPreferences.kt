package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.eclDataStore: DataStore<Preferences> by preferencesDataStore(name = "ecl_session_prefs")

class SessionPreferences(private val context: Context) {
    private val currentUserIdKey = stringPreferencesKey("current_user_id")

    // Default to pre-loaded player u_yacine so all screens and Match of the Day can be tested immediately.
    val currentUserId: Flow<String?> = context.eclDataStore.data.map { prefs ->
        if (prefs.contains(currentUserIdKey)) {
            prefs[currentUserIdKey]?.ifBlank { null }
        } else {
            "u_yacine"
        }
    }

    suspend fun setCurrentUserId(userId: String?) {
        context.eclDataStore.edit { prefs ->
            prefs[currentUserIdKey] = userId ?: ""
        }
    }
}
