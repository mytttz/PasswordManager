package com.example.passwordmanager.passwordlist

sealed interface PasswordListState {
    data class Content(val items: List<PasswordItemState>) : PasswordListState
    data object Initial : PasswordListState
}