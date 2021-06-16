package com.mskdev.githubsearchingtool.enums

enum class RepositorySearchSort(val value: String) {
    BEST_MATCH("best match"),
    STARS("stars"),
    FORKS("forks");

    companion object {
        fun findByName(name: String?) = when (name) {
            BEST_MATCH.value -> BEST_MATCH
            STARS.value -> STARS
            FORKS.value -> FORKS
            else -> BEST_MATCH
        }
    }

}