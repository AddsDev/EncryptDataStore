package dev.adds.encryptdatastore.preferences.utils

import android.security.keystore.KeyProperties.*
import android.util.Log
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * AES Algorithms for API 21+
 */
class AESUtil {
    companion object {
        private const val IV_VALUE = "XYZ1"
        private const val ENC_VALUE = "XYZ2"
        private const val ENCRYPT_PASSWORD = "YZW1"
        private const val SALT_VALUE = "XYZ3"
        private const val ALGORITHM_PBK = "PBKDF2WithHmacSHA1"
        private const val PROVIDER = "AndroidKeyStore"
        private val TRANSFORM_AES by lazy {
            "${KEY_ALGORITHM_AES}/${BLOCK_MODE_CBC}/${ENCRYPTION_PADDING_PKCS7}"
        }
    }

    private val iterationCount = 1324
    private val keyLength = 256
    private val cipher by lazy {
        Cipher.getInstance(TRANSFORM_AES)
    }
    private val keyBytes by lazy {
        SecretKeyFactory.getInstance(ALGORITHM_PBK)
    }

    /**
     * Validate exists Key
     */
    fun isKeyExists(alias: String): Boolean {
        val aliases = KeyStore.getInstance(PROVIDER).aliases()
        while (aliases.hasMoreElements())
            return (alias == aliases.nextElement())
        return false
    }
    /**
     * Encrypt data with AES
     */
    fun encrypt(dataToEncrypt: ByteArray): HashMap<String, ByteArray> {
        val salt = ByteArray(256)
        SecureRandom().nextBytes(salt)
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)

        val pbKeySpec = PBEKeySpec(ENCRYPT_PASSWORD.toCharArray(), salt, iterationCount, keyLength)
        val secKey = SecretKeySpec(keyBytes.generateSecret(pbKeySpec).encoded, KEY_ALGORITHM_AES)

        cipher.init(Cipher.ENCRYPT_MODE, secKey, IvParameterSpec(iv))

        Log.i("SECURE_EDIT", "encrypt: ${Json.encodeToString(hashMapOf(
            Pair(SALT_VALUE, salt),
            Pair(IV_VALUE, iv),
            Pair(ENC_VALUE, cipher.doFinal(dataToEncrypt))
        )).toByteArray().size}")
        return hashMapOf(
            Pair(SALT_VALUE, salt),
            Pair(IV_VALUE, iv),
            Pair(ENC_VALUE, cipher.doFinal(dataToEncrypt))
        )
    }

    /**
     * Decrypt data
     */

    fun decrypt(map: HashMap<String, ByteArray>): String {
        val salt = map[SALT_VALUE]
        val iv = map[IV_VALUE]
        val encryptedData = map[ENC_VALUE]

        val pbKeySpec = PBEKeySpec(ENCRYPT_PASSWORD.toCharArray(), salt, iterationCount, keyLength)
        val secKey = SecretKeySpec(keyBytes.generateSecret(pbKeySpec).encoded, KEY_ALGORITHM_AES)

        cipher.init(Cipher.DECRYPT_MODE, secKey, IvParameterSpec(iv))
        return String(cipher.doFinal(encryptedData))
    }
}