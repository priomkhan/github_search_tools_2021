package com.mskdev.githubsearchingtool.data.datasource

import com.mskdev.githubsearchingtool.data.model.UserDetails
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubUserDetailsService {


    @GET("/users/{userName}")
    suspend fun getUserDetailsData(@Path("userName") userName: String? ): Response<UserDetails>
}