package com.mskdev.githubsearchingtool.data.model

import com.mskdev.githubsearchingtool.enums.UserSearchSort

data class UserSearchQuery(var query: String="", var sort: UserSearchSort=UserSearchSort.BEST_MATCH, var token: String?="")
