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
        val matchMasterKeys =
            decryptedMasterKey == oldMasterKey && newMasterKey == againNewMasterKey && newMasterKey == oldMasterKey
        val masterkeysDontMatch = wrongOldMasterKey && wrongRepeatNewMasterKey

        val oldMasterKeyState: MasterKeyEditState.MasterKeyInputFieldState =
            if (oldMasterKey.isBlank()) {
                MasterKeyEditState.MasterKeyInputFieldState.Error(R.string.empty_field)
            } else if (masterkeysDontMatch || wrongOldMasterKey) {
                MasterKeyEditState.MasterKeyInputFieldState.Error(R.string.wrong_master_key)
            } else {
                MasterKeyEditState.MasterKeyInputFieldState.Default
            }

        val newMasterKeyState: MasterKeyEditState.MasterKeyInputFieldState =
            if (newMasterKey.isBlank()) {
                MasterKeyEditState.MasterKeyInputFieldState.Error(R.string.empty_field)
            } else if (masterkeysDontMatch || wrongRepeatNewMasterKey) {
                MasterKeyEditState.MasterKeyInputFieldState.Error(R.string.masterkeys_dont_match)
            } else {
                MasterKeyEditState.MasterKeyInputFieldState.Default
            }

        val againNewMasterKeyState: MasterKeyEditState.MasterKeyInputFieldState =
            if (againNewMasterKey.isBlank()) {
                MasterKeyEditState.MasterKeyInputFieldState.Error(R.string.empty_field)
            } else if (masterkeysDontMatch || wrongRepeatNewMasterKey) {
                MasterKeyEditState.MasterKeyInputFieldState.Error(R.string.masterkeys_dont_match)
            } else if (matchMasterKeys) {
                MasterKeyEditState.MasterKeyInputFieldState.Error(R.string.masterkeys_match)
            } else {
                MasterKeyEditState.MasterKeyInputFieldState.Default
            }

        _livedata.value = liveDataValue.copy(
            oldMasterKey = oldMasterKeyState,
            newMasterKey = newMasterKeyState,
            againNewMasterKey = againNewMasterKeyState
        )

        if (!wrongOldMasterKey && !wrongRepeatNewMasterKey && !matchMasterKeys) {
            var arrayOfPassword: ArrayList<Password>

            MyEncryptedSharedPreferences.initialize(context, decryptedMasterKey)
            var encryptedSharedPreferences: EncryptedSharedPreferences
            val arrayOfEncrypt = ArrayList<String>()
            CoroutineScope(Dispatchers.IO).launch {
                arrayOfPassword =
                    AppDatabase.getDatabase(context).passwordDao()
                        .getAllPasswords() as ArrayList<Password>
                MyEncryptedSharedPreferences.initialize(
                    context,
                    decryptedMasterKey
                )
                encryptedSharedPreferences =
                    MyEncryptedSharedPreferences.getEncryptedSharedPreferences()
                for (i in arrayOfPassword.indices) {
                    val password = arrayOfPassword[i]
                    arrayOfEncrypt.add(
                        encryptedSharedPreferences.getString(
                            "${password.site} ${password.login}".hashCode().toString(), ""
                        ).toString()
                    )
                }

                withContext(Dispatchers.Main) {
                    MyEncryptedSharedPreferences.updateMasterKey(
                        context,
                        newMasterKey
                    )
                    val newEncryptedSharedPreferences =
                        MyEncryptedSharedPreferences.getEncryptedSharedPreferences()
                    for (i in arrayOfPassword.indices) {
                        val password = arrayOfPassword[i]
                        newEncryptedSharedPreferences.edit().putString(
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
                    val intentBack = Intent(context, PasswordListActivity::class.java)
                    context.startActivity(intentBack)
                }
            }
        }
    }
}
