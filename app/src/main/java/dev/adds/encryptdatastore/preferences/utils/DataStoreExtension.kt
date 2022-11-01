package dev.adds.encryptdatastore.preferences.utils

import android.content.Context
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
    context: Context,
    keyAlias: String,
    value: T,
    crossinline editStore: (MutablePreferences, String) -> Unit
) {
    edit {
        /**
         * Using RSA Encrypt
         */
        /*val rsa = RSAUtil(context, keyAlias)
        val encryptedValue =
            rsa.encryptData(keyAlias, Json.encodeToString(value))
        editStore.invoke(it, encryptedValue)*/
        val encryptedValue =
            AESUtil().encrypt(Json.encodeToString(value).toByteArray())
        editStore.invoke(it, Json.encodeToString(encryptedValue))
    }
}

inline fun <reified T> Flow<Preferences>.secureMap(
    keyAlias: String,
    crossinline fetchValue: (value: Preferences) -> String
): Flow<T> {
    val json = Json { encodeDefaults = true }
    return map { preferences ->
        /**
         * Using RSA Decrypt
         */
        /*
        val rsa = RSAUtil(context, keyAlias)
        val decryptedValue = rsa.decryptData(
            keyAlias,
            fetchValue(preferences)
        )
        json.decodeFromString(decryptedValue)*/
        val decryptedValue = AESUtil().decrypt(
            json.decodeFromString(fetchValue(preferences)) as HashMap<String,ByteArray>
        )
        json.decodeFromString(decryptedValue)
    }
}