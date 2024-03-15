package com.example.passwordmanager.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface PasswordDao {
    @Query("SELECT * FROM passwords")
    fun getAllPasswords(): List<Password>

    @Query("SELECT * FROM passwords WHERE id = :id")
    fun getPasswordById(id: Long): Password?

    @Insert
    fun insertPassword(password: Password)

    @Query("DELETE FROM passwords WHERE id = :id")
    fun deletePasswordById(id: Long)

    @Query("DELETE FROM passwords")

    fun deleteAllPasswords()

    @Query("SELECT COUNT(*) FROM passwords")
    fun getPasswordsCount(): Int

    @Update
    fun updatePassword(password: Password)

}