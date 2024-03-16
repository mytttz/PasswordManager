package com.example.passwordmanager.createoredit

data class CreateOrEditPasswordActivityState(
    val editSite: InputFieldState,
    val editLogin: InputFieldState,
    val editPassword: InputFieldState
) {
    sealed interface InputFieldState {
        data object Default : InputFieldState
        data object Error : InputFieldState
        data class Initial(val initialText: String) : InputFieldState
    }
}