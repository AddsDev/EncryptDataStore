package dev.adds.encryptdatastore.preferences.user

import android.content.Context
import kotlinx.coroutines.flow.Flow

interface UserEncryptPreference {
    suspend fun updateTestDataEncrypt(context: Context, value: String)

    val dataEncrypt: Flow<String>
}