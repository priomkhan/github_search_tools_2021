package com.mskdev.githubsearchingtool.ui.main.fragments.repositorysearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mskdev.githubsearchingtool.data.datasource.GitHubRepository
import com.mskdev.githubsearchingtool.data.model.RepositorySearchQuery
import com.mskdev.githubsearchingtool.utilities.Global
import timber.log.Timber


class RepositorySearchViewModel (private val dataRepo: GitHubRepository): ViewModel(){
    val busyBool = dataRepo.busyBool
    val repositoryOnlineData = dataRepo.searchResultRepositoryList
    val repositoryOnlineMoreData = dataRepo.searchResultRepositoryListMoreData

    private val _userProfileImage = dataRepo.userProfileImage
    val userProfileImage : LiveData<String>
        get() =_userProfileImage


    fun logout(): Boolean{
        return dataRepo.logout()
    }

    fun searchRepository(query : RepositorySearchQuery){
        //Timber.tag(Global.LOG_TAG).d("TopicSearchViewModel: New Query Search: $query")
        dataRepo.refreshRepositoryData(query)
    }

    fun refreshRepositoryData() {
        dataRepo.refreshRepositoryData()
    }

    fun getCurrentUserProfilePicture(){
        //Timber.tag(LOG_TAG).d("getCurrentUserProfilePicture called")
        dataRepo.getProfileImage()
    }

}