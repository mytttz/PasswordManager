package com.example.passwordmanager.welcome

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.preference.PreferenceManager
import com.example.passwordmanager.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val viewModel = WelcomeViewModel(
            biometricManager = BiometricManager.from(this),
            encryptedSharedPreferencesIsExist = sharedPreferences.contains("EncryptedSharedPreferencesIsExist")
        )

        viewModel.liveData.observe(this) { state ->
            when (state.masterKeyInputState) {
                is WelcomeState.MasterKeyInputState.Default -> {
                    masterKeyInputLayout.hint =
                        resources.getString(state.masterKeyInputState.masterKeyHintRes)
                    masterKeyInput.text?.clear()
                    masterKeyInputLayout.boxStrokeColor =
                        getColor(R.color.md_theme_primaryContainer)
                    masterKeyInputLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    masterKeyInputLayout.error = null
                }

                is WelcomeState.MasterKeyInputState.Error -> {
                    masterKeyInputLayout.error = getString(R.string.wrong_master_key)
                    masterKeyInputLayout.boxStrokeErrorColor
                }
            }
        }
        buttonEnter.setOnClickListener {
            viewModel.checkMasterKey(this, masterKeyInput.text.toString())
        }
        resetMasterKey.setOnClickListener {
            viewModel.resetMasterKey(this)
        }
        fingerprintButton.setOnClickListener {
            viewModel.fingerPrintAuthentication(this)
        }
    }
}