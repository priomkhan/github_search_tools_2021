package com.mskdev.githubsearchingtool.data.datasource

import com.mskdev.githubsearchingtool.data.model.UserRepo
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubUserRepoService {
    @GET("/users/{userName}/repos")
    suspend fun getUserRepos(@Path("userName") userName: String? ): Response<List<UserRepo>>
}