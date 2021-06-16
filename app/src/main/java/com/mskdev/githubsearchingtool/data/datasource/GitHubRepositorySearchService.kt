package com.mskdev.githubsearchingtool.data.datasource

import com.mskdev.githubsearchingtool.data.model.RepositorySearchResult
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface GitHubRepositorySearchService {
    @GET("/search/repositories")
    suspend fun getRepositoryListData(@Query("q") q: String?, @Query("sort") sort: String?, @Query("page") page: String?): Response<RepositorySearchResult>
}