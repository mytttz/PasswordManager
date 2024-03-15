package com.example.passwordmanager.masterkeyedit

import KeystoreManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.security.crypto.EncryptedSharedPreferences
import com.example.passwordmanager.MyEncryptedSharedPreferences
import com.example.passwordmanager.R
import com.example.passwordmanager.database.AppDatabase
import com.example.passwordmanager.database.Password
import com.example.passwordmanager.passwordlist.PasswordListActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MasterKeyEditViewModel {


    private val _livedata =
        MutableLiveData(
            MasterKeyEditState(
                oldMasterKey = MasterKeyEditState.MasterKeyInputFieldState.Default,
                newMasterKey = MasterKeyEditState.MasterKeyInputFieldState.Default,
                againNewMasterKey = MasterKeyEditState.MasterKeyInputFieldState.Default
            )
        )
    val liveData: LiveData<MasterKeyEditState>
        get() = _livedata
    private val liveDataValue: MasterKeyEditState
        get() = liveData.value!!


    fun masterKeyEdit(
        context: Context,
        oldMasterKey: String,
        newMasterKey: String,
        againNewMasterKey: String
    ) {
        val decryptedMasterKey = KeystoreManager.getDecryptedString(context).toString()
        val wrongOldMasterKey = decryptedMasterKey != oldMasterKey
        val wrongRepeatNewMasterKey = newMasterKey != againNewMasterKey
        val isEmptyField =
            oldMasterKey.isBlank() || newMasterKey.isBlank() || againNewMasterKey.isBlank()
        var oldMasterKeyState: MasterKeyEditState.MasterKeyInputFieldState =
            MasterKeyEditState.MasterKeyInputFieldState.Default
        var newMasterKeyState: MasterKeyEditState.MasterKeyInputFieldState =
            MasterKeyEditState.MasterKeyInputFieldState.Default
        var againNewMasterKeyState: MasterKeyEditState.MasterKeyInputFieldState =
            MasterKeyEditState.MasterKeyInputFieldState.Default
        val errorMessageResId = when {
            isEmptyField -> R.string.empty_field
            wrongOldMasterKey && wrongRepeatNewMasterKey -> -1
            wrongOldMasterKey -> R.string.wrong_master_key
            wrongRepeatNewMasterKey -> R.string.masterkeys_dont_match
            else -> R.string.masterkeys_match
        }
        oldMasterKeyState = if (errorMessageResId == R.string.empty_field) {
            MasterKeyEditState.MasterKeyInputFieldState.Error(R.string.empty_field)
        } else if (errorMessageResId == -1 || wrongOldMasterKey) {
            MasterKeyEditState.MasterKeyInputFieldState.Error(R.string.wrong_master_key)
        } else {
            MasterKeyEditState.MasterKeyInputFieldState.Default
        }

        newMasterKeyState = if (errorMessageResId == R.string.empty_field) {
            MasterKeyEditState.MasterKeyInputFieldState.Error(R.string.empty_field)
        } else if (errorMessageResId == -1 || wrongRepeatNewMasterKey) {
            MasterKeyEditState.MasterKeyInputFieldState.Error(R.string.masterkeys_dont_match)
        } else {
            MasterKeyEditState.MasterKeyInputFieldState.Default
        }

        againNewMasterKeyState = if (errorMessageResId == R.string.empty_field) {
            MasterKeyEditState.MasterKeyInputFieldState.Error(R.string.empty_field)
        } else if (errorMessageResId == -1 || wrongRepeatNewMasterKey) {
            MasterKeyEditState.MasterKeyInputFieldState.Error(R.string.masterkeys_dont_match)
        } else {
            MasterKeyEditState.MasterKeyInputFieldState.Default
        }

        _livedata.value = liveDataValue.copy(
            oldMasterKey = oldMasterKeyState,
            newMasterKey = newMasterKeyState,
            againNewMasterKey = againNewMasterKeyState
        )

        if (!wrongOldMasterKey && !wrongRepeatNewMasterKey) {
            var arrayOfPassword: ArrayList<Password>

            MyEncryptedSharedPreferences.initialize(context, decryptedMasterKey)
            var preferences: EncryptedSharedPreferences
            val arrayOfEncrypt = ArrayList<String>()
            CoroutineScope(Dispatchers.IO).launch {
                arrayOfPassword =
                    AppDatabase.getDatabase(context).passwordDao()
                        .getAllPasswords() as ArrayList<Password>
                MyEncryptedSharedPreferences.initialize(
                    context,
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
                    val MasterKeyNew = newMasterKey
                    MyEncryptedSharedPreferences.updateMasterKey(
                        context,
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
                        context,
                        newMasterKey
                    )
                    _livedata.value =
                        liveDataValue.copy(
                            oldMasterKey = MasterKeyEditState.MasterKeyInputFieldState.Default,
                            newMasterKey = MasterKeyEditState.MasterKeyInputFieldState.Default,
                            againNewMasterKey = MasterKeyEditState.MasterKeyInputFieldState.Default
                        )
                    val intent = Intent(context, PasswordListActivity::class.java)
                    context.startActivity(intent)
                }
            }
        }
    }
}
