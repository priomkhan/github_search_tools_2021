package com.mskdev.githubsearchingtool.data.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class RepositorySearchResult(
    @Json(name = "total_count") val totalCount: Long,
    @Json(name = "items") val repositoryItems: List<RepositoryItem>): Parcelable

@Parcelize
data class RepositoryItem (
    val id: Long,

    @Json(name = "full_name") val fullName: String?,

    val owner: Owner,

    @Json(name = "html_url") val htmlURL: String?,

    val description: String?,

    @Json(name = "languages_url") val languagesURL: String?,

    @Json(name = "stargazers_url") val stargazersURL: String?,

    @Json(name = "created_at") val createdAt: String?,

    @Json(name = "updated_at") val updatedAt: String?,

    @Json(name = "git_url") val gitURL: String?,

    @Json(name = "stargazers_count") val stargazersCount: Long,

    @Json(name = "watchers_count") val watchersCount: Long,

    @Json(name = "language")  val language: String?,

    @Json(name = "forks_count") val forksCount: Long,

    @Json(name = "topics") val topics: List<String>?,
): Parcelable

@Parcelize
data class Owner (
    val id: Long,

    val login: String,

    @Json(name = "avatar_url") val avatarURL: String,

    val url: String,

    @Json(name = "html_url") val htmlURL: String,

): Parcelable
