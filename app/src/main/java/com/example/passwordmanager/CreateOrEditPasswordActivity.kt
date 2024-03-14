package com.example.passwordmanager

import KeystoreManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.passwordmanager.database.AppDatabase
import com.example.passwordmanager.database.Password
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateOrEditPasswordActivity : AppCompatActivity() {
    private lateinit var editSite: TextInputEditText
    private lateinit var editLogin: TextInputEditText
    private lateinit var editPassword: TextInputEditText
    private lateinit var editSiteLayout: TextInputLayout
    private lateinit var editLoginLayout: TextInputLayout
    private lateinit var editPasswordLayout: TextInputLayout
    private lateinit var toolbar: MaterialToolbar
    private lateinit var saveButton: MaterialButton
    private lateinit var deleteButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_or_edit_password)
        val intent = intent
        editSiteLayout = findViewById(R.id.editSiteLayout)
        editLoginLayout = findViewById(R.id.editLoginLayout)
        editPasswordLayout = findViewById(R.id.editPassLayout)
        val decryptedMasterKey = KeystoreManager.getDecryptedString(this).toString()
        MyEncryptedSharedPreferences.initialize(this, decryptedMasterKey)
        val sharedPreferences = MyEncryptedSharedPreferences.getEncryptedSharedPreferences()
        deleteButton = findViewById(R.id.deleteButton)
        val id = intent.getLongExtra("EXTRA_ID", -1)
        if (intent.getIntExtra("invisible", 0) == 1) {
            deleteButton.visibility = View.GONE
        }
        val intentBack = Intent(this, PasswordListActivity::class.java)
        editSite = findViewById(R.id.editSite)
        editLogin = findViewById(R.id.editLogin)
        editPassword = findViewById(R.id.editPass)
        toolbar = findViewById(R.id.topAppBar)
        setSupportActionBar(toolbar)
        saveButton = findViewById(R.id.saveButton)
        toolbar.setNavigationOnClickListener {
            finish()
        }

        if (id == -1L) {
            saveButton.setOnClickListener {
                if (editSite.text.toString() == "" && editLogin.text.toString() == "" && editPassword.text.toString() == "") {
                    editSiteLayout.error = getString(R.string.empty_field)
                    editSiteLayout.boxStrokeErrorColor
                    editLoginLayout.error = getString(R.string.empty_field)
                    editLoginLayout.boxStrokeErrorColor
                    editPasswordLayout.error = getString(R.string.empty_field)
                    editPasswordLayout.boxStrokeErrorColor
                } else if (editSite.text.toString() == "" && editLogin.text.toString() == "") {
                    editSiteLayout.error = getString(R.string.empty_field)
                    editSiteLayout.boxStrokeErrorColor
                    editLoginLayout.error = getString(R.string.empty_field)
                    editLoginLayout.boxStrokeErrorColor
                    editPasswordLayout.error = null
                    editPasswordLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editPasswordLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    getColorStateList(R.color.md_theme_onPrimaryContainer)
                } else if (editSite.text.toString() == "" && editPassword.text.toString() == "") {
                    editSiteLayout.error = getString(R.string.empty_field)
                    editSiteLayout.boxStrokeErrorColor
                    editPasswordLayout.error = getString(R.string.empty_field)
                    editPasswordLayout.boxStrokeErrorColor
                    editLoginLayout.error = null
                    editLoginLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editLoginLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                } else if (editLogin.text.toString() == "" && editPassword.text.toString() == "") {
                    editLoginLayout.error = getString(R.string.empty_field)
                    editLoginLayout.boxStrokeErrorColor
                    editPasswordLayout.error = getString(R.string.empty_field)
                    editPasswordLayout.boxStrokeErrorColor
                    editSiteLayout.error = null
                    editSiteLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editSiteLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                } else if (editSite.text.toString() == "") {
                    editSiteLayout.error = getString(R.string.empty_field)
                    editSiteLayout.boxStrokeErrorColor
                    editLoginLayout.error = null
                    editLoginLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editLoginLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    editPasswordLayout.error = null
                    editPasswordLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editPasswordLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                } else if (editLogin.text.toString() == "") {
                    editLoginLayout.error = getString(R.string.empty_field)
                    editLoginLayout.boxStrokeErrorColor
                    editSiteLayout.error = null
                    editSiteLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editSiteLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    editPasswordLayout.error = null
                    editPasswordLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editPasswordLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                } else if (editPassword.text.toString() == "") {
                    editPasswordLayout.error = getString(R.string.empty_field)
                    editPasswordLayout.boxStrokeErrorColor
                    editSiteLayout.error = null
                    editSiteLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editSiteLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    editLoginLayout.error = null
                    editLoginLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editLoginLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val newPass =
                            Password(
                                site = editSite.text.toString(),
                                login = editLogin.text.toString()
                            )
                        AppDatabase.getDatabase(this@CreateOrEditPasswordActivity).passwordDao()
                            .insertPassword(newPass)
                    }
                    sharedPreferences.edit().putString(
                        "${editSite.text.toString()} ${editLogin.text.toString()}".hashCode()
                            .toString(),
                        editPassword.text.toString()
                    ).apply()
                    editSiteLayout.error = null
                    editSiteLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editSiteLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    editLoginLayout.error = null
                    editLoginLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editLoginLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    editPasswordLayout.error = null
                    editPasswordLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editPasswordLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    startActivity(intentBack)
                }
            }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                val editPassId =
                    AppDatabase.getDatabase(this@CreateOrEditPasswordActivity).passwordDao()
                        .getPasswordById(id)
                withContext(Dispatchers.Main) {
                    editSite.setText(editPassId?.site)
                    editLogin.setText(editPassId?.login)
                    editPassword.setText(
                        sharedPreferences.getString(
                            "${editPassId?.site.toString()} ${editPassId?.login.toString()}".hashCode()
                                .toString(), ""
                        )
                    )
                }
            }


            saveButton.setOnClickListener {
                if (editSite.text.toString() == "" && editLogin.text.toString() == "" && editPassword.text.toString() == "") {
                    CoroutineScope(Dispatchers.IO).launch {
                        AppDatabase.getDatabase(this@CreateOrEditPasswordActivity).passwordDao()
                            .deletePasswordById(id)
                    }
                    sharedPreferences.edit()
                        .remove(
                            "${editSite.text.toString()} ${editLogin.text.toString()}".hashCode()
                                .toString()
                        ).apply()
                    startActivity(intentBack)
                } else if (editSite.text.toString() == "" && editLogin.text.toString() == "") {
                    editSiteLayout.error = getString(R.string.empty_field)
                    editSiteLayout.boxStrokeErrorColor
                    editLoginLayout.error = getString(R.string.empty_field)
                    editLoginLayout.boxStrokeErrorColor
                    editPasswordLayout.error = null
                    editPasswordLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editPasswordLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    getColorStateList(R.color.md_theme_onPrimaryContainer)
                } else if (editSite.text.toString() == "" && editPassword.text.toString() == "") {
                    editSiteLayout.error = getString(R.string.empty_field)
                    editSiteLayout.boxStrokeErrorColor
                    editPasswordLayout.error = getString(R.string.empty_field)
                    editPasswordLayout.boxStrokeErrorColor
                    editLoginLayout.error = null
                    editLoginLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editLoginLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                } else if (editLogin.text.toString() == "" && editPassword.text.toString() == "") {
                    editLoginLayout.error = getString(R.string.empty_field)
                    editLoginLayout.boxStrokeErrorColor
                    editPasswordLayout.error = getString(R.string.empty_field)
                    editPasswordLayout.boxStrokeErrorColor
                    editSiteLayout.error = null
                    editSiteLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editSiteLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                } else if (editSite.text.toString() == "") {
                    editSiteLayout.error = getString(R.string.empty_field)
                    editSiteLayout.boxStrokeErrorColor
                    editLoginLayout.error = null
                    editLoginLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editLoginLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    editPasswordLayout.error = null
                    editPasswordLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editPasswordLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                } else if (editLogin.text.toString() == "") {
                    editLoginLayout.error = getString(R.string.empty_field)
                    editLoginLayout.boxStrokeErrorColor
                    editSiteLayout.error = null
                    editSiteLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editSiteLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    editPasswordLayout.error = null
                    editPasswordLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editPasswordLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                } else if (editPassword.text.toString() == "") {
                    editPasswordLayout.error = getString(R.string.empty_field)
                    editPasswordLayout.boxStrokeErrorColor
                    editSiteLayout.error = null
                    editSiteLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editSiteLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    editLoginLayout.error = null
                    editLoginLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editLoginLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        val editPass =
                            Password(
                                id = id,
                                site = editSite.text.toString(),
                                login = editLogin.text.toString()
                            )
                        AppDatabase.getDatabase(this@CreateOrEditPasswordActivity).passwordDao()
                            .updatePassword(editPass)
                    }
                    sharedPreferences.edit().putString(
                        "${editSite.text.toString()} ${editLogin.text.toString()}".hashCode()
                            .toString(),
                        editPassword.text.toString()
                    ).apply()
                    editSiteLayout.error = null
                    editSiteLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editSiteLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    editLoginLayout.error = null
                    editLoginLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editLoginLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    editPasswordLayout.error = null
                    editPasswordLayout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                    editPasswordLayout.hintTextColor =
                        getColorStateList(R.color.md_theme_onPrimaryContainer)
                    startActivity(intentBack)
                }
            }
        }


        deleteButton.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getDatabase(this@CreateOrEditPasswordActivity).passwordDao()
                    .deletePasswordById(id)
            }
            sharedPreferences.edit()
                .remove(
                    "${editSite.text.toString()} ${editLogin.text.toString()}".hashCode().toString()
                ).apply()
            startActivity(intentBack)
        }

    }
}
