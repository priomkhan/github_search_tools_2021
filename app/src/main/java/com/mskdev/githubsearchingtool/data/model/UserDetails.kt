package com.mskdev.githubsearchingtool.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.util.*
@Parcelize
data class UserDetails(
    val name : String?,
    val email: String?,
    val blog: String?,
    val location: String?,
    val created_at : String?,
    val bio : String?,
    val followers :Int,
    val following : Int,
    val public_repos: Int): Parcelable {

}
