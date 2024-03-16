package com.example.passwordmanager.passwordlist

data class PasswordItemState(
    val id: Long,
    val itemSite: String,
    val itemLogin: String,
    val itemPass: String
)