package dev.adds.encryptdatastore.preferences.user

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.adds.encryptdatastore.preferences.user.UserStorePreference.PreferencesKeys.PREF_DATA
import dev.adds.encryptdatastore.preferences.user.UserStorePreference.PreferencesKeys.PREF_USER_ID
import dev.adds.encryptdatastore.preferences.utils.secureEdit
import dev.adds.encryptdatastore.preferences.utils.secureMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserStorePreference constructor(
    private val dataStore: DataStore<Preferences>
) : UserEncryptPreference {
    object PreferencesKeys {
        val PREF_DATA = stringPreferencesKey("pref_data")
        val PREF_USER_ID = stringPreferencesKey("pref_user_id")
    }
    override suspend fun updateTestDataEncrypt(value: String) {
        dataStore.secureEdit(PREF_DATA.name, value) { preferences, encryptedValue ->
            preferences[PREF_DATA] = encryptedValue
        }
    }

    override val dataEncrypt: Flow<String> = dataStore.data.secureMap(PREF_DATA.name) {
        it[PREF_DATA].orEmpty()
    }

    suspend fun updateUserId(userId: String) {
        dataStore.edit {
            it[PREF_USER_ID] = userId
        }
    }

    val userId: Flow<String> = dataStore.data.map { it[PREF_USER_ID] ?: "" }


}