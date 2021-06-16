package com.mskdev.githubsearchingtool.data.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
class UserRepo(@Json(name="name") val repoName: String?,
               val forks_count: Int,
               val stargazers_count: Int,
               val html_url: String): Parcelable {

}
