package dev.adds.encryptdatastore.preferences.utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties.*
import android.util.Base64
import android.util.Log
import java.math.BigInteger
import java.security.GeneralSecurityException
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.KeyStore.Entry
import java.security.KeyStore.PrivateKeyEntry
import java.security.spec.RSAKeyGenParameterSpec
import java.security.spec.RSAKeyGenParameterSpec.F4
import java.util.*
import javax.crypto.Cipher
import javax.security.auth.x500.X500Principal
import kotlin.math.abs

class RSAUtil(private val context: Context,private val keyAlias: String) {

    companion object {
        private const val TAG = "SecurityUtil"
    }

    private val provider = "AndroidKeyStore"
    private val cipher by lazy {
        Cipher.getInstance("${KEY_ALGORITHM_RSA}/NONE/${ENCRYPTION_PADDING_RSA_PKCS1}")
    }

    init {
        createKeys(context, keyAlias)
    }

    /**
     * Create a private and public key and store
     */
    private fun createKeys(context: Context, keyAlias: String) {
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
            KeyGenParameterSpec.Builder(
                alias,
                PURPOSE_ENCRYPT or PURPOSE_DECRYPT
            )
                .setAlgorithmParameterSpec(RSAKeyGenParameterSpec(4096, F4))
                .setBlockModes(BLOCK_MODE_CBC)
                .setEncryptionPaddings(ENCRYPTION_PADDING_RSA_PKCS1)
                .setDigests(DIGEST_SHA256)
                .setUserAuthenticationRequired(auth)
                .build()
        )
        return keyPairGenerator.generateKeyPair().apply {
            Log.d(TAG, "Public Key is: " + this.public.toString());
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
}