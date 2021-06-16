package com.mskdev.githubsearchingtool.data.datasource

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mskdev.githubsearchingtool.data.model.LocalUser

@Dao
interface UserDao {
    @Query("SELECT * from  users")
    fun getAll(): List<LocalUser>

    @Insert
    suspend fun insertUser(user: LocalUser)

    @Query("DELETE from users")
    suspend fun deleteAll()
}