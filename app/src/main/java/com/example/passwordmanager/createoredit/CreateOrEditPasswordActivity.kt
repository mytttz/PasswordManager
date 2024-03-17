package com.example.passwordmanager.createoredit

import com.example.passwordmanager.KeystoreManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.passwordmanager.MyEncryptedSharedPreferences
import com.example.passwordmanager.R
import com.example.passwordmanager.database.AppDatabase
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
        deleteButton = findViewById(R.id.deleteButton)
        toolbar = findViewById(R.id.topAppBar)
        val id = intent.getLongExtra("EXTRA_ID", -1)
        if (intent.getIntExtra("invisible", 0) == 1) {
            deleteButton.visibility = View.GONE
        } else {
            toolbar.title = resources.getString(R.string.edit_note)
        }
        editSite = findViewById(R.id.editSite)
        editLogin = findViewById(R.id.editLogin)
        editPassword = findViewById(R.id.editPass)
        saveButton = findViewById(R.id.saveButton)

        setSupportActionBar(toolbar)

        toolbar.setNavigationOnClickListener {
            finish()
        }
        var viewModel: CreateOrEditPasswordViewModel? = null

        CoroutineScope(Dispatchers.IO).launch {
            val editPassId =
                AppDatabase.getDatabase(this@CreateOrEditPasswordActivity).passwordDao()
                    .getPasswordById(id)
            MyEncryptedSharedPreferences.initialize(
                this@CreateOrEditPasswordActivity,
                KeystoreManager.getDecryptedString(this@CreateOrEditPasswordActivity).toString()
            )
            val encryptedSharedPreferences =
                MyEncryptedSharedPreferences.getEncryptedSharedPreferences()

            withContext(Dispatchers.Main) {
                viewModel = CreateOrEditPasswordViewModel(
                    editPassId, encryptedSharedPreferences.getString(
                        "${editPassId?.site} ${editPassId?.login}".hashCode().toString(), ""
                    ).toString()
                )
                viewModel?.liveData?.observe(this@CreateOrEditPasswordActivity) { state ->
                    handleInputFieldState(state.editSite, editSiteLayout, editSite)
                    handleInputFieldState(state.editLogin, editLoginLayout, editLogin)
                    handleInputFieldState(state.editPassword, editPasswordLayout, editPassword)
                }
            }
        }

        saveButton.setOnClickListener {
            viewModel?.createOrEdit(
                this,
                id,
                editSite.text.toString().trim(),
                editLogin.text.toString().trim(),
                editPassword.text.toString()
            )
        }

        deleteButton.setOnClickListener {
            viewModel?.deletePassword(
                this,
                id,
                editSite.text.toString(),
                editLogin.text.toString()
            )
        }
    }

    private fun handleInputFieldState(
        fieldState: CreateOrEditPasswordActivityState.InputFieldState,
        layout: TextInputLayout,
        editText: TextInputEditText
    ) {
        when (fieldState) {
            is CreateOrEditPasswordActivityState.InputFieldState.Default -> {
                layout.error = null
                layout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                layout.hintTextColor = getColorStateList(R.color.md_theme_onPrimaryContainer)
            }

            is CreateOrEditPasswordActivityState.InputFieldState.Error -> {
                layout.error = resources.getString(R.string.empty_field)
                layout.boxStrokeErrorColor
            }

            is CreateOrEditPasswordActivityState.InputFieldState.Initial -> {
                editText.setText(fieldState.initialText)
            }
        }
    }
}
