package dev.adds.encryptdatastore.preferences.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
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
        /**
         * Using AES with CBC and No Padding Encrypt
         */
        val encryptedData = AESCBCUtil.encrypt(keyAlias, Json.encodeToString(value))
        editStore.invoke(it, Json.encodeToString(encryptedData))
    }
}

inline fun <reified T> Flow<Preferences>.secureMap(
    keyAlias: String,
    crossinline fetchValue: (value: Preferences) -> String
): Flow<T> {
    val json = Json { encodeDefaults = true }
    return map { preferences ->
        /**
         * Using AES with CBC and No Padding Encrypt
         */
        val pair = json.decodeFromString<Pair<ByteArray, ByteArray>>(fetchValue(preferences))
        json.decodeFromString(AESCBCUtil.decrypt(keyAlias,pair.first, pair.second))
    }
}

/**
eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJhdWQiOiI5MjVlYzAxZC1iOGMzLTRhYWUtOTc5OS1hYjEyZDA2OTE5NTMiLCJqdGkiOiI4M2EyZjNmZWM4OGMzOWVhM2E2ZGYxMmFhMDk1ZWJmY2Q1ZTlmYzU3ZjM1MTc4NTM3MTgxN2M1YmIwNDE0YTVkM2RlY2ZlMTZkMjEzNTBhYyIsImlhdCI6MTY2NzMxMDE1OC42MjIxMDIsIm5iZiI6MTY2NzMxMDE1OC42MjIxMDYsImV4cCI6MTY2NzMxNzM1OC41ODk5MTIsInN1YiI6Ijk3NzAzNjJhLWJjZTMtNGM4NS05YzM4LTdhMTllMzIyZGM4ZCIsInNjb3BlcyI6WyIqIl19.pFVGoXeNZ8Dhew-a2_iH8vfV3VN1O-Pp0xLZR8UP9Rm1uG0qlqSKjv19WVglIZ9P-EA1HI0GxnzK7aMD4iXV2ykqVbqnlxuSwrZqaUSHTZwk4Mw5YUInZggB9HQmFXvYgvwdT68bTG9FyYS8ecK-nDyjT1nmjSwr3OMooTZqXFm2Ga_HNBuv0jyZCSiJjYrnVpylFMt9RH1JdlNyA1LyQdDsQuR2OUiIOYr6y3FCfwER7GCof3pe347oXs-FHK-kR-ipUEpi4vEEwDPcTS8_7vX70CfZ-e-14lkOqAcB7qzclv-Qa85dNGL_Jh-GBxiHb8-6BNPvTKqMHfCZqwGkajkFGQjrUsCM_SZPRgkdwTBOcEywQei7j-wy-Vca_qowLNMMk2GA3aVbOgvp7A9FmQYtjItM5eY-W5TTr-ntTPvyfUw3jhxMn0SIh-h6QYemUZZQDYMFxxHmDjWZiSpplGk6tDmuuq6HfmhjOy1JkLny91-wt4xz8SP-qCHcHcMHZdEpfursu54TvvQh5kHAEG25DjZvWkVTacgeZp_kWUybLqDvxqbtTPF8OyIDRf7lF9ruuIQborITkPduBZX-kj3qivg2fbQZwEv3cc1gfRWXupqvZypFR5IraKQj9Y9rqNDWK4924Mj8LdULlow1qD5xyOkPsPWcIjtmuPEnLrk
 */
