package com.example.passwordmanager.welcome

data class WelcomeState(
    val masterKeyInputState: MasterKeyInputState,
    val fingerPrintAvailable: Boolean
) {
    sealed interface MasterKeyInputState {
        data class Default(val masterKeyHintRes: Int) : MasterKeyInputState
        data object Error : MasterKeyInputState
    }
}