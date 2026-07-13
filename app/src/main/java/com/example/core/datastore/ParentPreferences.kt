package com.example.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "parent_prefs")

class ParentPreferences(private val context: Context) {
    
    companion object {
        val SELECTED_CHILD_ID = stringPreferencesKey("selected_child_id")
    }

    fun getSelectedChild(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[SELECTED_CHILD_ID]
        }
    }

    suspend fun saveSelectedChild(childId: String) {
        context.dataStore.edit { preferences ->
            preferences[SELECTED_CHILD_ID] = childId
        }
    }
}
