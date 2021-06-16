package com.mskdev.githubsearchingtool.data.model

import com.mskdev.githubsearchingtool.enums.RepositorySearchSort


data class RepositorySearchQuery(var query: String="", var sort: RepositorySearchSort = RepositorySearchSort.BEST_MATCH, var page : Int=1, var token: String?="")
