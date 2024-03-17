package com.example.passwordmanager.welcome

import com.example.passwordmanager.KeystoreManager
import android.content.Context
import android.content.Intent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.example.passwordmanager.MyEncryptedSharedPreferences
import com.example.passwordmanager.R
import com.example.passwordmanager.database.AppDatabase
import com.example.passwordmanager.passwordlist.PasswordListActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class WelcomeViewModel(
    biometricManager: BiometricManager,
    encryptedSharedPreferencesIsExist: Boolean
) : ViewModel() {

    private val _livedata =
        MutableLiveData(
            WelcomeState(
                masterKeyInputState = WelcomeState.MasterKeyInputState.Default(
                    if (encryptedSharedPreferencesIsExist) R.string.enter_masterkey
                    else R.string.create_masterkey
                ),
                fingerPrintAvailable = isFingerprintAvailable(biometricManager)
            )
        )
    val liveData: LiveData<WelcomeState>
        get() = _livedata
    private val liveDataValue: WelcomeState
        get() = liveData.value!!

    fun checkMasterKey(context: Context, masterKey: String) {
        try {
            MyEncryptedSharedPreferences.initialize(context, masterKey)
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            sharedPreferences.edit().putInt("EncryptedSharedPreferencesIsExist", 1).apply()
            KeystoreManager.saveEncryptedString(context, masterKey)
            val intent = Intent(context, PasswordListActivity::class.java)
            context.startActivity(intent)
        } catch (e: Exception) {
            _livedata.value =
                liveDataValue.copy(
                    masterKeyInputState = WelcomeState.MasterKeyInputState.Error
                )
        }
    }

    fun resetMasterKey(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.deletion_confirmation)
            .setMessage(R.string.are_you_sure_you_want_to_reset_the_masterkey)
            .setNegativeButton(context.getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.reset_the_masterkey) { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    AppDatabase.getDatabase(context).passwordDao()
                        .deleteAllPasswords()
                }
                MyEncryptedSharedPreferences.deleteEncryptedSharedPreferences(context)
                KeystoreManager.saveEncryptedString(context, "")
                val preferences = PreferenceManager.getDefaultSharedPreferences(context)
                preferences.edit().putInt("EncryptedSharedPreferencesIsExist", -1).apply()
                _livedata.value =
                    liveDataValue.copy(
                        masterKeyInputState = WelcomeState.MasterKeyInputState.Default(
                            masterKeyHintRes = R.string.create_masterkey
                        )
                    )
            }
            .show()
    }

    fun fingerPrintAuthentication(activity: FragmentActivity) {
        if (liveDataValue.fingerPrintAvailable &&
            KeystoreManager.getDecryptedString(activity).toString() != ""
        ) {
            authenticateWithFingerprint(activity)
        } else {
            if (!liveDataValue.fingerPrintAvailable) {
                MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.fingerprint_not_available)
                    .setPositiveButton(R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            } else if (KeystoreManager.getDecryptedString(activity).toString() == "") {
                MaterialAlertDialogBuilder(activity)
                    .setTitle(R.string.masterkey_not_specified)
                    .setMessage(R.string.create_masterkey)
                    .setPositiveButton(R.string.ok) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        }
    }

    private fun isFingerprintAvailable(biometricManager: BiometricManager): Boolean {
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_WEAK
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }


    private fun authenticateWithFingerprint(activity: FragmentActivity) {
        val biometricPrompt = BiometricPrompt(
            activity,
            Executors.newSingleThreadExecutor(),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val intent = Intent(activity, PasswordListActivity::class.java)
                    activity.startActivity(intent)
                }
            })
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.authenticate_with_fingerprint))
            .setNegativeButtonText(activity.getString(R.string.cancel))
            .build()
        biometricPrompt.authenticate(promptInfo)
    }
}