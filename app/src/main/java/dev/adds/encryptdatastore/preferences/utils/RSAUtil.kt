package dev.adds.encryptdatastore.preferences.utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.KeyProperties.*
import android.util.Base64
import android.util.Log
import java.math.BigInteger
import java.security.*
import java.security.KeyStore.Entry
import java.security.KeyStore.PrivateKeyEntry
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal
import kotlin.math.abs

object RSAUtil{

    private const val TAG = "SecurityUtil"

    private const val provider = "AndroidKeyStore"
    private val cipher by lazy {
        Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
    }

    /**
     * Create a private and public key and store
     */
    fun createKeys(context: Context, keyAlias: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            createKeysM(keyAlias, false)
        else
            createKeysJBRM2(keyAlias, context)
    }

    /**
     * Create KeyPair for Olds API
     */
    private fun createKeysJBRM2(alias: String, context: Context): KeyPair {
        val start = GregorianCalendar()
        val end = GregorianCalendar().apply {
            add(Calendar.YEAR, 30)
        }
        val spec = KeyPairGeneratorSpec.Builder(context)
            .setAlias(alias)
            .setSubject(X500Principal("CN=$alias"))
            .setSerialNumber(BigInteger.valueOf(abs(alias.hashCode()).toLong()))
            .setStartDate(start.time)
            .setEndDate(end.time)
            .build()
        val keyPair = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, provider)
        keyPair.initialize(spec)
        return keyPair.genKeyPair().apply {
            Log.d(TAG, "Public Key is: " + this.public.toString());
        }
    }

    /**
     * Create KeyPair
     */
    @TargetApi(Build.VERSION_CODES.M)
    private fun createKeysM(alias: String, auth: Boolean): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM_RSA, provider)
        keyPairGenerator.initialize(
            KeyGenParameterSpec.Builder(alias, PURPOSE_ENCRYPT or PURPOSE_DECRYPT)
                .setDigests(DIGEST_SHA256, DIGEST_SHA512)
                .setEncryptionPaddings(ENCRYPTION_PADDING_RSA_OAEP)
                .build()
        )
        return keyPairGenerator.generateKeyPair().apply {
            Log.d(TAG, "Public Key is: " + this.public.toString());
            Log.d(TAG, "Public Key is: " + keyPairGenerator.algorithm.toString())
        }

    }

    /**
     * Validate signing key
     */
    fun isSigningKey(alias: String): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            with(KeyStore.getInstance(provider)) {
                load(null)
                containsAlias(alias)
            }
        } else
            false
    }

    fun getSigningKey(alias: String): String {
        val certificate = getPrivateKeyEntry(alias)?.certificate
        return Base64.encodeToString(certificate?.encoded, Base64.NO_WRAP) ?: "null"
    }

    private fun getPrivateKeyEntry(alias: String): PrivateKeyEntry? {
        val keyStore = KeyStore.getInstance(provider).apply {
            load(null)
        }
        val entry: Entry? = keyStore.getEntry(alias, null)

        return entry?.let {
            it as PrivateKeyEntry
        }
    }

    fun encrypt(keyAlias: String, text: String): String =
        try {
            val publicKey =
                getPrivateKeyEntry(alias = keyAlias)?.certificate?.publicKey

            with(cipher) {
                init(Cipher.ENCRYPT_MODE, publicKey)
                Base64.encodeToString(doFinal(text.toByteArray()), Base64.NO_WRAP)
            }
        } catch (ex: GeneralSecurityException) {
            Log.wtf(TAG, ex)
            "\"Encrypt Error\""
        } catch (ex: java.lang.Exception) {
            Log.wtf(TAG, ex)
            "\"Encrypt Error\""
        }

    fun decrypt(keyAlias: String, encryptData: String): String =
        try {
            val privateKey = getPrivateKeyEntry(alias = keyAlias)?.privateKey
            with(cipher) {
                init(Cipher.DECRYPT_MODE, privateKey)
                String(doFinal(Base64.decode(encryptData, Base64.NO_WRAP)))
            }
        } catch (ex: GeneralSecurityException) {
            Log.wtf(TAG, ex)
            "\"Decrypt Error\""
        } catch (ex: java.lang.Exception) {
            Log.wtf(TAG, ex)
            "\"Decrypt Error\""
        }

    fun createStringFromSize(alias: String, size: Int): String {
        var newString = alias

        while(newString.toByteArray().size - 11 < size - 11){
            newString += "b".repeat(1)
        }

        return newString
    }
}