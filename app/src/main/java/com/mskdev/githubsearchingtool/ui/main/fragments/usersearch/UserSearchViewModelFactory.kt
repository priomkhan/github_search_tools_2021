package com.mskdev.githubsearchingtool.ui.main.fragments.usersearch

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mskdev.githubsearchingtool.data.datasource.GitHubRepository
import java.lang.reflect.InvocationTargetException

class UserSearchViewModelFactory (private val repository: GitHubRepository?):  ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        try {
            return modelClass.getConstructor(GitHubRepository::class.java)
                .newInstance(repository)
        } catch (e: Throwable) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException("Cannot create an instance of $modelClass", e)
        }
    }

    companion object {

        fun createFactory(activity: Activity): UserSearchViewModelFactory {
            val context = activity.applicationContext ?: throw IllegalStateException("Not yet attached to Application")

            return UserSearchViewModelFactory(GitHubRepository.getInstance(context))
        }
    }
}