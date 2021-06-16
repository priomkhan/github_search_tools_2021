package com.mskdev.githubsearchingtool.utilities


class Global {
    companion object{
        var isNetworkConnected: Boolean = false
        const val LOG_TAG = "GitHubSearchTools" //Global Log ID.

        const val GITHUB_SERVICE_URL = "https://api.github.com"  // constant that is the location of the web service
    }
}
