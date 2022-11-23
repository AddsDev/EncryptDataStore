package dev.adds.encryptdatastore.preferences.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

/**
 * AES Algorithms for API 23+
 */
object AESCBCUtil {

    private val PROVIDER by lazy {
        "AndroidKeyStore"
    }

    private val keyGenerator by lazy {
        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
    }

    private val keyStore by lazy {
        KeyStore.getInstance(PROVIDER).apply {
            load(null)
        }
    }

    private val cipher by lazy {
        Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_NONE}")
    }

    private fun generateKey(alias: String): SecretKey =
        with(keyStore) {
            val keyGenParameterSpec =
                KeyGenParameterSpec.Builder(
                    alias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }

    private fun getKey(alias: String): SecretKey =
        (keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry).secretKey

    fun encrypt(alias: String, dataToEncrypt: String): Pair<ByteArray, ByteArray> {
        generateKey(alias)
        var temp = dataToEncrypt
        while (temp.toByteArray().size % 16 != 0)
            temp += "\u0020"
        cipher.init(Cipher.ENCRYPT_MODE, getKey(alias))
        return Pair(cipher.iv, cipher.doFinal(temp.toByteArray(Charsets.UTF_8)))
    }

    fun decrypt(alias: String, ivBytes: ByteArray, data: ByteArray): String =
        with(cipher) {
            init(Cipher.DECRYPT_MODE, getKey(alias), IvParameterSpec(ivBytes))
            doFinal(data).toString(Charsets.UTF_8).trim()
        }

}