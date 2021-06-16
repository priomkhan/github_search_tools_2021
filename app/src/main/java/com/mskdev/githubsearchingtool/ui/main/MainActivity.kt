package com.mskdev.githubsearchingtool.ui.main

import android.os.Bundle
import androidx.navigation.findNavController
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mskdev.githubsearchingtool.R
import com.mskdev.githubsearchingtool.utilities.Global.Companion.LOG_TAG
import timber.log.Timber
import java.lang.Exception


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            val navController = findNavController(R.id.navigationHostFragment)
            val navView: BottomNavigationView = findViewById(R.id.bottomNavView)

            val appBarConfiguration = AppBarConfiguration(
                setOf(R.id.userSearchFragment, R.id.repositorySearchFragment)
            )

            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)

        }catch (e: Exception){
            Timber.tag(LOG_TAG).e("MainActivity: $e")
        }


    }


}