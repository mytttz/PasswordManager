package com.example.passwordmanager.passwordlist

import KeystoreManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.view.MenuItem
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.passwordmanager.MyEncryptedSharedPreferences
import com.example.passwordmanager.R
import com.example.passwordmanager.createoredit.CreateOrEditPasswordActivity
import com.example.passwordmanager.database.AppDatabase
import com.example.passwordmanager.masterkeyedit.MasterKeyEditActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PasswordListViewModel(
    context: Context
) : ViewModel() {
    private val _livedata = MutableLiveData<PasswordListState>(
        PasswordListState.Initial
    )

    val liveData: LiveData<PasswordListState>
        get() = _livedata

    private val liveDataValue: PasswordListState
        get() = liveData.value!!

    init {
        CoroutineScope(Dispatchers.IO).launch {
            val listOfPasswords =
                AppDatabase.getDatabase(context).passwordDao()
                    .getAllPasswords()

            withContext(Dispatchers.Main) {

                val itemState = mutableListOf<PasswordItemState>()
                for (i in listOfPasswords) {
                    val encryptedSharedPreferences =
                        MyEncryptedSharedPreferences.getEncryptedSharedPreferences()
                    val key =
                        "${i.site} ${i.login}".hashCode()
                            .toString()
                    val password = encryptedSharedPreferences.getString(key, "").toString()
                    itemState.add(PasswordItemState(i.id, i.site, i.login, password))
                }
                _livedata.value = PasswordListState.Content(itemState)
            }
        }
    }

    fun menuSelected(context: Context, menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.deleteAllData -> {
                MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.deletion_confirmation)
                    .setMessage(R.string.are_you_sure_you_want_to_delete_all_data_this_action_is_irreversible)
                    .setNegativeButton(R.string.cancel) { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setPositiveButton(R.string.delete_all_data) { _, _ ->
                        CoroutineScope(Dispatchers.IO).launch {
                            AppDatabase.getDatabase(context).passwordDao()
                                .deleteAllPasswords()

                            withContext(Dispatchers.Main) {
                                val decryptedMasterKey =
                                    KeystoreManager.getDecryptedString(context).toString()
                                MyEncryptedSharedPreferences.initialize(
                                    context,
                                    decryptedMasterKey
                                )
                                val encryptedSharedPreferences =
                                    MyEncryptedSharedPreferences.getEncryptedSharedPreferences()
                                encryptedSharedPreferences.edit().clear().apply()
                                val newState = when (val oldState = liveDataValue) {
                                    is PasswordListState.Initial -> PasswordListState.Content(
                                        emptyList()
                                    )

                                    is PasswordListState.Content -> oldState.copy(
                                        items = emptyList()
                                    )
                                }
                                _livedata.value = newState
                            }
                        }
                    }
                    .show()
                return true
            }

            R.id.editMasterKey -> {
                val intent = Intent(context, MasterKeyEditActivity::class.java)
                context.startActivity(intent)
                return true
            }

            else -> return false
        }
    }

    fun add(context: Context) {
        val intentAdd = Intent(context, CreateOrEditPasswordActivity::class.java)
        intentAdd.putExtra("invisible", 1)
        context.startActivity(intentAdd)
    }

    fun copyPassword(context: Context, item: String) {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", item)
        clipboardManager.setPrimaryClip(clipData)
        Toast.makeText(
            context,
            R.string.text_copied_to_clipboard, Toast.LENGTH_SHORT
        ).show()
    }

    fun editPassword(context: Context, id: Long) {
        val intentEdit = Intent(context, CreateOrEditPasswordActivity::class.java)
        intentEdit.putExtra("EXTRA_ID", id)
        context.startActivity(intentEdit)
    }
}