package com.example.passwordmanager

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

object KeystoreManager {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_ALIAS =
        "583109675FA26D518DAECA5C5DE1CE8643F844C95CF6731E9315DC8E2BA7FC12C1C25C31D8B8D41AA12B91ED1BF5BBD5"

    fun saveEncryptedString(context: Context, value: String) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator =
                KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(value.toByteArray())
        val encryptedString = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        val ivString = Base64.encodeToString(iv, Base64.DEFAULT)
        val sharedPreferences =
            context.getSharedPreferences("encrypted_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString("encrypted_value", encryptedString)
            putString("iv", ivString)
            apply()
        }
    }

    fun getDecryptedString(context: Context): String? {
        val sharedPreferences =
            context.getSharedPreferences("encrypted_prefs", Context.MODE_PRIVATE)
        val encryptedString = sharedPreferences.getString("encrypted_value", null) ?: return null
        val ivString = sharedPreferences.getString("iv", null) ?: return null
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val iv = Base64.decode(ivString, Base64.DEFAULT)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))
        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedString, Base64.DEFAULT))
        return String(decryptedBytes)
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey
    }
}
