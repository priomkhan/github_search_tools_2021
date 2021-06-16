package com.mskdev.githubsearchingtool.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class LocalUser(
                    @PrimaryKey(autoGenerate = true)
                    val userId: Int,
                    val userName: String,
                    val userUID: String,
                    val userProfileImage : String) {
}