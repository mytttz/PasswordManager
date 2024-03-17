package com.example.passwordmanager.createoredit

import com.example.passwordmanager.KeystoreManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.passwordmanager.MyEncryptedSharedPreferences
import com.example.passwordmanager.R
import com.example.passwordmanager.database.AppDatabase
import com.example.passwordmanager.database.Password
import com.example.passwordmanager.masterkeyedit.MasterKeyEditState
import com.example.passwordmanager.passwordlist.PasswordListActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateOrEditPasswordViewModel(passwordItem: Password?, password: String) {

    private val _liveData: MutableLiveData<CreateOrEditPasswordActivityState>

    val liveData: LiveData<CreateOrEditPasswordActivityState>
        get() = _liveData
    private val liveDataValue: CreateOrEditPasswordActivityState
        get() = liveData.value!!

    init {
        _liveData = MutableLiveData(
            CreateOrEditPasswordActivityState(
                editSite = CreateOrEditPasswordActivityState.InputFieldState.Initial(
                    passwordItem?.site ?: ""
                ),
                editLogin = CreateOrEditPasswordActivityState.InputFieldState.Initial(
                    passwordItem?.login ?: ""
                ),
                editPassword = CreateOrEditPasswordActivityState.InputFieldState.Initial(password)
            )
        )
    }

    private fun createPassword(
        context: Context,
        editSite: String,
        editLogin: String,
        editPassword: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val newPass =
                Password(
                    site = editSite,
                    login = editLogin
                )
            AppDatabase.getDatabase(context).passwordDao()
                .insertPassword(newPass)
        }
        writeDataAndBack(context, editSite, editLogin, editPassword)
    }


    private fun editPassword(
        context: Context,
        id: Long,
        editSite: String,
        editLogin: String,
        editPassword: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val editPass =
                Password(
                    id = id,
                    site = editSite,
                    login = editLogin
                )
            AppDatabase.getDatabase(context).passwordDao()
                .updatePassword(editPass)
        }
        writeDataAndBack(context, editSite, editLogin, editPassword)
    }

    private fun writeDataAndBack(
        context: Context,
        editSite: String,
        editLogin: String,
        editPassword: String
    ) {
        MyEncryptedSharedPreferences.initialize(
            context,
            KeystoreManager.getDecryptedString(context).toString()
        )
        val encryptedSharedPreferences =
            MyEncryptedSharedPreferences.getEncryptedSharedPreferences()

        encryptedSharedPreferences.edit().putString(
            "$editSite $editLogin".hashCode()
                .toString(),
            editPassword
        ).apply()
        _liveData.value =
            liveDataValue.copy(
                editSite = CreateOrEditPasswordActivityState.InputFieldState.Default,
                editLogin = CreateOrEditPasswordActivityState.InputFieldState.Default,
                editPassword = CreateOrEditPasswordActivityState.InputFieldState.Default
            )
        val intentBack = Intent(context, PasswordListActivity::class.java)
        context.startActivity(intentBack)
    }


    fun deletePassword(context: Context, id: Long, editSite: String, editLogin: String) {
        MyEncryptedSharedPreferences.initialize(
            context,
            KeystoreManager.getDecryptedString(context).toString()
        )
        val encryptedSharedPreferences =
            MyEncryptedSharedPreferences.getEncryptedSharedPreferences()

        CoroutineScope(Dispatchers.IO).launch {
            AppDatabase.getDatabase(context).passwordDao()
                .deletePasswordById(id)
        }
        encryptedSharedPreferences.edit()
            .remove(
                "$editSite $editLogin".hashCode().toString()
            ).apply()
        val intentBack = Intent(context, PasswordListActivity::class.java)
        context.startActivity(intentBack)
    }

    fun createOrEdit(
        context: Context,
        id: Long,
        editSite: String,
        editLogin: String,
        editPassword: String
    ) {
        val siteState = when {
            editSite.isBlank() -> CreateOrEditPasswordActivityState.InputFieldState.Error
            else -> CreateOrEditPasswordActivityState.InputFieldState.Default
        }

        val loginState = when {
            editLogin.isBlank() -> CreateOrEditPasswordActivityState.InputFieldState.Error
            else -> CreateOrEditPasswordActivityState.InputFieldState.Default
        }

        val passwordState = when {
            editPassword.isBlank() -> CreateOrEditPasswordActivityState.InputFieldState.Error
            else -> CreateOrEditPasswordActivityState.InputFieldState.Default
        }


        _liveData.value = liveDataValue.copy(
            editSite = siteState,
            editLogin = loginState,
            editPassword = passwordState
        )
        val isNotEmpty =
            editSite.isNotBlank() && editLogin.isNotBlank() && editPassword.isNotBlank()
        if (isNotEmpty) {
            if (id != -1L) {
                editPassword(
                    context,
                    id,
                    editSite,
                    editLogin,
                    editPassword
                )
            } else {
                createPassword(
                    context,
                    editSite,
                    editLogin,
                    editPassword
                )
            }
        }
    }
}