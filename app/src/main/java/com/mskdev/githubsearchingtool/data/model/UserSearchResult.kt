package com.mskdev.githubsearchingtool.data.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize
import java.io.Serializable

/*
Step:1
Here we define our user data class that we get from the search result of github api
 */
@Parcelize
data class UserSearchResult(
    @Json(name = "login") val userName: String,
    @Json(name = "html_url") val userUrl: String,
    @Json(name = "avatar_url") val avatarUrl: String,
    @Json(name = "repos_url") val reposUrl: String
): Parcelable{

}
