package com.mskdev.githubsearchingtool.ui.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.mskdev.githubsearchingtool.data.datasource.GitHubRepository


class LoginViewModel (private val dataRepo: GitHubRepository): ViewModel(){

    fun checkIfUserExist(): Boolean{
        return dataRepo.checkIfUserInfoExist()
    }

    fun saveUserInDB(userName: String, gitHubUser: FirebaseUser ){
        dataRepo.saveUserInDB(userName,gitHubUser)
    }




}