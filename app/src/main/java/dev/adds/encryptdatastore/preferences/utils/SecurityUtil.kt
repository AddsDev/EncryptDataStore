package dev.adds.encryptdatastore.preferences.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.*
import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.GeneralSecurityException
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.SecureRandom
import java.security.spec.MGF1ParameterSpec
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.OAEPParameterSpec
import javax.crypto.spec.PSource

object SecurityUtil {

    private const val TAG = "SecurityUtil"

    private const val provider = "AndroidKeyStore"

    private val cipher by lazy {
        Cipher.getInstance("AES/GCM/NoPadding")
    }

    private val charset by lazy {
        StandardCharsets.UTF_8
    }

    private val keyStore by lazy {
        KeyStore.getInstance(provider).apply {
            load(null)
        }
    }

    private val keyGenerator by lazy {
        KeyGenerator.getInstance(KEY_ALGORITHM_AES, provider)
    }

    private val secureRandom by lazy {
        SecureRandom()
    }

    private var iv = byteArrayOf()
/*    private val keyPairGenerator by lazy {
        KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, provider)
    }*/

    private fun getSecretKey(keyAlias: String) =
        (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey

    private fun generateSecretKey(keyAlias: String) =
        keyGenerator.apply {
            secureRandom.nextBytes(iv)
            init(
                KeyGenParameterSpec
                    .Builder(keyAlias, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
                    .setBlockModes(BLOCK_MODE_GCM)
                    .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
                    .build(),
                secureRandom
            )
        }.generateKey()

    fun encryptData(keyAlias: String, text: String): ByteArray =
        cipher.apply {
            val secretKey = generateSecretKey(keyAlias)
            Log.i(TAG, "generateSecretKey: $secretKey")
            Log.i(TAG, "text: $text")
            init(Cipher.ENCRYPT_MODE, secretKey)
        }.doFinal(text.toByteArray(charset))

    fun decryptData(keyAlias: String, encryptData: ByteArray): String =
        try {
            cipher.apply {
                Log.i(TAG, "getSecretKey: ${getSecretKey(keyAlias)}")
                Log.i(TAG, "getSecretKey 2: ${getSecretKey(keyAlias)}")
                Log.i(TAG, "iv: ${cipher.iv}")
                Log.i(TAG, "encryptData: $encryptData")
                init(
                    Cipher.DECRYPT_MODE,
                    getSecretKey(keyAlias),
                    GCMParameterSpec(128, iv)
                    //OAEPParameterSpec("SHA-256","MGF1", MGF1ParameterSpec.SHA1, PSource.PSpecified.DEFAULT)
                )//)
            }.doFinal(encryptData).toString()
        }catch (ex: GeneralSecurityException) {
            Log.wtf(TAG, ex )
            "\"Decrypt Error\""
        }catch (ex: java.lang.Exception) {
            Log.wtf(TAG, ex )
            "\"Decrypt Error\""
        }
}