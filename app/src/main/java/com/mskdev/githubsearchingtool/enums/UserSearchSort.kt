package com.mskdev.githubsearchingtool.enums

enum class UserSearchSort (val value: String){
    BEST_MATCH("best match"),
    FOLLOWERS("followers"),
    REPOSITORIES("repositories");

    companion object {
        fun findByName(name: String?) = when (name) {
            BEST_MATCH.value -> BEST_MATCH
            FOLLOWERS.value -> FOLLOWERS
            REPOSITORIES.value -> REPOSITORIES
            else -> BEST_MATCH
        }
    }

}