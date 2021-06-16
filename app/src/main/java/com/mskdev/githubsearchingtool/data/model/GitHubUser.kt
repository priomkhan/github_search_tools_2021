package com.mskdev.githubsearchingtool.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class GitHubUser(val userSearchResult: UserSearchResult?,
                      val userDetails: UserDetails?,
                      val userRepoList: List<UserRepo>?) : Parcelable {
}