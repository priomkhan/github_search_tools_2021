package com.mskdev.githubsearchingtool.ui.main.fragments.usersearch

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.mskdev.githubsearchingtool.data.datasource.GitHubRepository
import com.mskdev.githubsearchingtool.data.model.UserDetailsQuery
import com.mskdev.githubsearchingtool.data.model.UserSearchQuery
import com.mskdev.githubsearchingtool.data.model.UserSearchResult
import com.mskdev.githubsearchingtool.utilities.Global.Companion.LOG_TAG
import timber.log.Timber


class UserSearchViewModel (private val dataRepo: GitHubRepository): ViewModel(){
    val busyBool = dataRepo.busyBool
    val userOnlineData = dataRepo.searchResultUserList
    val selectedUserData = dataRepo.selectedUserData

    private val _userProfileImage = dataRepo.userProfileImage
    val userProfileImage : LiveData<String>
        get() =_userProfileImage

    fun logout(): Boolean{
        return dataRepo.logout()
    }

    fun getSelectedUserData(query: UserDetailsQuery){

        dataRepo.getDetails(query)

    }

    fun searchUser(query: UserSearchQuery){
        //Timber.tag(LOG_TAG).d("UserSearchViewModel: New Query Search: $query")
        dataRepo.refreshUserData(query)
    }

    fun refreshUserData() {
        dataRepo.refreshUserData()
    }

    fun getCurrentUserProfilePicture(){
        //Timber.tag(LOG_TAG).d("getCurrentUserProfilePicture called")
        dataRepo.getProfileImage()
    }
}