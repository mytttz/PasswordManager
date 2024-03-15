package com.example.passwordmanager.passwordlist

import android.opengl.Visibility
import com.example.passwordmanager.database.Password

data class PasswordItemState(
    val id: Long,
    val itemSite: String,
    val itemLogin: String,
    val itemPass: String
)