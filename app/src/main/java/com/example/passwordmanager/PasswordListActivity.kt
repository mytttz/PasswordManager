package com.example.passwordmanager

import KeystoreManager
import PasswordAdapter
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.security.crypto.EncryptedSharedPreferences
import com.example.passwordmanager.database.AppDatabase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PasswordListActivity : AppCompatActivity() {
    private lateinit var passwordList: RecyclerView
    private lateinit var dbPassword: AppDatabase

    //    private lateinit var topAppBarLayout: AppBarLayout
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var addButton: FloatingActionButton
    private lateinit var sharedPreferences: EncryptedSharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val decryptedMasterKey = KeystoreManager.getDecryptedString(this).toString()
        MyEncryptedSharedPreferences.initialize(this, decryptedMasterKey)
        sharedPreferences = MyEncryptedSharedPreferences.getEncryptedSharedPreferences()
//        topAppBarLayout = findViewById(R.id.topAppBarLayout)
        setContentView(R.layout.activity_password_list)
        passwordList = findViewById(R.id.passwordList)
        topAppBar = findViewById(R.id.topAppBar)
        addButton = findViewById(R.id.addButton)
        dbPassword = AppDatabase.getDatabase(this)
        val rootView = findViewById<ViewGroup>(android.R.id.content)

        CoroutineScope(Dispatchers.IO).launch {

            val itemsList = dbPassword.passwordDao().getAllPasswords()
            withContext(Dispatchers.Main) {
                val adapter =
                    PasswordAdapter(
                        this@PasswordListActivity,
                        itemsList,
                        decryptedMasterKey,
                        rootView
                    )
                passwordList.adapter = adapter
                passwordList.layoutManager =
                    object : LinearLayoutManager(this@PasswordListActivity) {
                        override fun calculateExtraLayoutSpace(
                            state: RecyclerView.State,
                            extraLayoutSpace: IntArray,
                        ) { //// фикисит анимацию появления последнего элемента при сворачивании карточки
                            val extraSpace =
                                (Resources.getSystem().displayMetrics.density * 160).toInt()
                            extraLayoutSpace[1] = extraSpace
                            extraLayoutSpace[0] = extraSpace
                        }
                    }
            }
        }

        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.deleteAllData -> {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Deletion confirmation")
                        .setMessage("Are you sure you want to delete all data? This action is irreversible")
                        .setNegativeButton("Cancel") { dialog, which ->
                            dialog.dismiss()
                        }
                        .setPositiveButton("Delete all data") { dialog, which ->
                            CoroutineScope(Dispatchers.IO).launch {
                                AppDatabase.getDatabase(this@PasswordListActivity).passwordDao()
                                    .deleteAllPasswords()
                                MyEncryptedSharedPreferences.initialize(
                                    this@PasswordListActivity,
                                    decryptedMasterKey
                                )
                                val preferences =
                                    MyEncryptedSharedPreferences.getEncryptedSharedPreferences()
                                preferences.edit().clear().apply()
                                withContext(Dispatchers.Main) {
                                    (passwordList.adapter as PasswordAdapter).setItems(emptyList())
                                }
                            }
                        }
                        .show()
                    true
                }

                R.id.editMasterKey -> {
                    val intent = Intent(this, MasterKeyEditActivity::class.java)
                    startActivity(intent)
                    true
                }

                else -> false
            }
        }

        addButton.setOnClickListener {
            val intent = Intent(this, CreateOrEditPasswordActivity::class.java)
            intent.putExtra("invisible", 1)
            startActivity(intent)
        }

    }
}

