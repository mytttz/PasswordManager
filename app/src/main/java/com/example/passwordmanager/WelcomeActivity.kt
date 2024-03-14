package com.example.passwordmanager

import KeystoreManager
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.preference.PreferenceManager
import com.example.passwordmanager.database.AppDatabase
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class WelcomeActivity : AppCompatActivity() {
    private lateinit var buttonEnter: MaterialButton
    private lateinit var masterKeyInput: TextInputEditText
    private lateinit var resetMasterKey: MaterialButton
    private lateinit var masterKeyInputLayout: TextInputLayout
    private lateinit var fingerprintButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        buttonEnter = findViewById(R.id.enterButton)
        masterKeyInput = findViewById(R.id.masterKeyEnter)
        resetMasterKey = findViewById(R.id.resetMasterKey)
        masterKeyInputLayout = findViewById(R.id.textFieldLayout)
        fingerprintButton = findViewById(R.id.fingerprintButton)
        val decryptedMasterKey = KeystoreManager.getDecryptedString(this).toString()
        Log.i("master", decryptedMasterKey)
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (preferences.getInt("EncryptedSharedPreferencesIsExist", -1) == 1) {
            masterKeyInputLayout.hint = resources.getString(R.string.enter_masterkey)
        } else {
            masterKeyInputLayout.hint = resources.getString(R.string.create_masterkey)
        }
        val intent = Intent(this, PasswordListActivity::class.java)


        if (isFingerprintAvailable() && decryptedMasterKey != "") {
            fingerprintButton.setOnClickListener {
                authenticateWithFingerprint(intent)
            }
        } else {
            fingerprintButton.setOnClickListener {

                if (!isFingerprintAvailable()) {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Fingerprint not available")
                        .setPositiveButton("Cancel") { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()
                } else if (decryptedMasterKey == "") {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("MasterKey not specified")
                        .setMessage("Create MasterKey")
                        .setPositiveButton("Ok") { dialog, which ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
        buttonEnter.setOnClickListener {
            try {
                MyEncryptedSharedPreferences.initialize(this, masterKeyInput.text.toString())
                preferences.edit().putInt("EncryptedSharedPreferencesIsExist", 1).apply()
                KeystoreManager.saveEncryptedString(this, masterKeyInput.text.toString())
                startActivity(intent)
            } catch (e: Exception) {
                masterKeyInputLayout.error = getString(R.string.wrong_master_key)
                masterKeyInputLayout.boxStrokeErrorColor
            }
        }
        resetMasterKey.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle("Deletion confirmation")
                .setMessage("Are you sure you want to reset the MasterKey? This will delete all recorded data and passwords. This action is irreversible")
                .setNegativeButton("Cancel") { dialog, which ->
                    dialog.dismiss()
                }
                .setPositiveButton("Reset the MasterKey") { dialog, which ->
                    CoroutineScope(Dispatchers.IO).launch {
                        AppDatabase.getDatabase(this@WelcomeActivity).passwordDao()
                            .deleteAllPasswords()
                    }
                    MyEncryptedSharedPreferences.deleteEncryptedSharedPreferences(this@WelcomeActivity)
                    KeystoreManager.saveEncryptedString(this, "")
                    masterKeyInputLayout.hint = resources.getString(R.string.create_masterkey)
                    masterKeyInput.text?.clear()
                    masterKeyInputLayout.boxStrokeColor =
                        getColor(R.color.md_theme_primaryContainer)
                    masterKeyInputLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    masterKeyInputLayout.error = null
                }
                .show()
        }
    }

    private fun isFingerprintAvailable(): Boolean {
        val biometricManager = BiometricManager.from(this)
        return biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS
    }

    private fun authenticateWithFingerprint(intent: Intent) {
        val biometricPrompt = BiometricPrompt(
            this,
            Executors.newSingleThreadExecutor(),
            object : BiometricPrompt.AuthenticationCallback() {


                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    startActivity(intent)
                }


            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Authenticate with fingerprint")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

}