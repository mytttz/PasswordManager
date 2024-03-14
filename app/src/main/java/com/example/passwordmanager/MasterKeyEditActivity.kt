package com.example.passwordmanager

import KeystoreManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.security.crypto.EncryptedSharedPreferences
import com.example.passwordmanager.database.AppDatabase
import com.example.passwordmanager.database.Password
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MasterKeyEditActivity : AppCompatActivity() {
    private lateinit var confirmButton: MaterialButton
    private lateinit var oldMasterKeyLayout: TextInputLayout
    private lateinit var newMasterKeyLayout: TextInputLayout
    private lateinit var againNewMasterKeyLayout: TextInputLayout
    private lateinit var oldMasterKey: TextInputEditText
    private lateinit var newMasterKey: TextInputEditText
    private lateinit var toolbar: MaterialToolbar
    private lateinit var againNewMasterKey: TextInputEditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_master_key_edit)
        confirmButton = findViewById(R.id.confirmButton)
        oldMasterKey = findViewById(R.id.oldMasterKey)
        newMasterKey = findViewById(R.id.newMasterKey)
        againNewMasterKey = findViewById(R.id.againNewMasterKey)
        oldMasterKeyLayout = findViewById(R.id.oldMasterKeyLayout)
        newMasterKeyLayout = findViewById(R.id.newMasterKeyLayout)
        againNewMasterKeyLayout = findViewById(R.id.againNewMasterKeyLayout)
        val decryptedMasterKey = KeystoreManager.getDecryptedString(this).toString()
        toolbar = findViewById(R.id.topAppBar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        confirmButton.setOnClickListener {
            if (decryptedMasterKey == oldMasterKey.text.toString() && newMasterKey.text.toString() == oldMasterKey.text.toString() && againNewMasterKey.text.toString() == oldMasterKey.text.toString()) {
                oldMasterKeyLayout.error = null
                oldMasterKeyLayout.boxStrokeColor =
                    getColor(R.color.md_theme_primaryContainer)
                oldMasterKeyLayout.hintTextColor =
                    getColorStateList(R.color.md_theme_onPrimaryContainer)
                newMasterKeyLayout.error = null
                newMasterKeyLayout.boxStrokeColor =
                    getColor(R.color.md_theme_primaryContainer)
                newMasterKeyLayout.hintTextColor =
                    getColorStateList(R.color.md_theme_onPrimaryContainer)
                againNewMasterKeyLayout.error = getString(R.string.masterkeys_match)
                againNewMasterKeyLayout.boxStrokeColor =
                    getColor(R.color.md_theme_primaryContainer)
                againNewMasterKeyLayout.hintTextColor =
                    getColorStateList(R.color.md_theme_onPrimaryContainer)

            } else if (decryptedMasterKey != oldMasterKey.text.toString() && newMasterKey.text.toString() != againNewMasterKey.text.toString()) {
                oldMasterKeyLayout.error = getString(R.string.wrong_master_key)
                oldMasterKeyLayout.boxStrokeErrorColor
                newMasterKeyLayout.boxStrokeErrorColor
                againNewMasterKeyLayout.boxStrokeErrorColor
                againNewMasterKeyLayout.error = getString(R.string.masterkeys_dont_match)

            } else if (decryptedMasterKey != oldMasterKey.text.toString() && newMasterKey.text.toString() == againNewMasterKey.text.toString()) {
                oldMasterKeyLayout.error = getString(R.string.wrong_master_key)
                oldMasterKeyLayout.boxStrokeErrorColor
                newMasterKeyLayout.error = null
                newMasterKeyLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                newMasterKeyLayout.hintTextColor =
                    getColorStateList(R.color.md_theme_onPrimaryContainer)
                againNewMasterKeyLayout.error = null
                againNewMasterKeyLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                againNewMasterKeyLayout.hintTextColor =
                    getColorStateList(R.color.md_theme_onPrimaryContainer)

            } else if (newMasterKey.text.toString() != againNewMasterKey.text.toString() && decryptedMasterKey == oldMasterKey.text.toString()) {
                newMasterKeyLayout.boxStrokeErrorColor
                againNewMasterKeyLayout.boxStrokeErrorColor
                againNewMasterKeyLayout.error = getString(R.string.masterkeys_dont_match)
                oldMasterKeyLayout.error = null
                oldMasterKeyLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                oldMasterKeyLayout.hintTextColor =
                    getColorStateList(R.color.md_theme_onPrimaryContainer)
            } else if (decryptedMasterKey == oldMasterKey.text.toString() && newMasterKey.text.toString() == againNewMasterKey.text.toString()
            ) {
                var arrayOfPassword: ArrayList<Password>

                MyEncryptedSharedPreferences.initialize(this, decryptedMasterKey)
                var preferences: EncryptedSharedPreferences
                val arrayOfEncrypt = ArrayList<String>()
                CoroutineScope(Dispatchers.IO).launch {
                    arrayOfPassword =
                        AppDatabase.getDatabase(this@MasterKeyEditActivity).passwordDao()
                            .getAllPasswords() as ArrayList<Password>
                    MyEncryptedSharedPreferences.initialize(
                        this@MasterKeyEditActivity,
                        decryptedMasterKey
                    )
                    preferences = MyEncryptedSharedPreferences.getEncryptedSharedPreferences()
                    for (i in arrayOfPassword.indices) {
                        val password = arrayOfPassword[i]
                        arrayOfEncrypt.add(
                            preferences.getString(
                                "${password.site} ${password.login}".hashCode().toString(), ""
                            ).toString()
                        )
                    }

                    withContext(Dispatchers.Main) {
                        val MasterKeyNew = newMasterKey.text.toString()
                        MyEncryptedSharedPreferences.updateMasterKey(
                            this@MasterKeyEditActivity,
                            MasterKeyNew
                        )
                        val newPreferences =
                            MyEncryptedSharedPreferences.getEncryptedSharedPreferences()
                        for (i in arrayOfPassword.indices) {
                            val password = arrayOfPassword[i]
                            newPreferences.edit().putString(
                                "${password.site} ${password.login}".hashCode().toString(),
                                arrayOfEncrypt[i]
                            ).apply()
                        }
                        KeystoreManager.saveEncryptedString(
                            this@MasterKeyEditActivity,
                            newMasterKey.text.toString()
                        )
                        oldMasterKeyLayout.error = null
                        oldMasterKeyLayout.boxStrokeColor =
                            getColor(R.color.md_theme_primaryContainer)
                        oldMasterKeyLayout.hintTextColor =
                            getColorStateList(R.color.md_theme_onPrimaryContainer)
                        newMasterKeyLayout.error = null
                        newMasterKeyLayout.boxStrokeColor =
                            getColor(R.color.md_theme_primaryContainer)
                        newMasterKeyLayout.hintTextColor =
                            getColorStateList(R.color.md_theme_onPrimaryContainer)
                        againNewMasterKeyLayout.error = null
                        againNewMasterKeyLayout.boxStrokeColor =
                            getColor(R.color.md_theme_primaryContainer)
                        againNewMasterKeyLayout.hintTextColor =
                            getColorStateList(R.color.md_theme_onPrimaryContainer)
                        finish()
                    }
                }
            }

        }
    }
}