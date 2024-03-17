package com.example.passwordmanager.passwordlist

import com.example.passwordmanager.KeystoreManager
import PasswordAdapter
import android.content.res.Resources
import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.passwordmanager.MyEncryptedSharedPreferences
import com.example.passwordmanager.R
import com.example.passwordmanager.database.AppDatabase
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PasswordListActivity : AppCompatActivity() {
    private lateinit var passwordListRecycler: RecyclerView
    private lateinit var dbPassword: AppDatabase
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var addButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyEncryptedSharedPreferences.initialize(
            this,
            KeystoreManager.getDecryptedString(this).toString()
        )
        setContentView(R.layout.activity_password_list)
        passwordListRecycler = findViewById(R.id.passwordList)
        topAppBar = findViewById(R.id.topAppBar)
        addButton = findViewById(R.id.addButton)
        dbPassword = AppDatabase.getDatabase(this)
        val rootView = findViewById<ViewGroup>(android.R.id.content)
        val viewModel = PasswordListViewModel(applicationContext)
        val adapter =
            PasswordAdapter(
                this,
                rootView,
                viewModel
            )
        passwordListRecycler.adapter = adapter
        passwordListRecycler.layoutManager =
            object : LinearLayoutManager(this) {
                override fun calculateExtraLayoutSpace(
                    state: RecyclerView.State,
                    extraLayoutSpace: IntArray,
                ) { // фикисит анимацию появления последнего элемента при сворачивании карточки
                    val extraSpace =
                        (Resources.getSystem().displayMetrics.density * 160).toInt()
                    extraLayoutSpace[1] = extraSpace
                    extraLayoutSpace[0] = extraSpace
                }
            }

        viewModel.liveData.observe(this) { state ->
            val items = when (state) {
                is PasswordListState.Initial -> emptyList()
                is PasswordListState.Content -> state.items
            }
            adapter.submitList(items)
        }
        topAppBar.setOnMenuItemClickListener { menuItem ->
            viewModel.menuSelected(this, menuItem)
        }
        addButton.setOnClickListener {
            viewModel.add(this)
        }
    }
}