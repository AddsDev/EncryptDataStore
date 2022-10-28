package dev.adds.encryptdatastore.preferences.utils

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json



suspend inline fun <reified T> DataStore<Preferences>.secureEdit(
    keyAlias: String,
    value: T,
    crossinline editStore: (MutablePreferences, String) -> Unit
) {
    edit {
        val encryptedValue =
            SecurityUtil.encryptData(keyAlias, Json.encodeToString(value))
        editStore.invoke(it, encryptedValue.toString())
    }
}

inline fun <reified T> Flow<Preferences>.secureMap(keyAlias: String, crossinline fetchValue: (value: Preferences) -> String ): Flow<T> {
    val json = Json { encodeDefaults = true }
    val TAG = "SecurityUtil"
    return map { preferences ->
        val decryptedValue = SecurityUtil.decryptData(
            keyAlias,
            fetchValue(preferences).toByteArray()
        )
        json.decodeFromString(decryptedValue)
    }
}