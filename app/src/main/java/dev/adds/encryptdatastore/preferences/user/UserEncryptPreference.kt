package dev.adds.encryptdatastore.preferences.user

import kotlinx.coroutines.flow.Flow

interface UserEncryptPreference {
    suspend fun updateTestDataEncrypt(value: String)

    val dataEncrypt: Flow<String>
}