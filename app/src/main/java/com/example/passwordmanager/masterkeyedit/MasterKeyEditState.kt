package com.example.passwordmanager.masterkeyedit

data class MasterKeyEditState(
    val oldMasterKey: MasterKeyInputFieldState,
    val newMasterKey: MasterKeyInputFieldState,
    val againNewMasterKey: MasterKeyInputFieldState
) {
    sealed interface MasterKeyInputFieldState {
        data object Default : MasterKeyInputFieldState
        data class Error(val messageRes: Int) : MasterKeyInputFieldState
    }

}


