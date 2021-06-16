package com.mskdev.githubsearchingtool

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import com.mskdev.githubsearchingtool.network.CheckNetwork
import com.mskdev.githubsearchingtool.utilities.Global.Companion.LOG_TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class GithubSearchToolApplication : Application(){
    private val applicationScope = CoroutineScope(Dispatchers.IO)
    override fun onCreate() {
        super.onCreate()

        delayedInit()

        registerActivityLifecycleCallbacks(object: Application.ActivityLifecycleCallbacks{
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }

            override fun onActivityStarted(activity: Activity) {

            }

            override fun onActivityResumed(activity: Activity) {

            }

            override fun onActivityPaused(activity: Activity) {

            }

            override fun onActivityStopped(activity: Activity) {

            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

            }

            override fun onActivityDestroyed(activity: Activity) {

            }

        })

    }

    private fun delayedInit() {
        applicationScope.launch {
            //Initialize Timber Log
            if (BuildConfig.DEBUG){
                Timber.plant(Timber.DebugTree())
                Timber.tag(LOG_TAG).d("Application-> Timber Initiated.")

            }

            CheckNetwork(applicationContext)

        }



    }
}

class CrashReportingTree: Timber.Tree(){
    @SuppressLint("LogNotTimber")
    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (priority == Log.DEBUG || priority== Log.ERROR){
            Log.e(LOG_TAG, message)
        }
    }

}