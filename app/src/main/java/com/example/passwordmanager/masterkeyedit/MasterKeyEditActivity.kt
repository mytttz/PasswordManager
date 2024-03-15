package com.example.passwordmanager.masterkeyedit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.passwordmanager.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

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
        toolbar = findViewById(R.id.topAppBar)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        val viewModel = MasterKeyEditViewModel()

        viewModel.liveData.observe(this) { state ->
            listOf(
                state.oldMasterKey to oldMasterKeyLayout,
                state.newMasterKey to newMasterKeyLayout,
                state.againNewMasterKey to againNewMasterKeyLayout
            ).forEach { (fieldState, layout) ->
                when (fieldState) {
                    is MasterKeyEditState.MasterKeyInputFieldState.Default -> {
                        layout.error = null
                        layout.boxStrokeColor = getColor(R.color.md_theme_primaryContainer)
                        layout.hintTextColor =
                            getColorStateList(R.color.md_theme_onPrimaryContainer)
                    }

                    is MasterKeyEditState.MasterKeyInputFieldState.Error -> {
                        layout.error = resources.getString(fieldState.messageRes)
                        layout.boxStrokeErrorColor
                    }
                }
            }
        }



        confirmButton.setOnClickListener {
            viewModel.masterKeyEdit(
                this,
                oldMasterKey.text.toString(),
                newMasterKey.text.toString(),
                againNewMasterKey.text.toString()
            )
        }
    }
}