package com.mskdev.githubsearchingtool.data.datasource

import com.mskdev.githubsearchingtool.data.model.UserSearchList
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GitHubUserSearchService {

        //calling "/search/user?q=user"
        @GET("/search/users")
        suspend fun getUserSearchData(@Query("q") q: String?, @Query("sort") sort: String?): Response<UserSearchList>


}