package com.example.passwordmanager

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object MyEncryptedSharedPreferences {
    private var sharedPreferences: EncryptedSharedPreferences? = null

    fun initialize(context: Context, masterKeyString: String) {

        val masterKey =
            MasterKey.Builder(context, masterKeyString)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "encrypted_prefs_file",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun updateMasterKey(context: Context, newMasterKeyString: String) {
        context.getSharedPreferences("encrypted_prefs_file", Context.MODE_PRIVATE).edit().clear()
            .apply()

        initialize(context, newMasterKeyString)
    }

    fun deleteEncryptedSharedPreferences(context: Context) {
        context.getSharedPreferences("encrypted_prefs_file", Context.MODE_PRIVATE).edit().clear()
            .apply()
    }

    fun getEncryptedSharedPreferences(): EncryptedSharedPreferences {
        return sharedPreferences
            ?: throw IllegalStateException("SharedPreferencesManager is not initialized")
    }
}
