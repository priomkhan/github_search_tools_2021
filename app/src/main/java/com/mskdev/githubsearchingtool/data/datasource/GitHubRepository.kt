package com.mskdev.githubsearchingtool.data.datasource

import android.content.Context
import android.os.SystemClock
import androidx.annotation.WorkerThread
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mskdev.githubsearchingtool.data.model.*
import com.mskdev.githubsearchingtool.utilities.Global.Companion.GITHUB_SERVICE_URL
import com.mskdev.githubsearchingtool.utilities.Global.Companion.LOG_TAG
import com.mskdev.githubsearchingtool.utilities.Global.Companion.isNetworkConnected
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.*
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import timber.log.Timber
import java.io.IOException
import java.lang.Exception


class GitHubRepository (private val userDao: UserDao){

    val searchResultUserList = MutableLiveData<List<UserSearchResult>>()
    val selectedUserData = MutableLiveData<GitHubUser>()

    val searchResultRepositoryList = MutableLiveData<List<RepositoryItem>>()
    val searchResultRepositoryListMoreData = MutableLiveData<List<RepositoryItem>>()

    //Authentication of user
    val localUserInfo = MutableLiveData<LocalUser?>()
    private lateinit var currentLocalUserInfo : LocalUser
    val localUserInfoExist = MutableLiveData<Boolean>()
    private val userSearchQuery = MutableLiveData<UserSearchQuery>()

    private val repoSearchQuery = MutableLiveData<RepositorySearchQuery>()

    val busyBool = MutableLiveData<Boolean>()
    val userProfileImage = MutableLiveData<String>()

    /*
    'listType' a parameter rise Type.
    We will using this more than once, so we declare this value as a property of the viewModel class.
    And this creates a custom type that I can use to create or parse JSON content.
     */
    private val listType = Types.newParameterizedType(List::class.java, UserSearchResult::class.java)
    /*
    Here we create an init block, and we will simply call getUserSearchLocalData,
    so that we can retrieve the data as the repository is created.
    */

    init {

        checkIfUserInfoExist()

   }


    fun saveUserInDB(userName: String, gitHubUser: FirebaseUser){
        var bool = false
        CoroutineScope(Dispatchers.IO).launch {
            userDao.deleteAll()
            Timber.tag(LOG_TAG).d("User Profile URI: ${gitHubUser.photoUrl.toString()}")

            val localUser = LocalUser(0,userName,gitHubUser.uid, gitHubUser.photoUrl.toString())
            userDao.insertUser(localUser)
            bool= checkIfUserInfoExist()
            if(bool){
                Timber.tag(LOG_TAG).d("User Now Exist in Local Database")
            }
        }
    }

    fun getProfileImage(){
        //Timber.tag(LOG_TAG).d("Profile Image Loading from database....")
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val localUserData = userDao.getAll()
                if (localUserData.isNotEmpty()){

                    userProfileImage.postValue(localUserData[0].userProfileImage)

                }
            }
        }catch (e: Exception){
                Timber.tag(LOG_TAG).d("Profile Image Not Found. Error: $e")
        }


    }

    fun logout(): Boolean{
        return try {
            CoroutineScope(Dispatchers.IO).launch {
                userDao.deleteAll()
            }
            localUserInfoExist.postValue(false)
            localUserInfo.postValue(null)
            FirebaseAuth.getInstance().signOut()
            true
        }catch (e: Exception){
            Timber.tag(LOG_TAG).e("Logout Error: $e")
            false
        }
    }

    fun checkIfUserInfoExist(): Boolean{
        var isExist = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val data = userDao.getAll()
                if(data.isEmpty()){
                    localUserInfoExist.postValue(false)
                    Timber.tag(LOG_TAG).d("Local Database is Empty" )
                    isExist = false
                }else{
                    localUserInfo.postValue(data[0])
                    currentLocalUserInfo = data.first()
                    localUserInfoExist.postValue(true)
                    Timber.tag(LOG_TAG).d("Local Data: ${data[0]}" )
                    isExist = true
                    refreshUserData()

                }
            }catch (e: Exception){
                Timber.tag(LOG_TAG).e("Database Error: $e" )
            }

        }
        return isExist
    }

    fun refreshUserData() {
        val currentQuery= userSearchQuery.value?: UserSearchQuery()
        if(currentQuery.query.isNotBlank() && currentQuery.query.isNotEmpty()){
            CoroutineScope(Dispatchers.IO).launch{
                Timber.tag(LOG_TAG).d("GitHubRepository: Auto Query Search: ${currentQuery.query}, sort by ${currentQuery.sort}")
                getUserData(currentQuery)
            }
        }

    }

    fun refreshUserData(mUserQuery: UserSearchQuery) {

        if(mUserQuery.query.isNotBlank() && mUserQuery.query.isNotEmpty()){
            userSearchQuery.postValue(mUserQuery)
            CoroutineScope(Dispatchers.IO).launch{
                Timber.tag(LOG_TAG).d("GitHubRepository: Manual Query Search: ${mUserQuery.query} and sort by ${mUserQuery.sort}")
                getUserData(mUserQuery)
            }
        }else{
            userSearchQuery.postValue(UserSearchQuery())
        }

    }



    fun refreshRepositoryData() {
        val mRepositoryQuery= repoSearchQuery.value?: RepositorySearchQuery()
        if(mRepositoryQuery.query.isNotBlank()&&mRepositoryQuery.query.isNotEmpty()){
            CoroutineScope(Dispatchers.IO).launch{
                Timber.tag(LOG_TAG).d("GitHubRepository: Auto Query Search: ${mRepositoryQuery.query}")
                searchResultRepositoryList.postValue(emptyList())
                getRepositoryData(mRepositoryQuery)
            }
        }

    }


    fun refreshRepositoryData(mRepositoryQuery: RepositorySearchQuery) {

        if(mRepositoryQuery.query.isNotBlank()&&mRepositoryQuery.query.isNotEmpty()){
            repoSearchQuery.postValue(mRepositoryQuery)
            CoroutineScope(Dispatchers.IO).launch{
                Timber.tag(LOG_TAG).d("GitHubRepository: Manual Query Search: $mRepositoryQuery")
                getRepositoryData(mRepositoryQuery)
            }
        }else{
            repoSearchQuery.postValue(RepositorySearchQuery())
        }

    }

    private fun getRepositoryData(query: RepositorySearchQuery) = runBlocking { // this: CoroutineScope
        launch {
            busyBool.postValue(true)
            Timber.tag(LOG_TAG).d("Task from runBlocking to get search result")

            //delay(200L)
        }

        coroutineScope { // Creates a coroutine scope to get each user details
            launch {

                Timber.tag(LOG_TAG).d("Task from nested launch to get each user details")
                searchGithubRepository(query)
                //delay(200L)
            }

        }
        //delay(200L)
        Timber.tag(LOG_TAG).d("getData() Coroutine scope is over") // This line is not printed until the nested launch completes
        busyBool.postValue(false)
    }

    @WorkerThread
    suspend fun searchGithubRepository(mRepositoryQuery: RepositorySearchQuery) {
        Timber.tag(LOG_TAG).d("Getting Data with query: ${mRepositoryQuery.query}, sortby: ${mRepositoryQuery.sort} and pageNumber: ${mRepositoryQuery.page}")

        //val authToken = currentLocalUserInfo.githubAccessToken
        val authToken = mRepositoryQuery.token

        if(networkAvailable() && !authToken.isNullOrBlank()){
            try {
                //Timber.tag(LOG_TAG).d("Network Available: Getting Data with Token: $authToken")
                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
                //val authToken = "Basic " + Base64.getEncoder().encode("$username:$password".toByteArray()).toString(Charsets.UTF_8)


                val oKHttpClientBuilder = OkHttpClient.Builder()

                val oKHttpDispatcher = Dispatcher()
                oKHttpDispatcher.maxRequests = 1

                val networkInterceptor: Interceptor = Interceptor { chain ->
                    SystemClock.sleep(1000)
                    chain.proceed(chain.request())
                }

                oKHttpClientBuilder
                    .addNetworkInterceptor(networkInterceptor)
                    .dispatcher(oKHttpDispatcher)
                    .addInterceptor{ chain ->
                        val original = chain.request()
                        val builder = original.newBuilder()
                            .header("Accept", "application/vnd.github.mercy-preview+json")
                            .header("Authorization", authToken)
                        val request = builder.build()
                        chain.proceed(request)
                    }


                val httpClient = oKHttpClientBuilder.build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(GITHUB_SERVICE_URL)
                    .addConverterFactory(MoshiConverterFactory.create(moshi)) //we used moshi builder to map the json to class property
                    .client(httpClient)
                    .build()

                val service = retrofit.create(GitHubRepositorySearchService::class.java)

                val serviceData = service.getRepositoryListData(mRepositoryQuery.query, mRepositoryQuery.sort.value, mRepositoryQuery.page.toString()).body()?: RepositorySearchResult(0, emptyList())
                val totalResult = serviceData.totalCount
                Timber.tag(LOG_TAG).d("Total Result Found: $totalResult \n")
                if(totalResult>0){
                    val repositorySearchList = serviceData.repositoryItems
                    val listSize = repositorySearchList.size
                    //searchResultRepositoryList.postValue(repositorySearchList)
                    if (mRepositoryQuery.page==1){
                        searchResultRepositoryList.postValue(repositorySearchList)
                    }else if (mRepositoryQuery.page>1){
                        Timber.tag(LOG_TAG).d("$listSize more data Found at page ${mRepositoryQuery.page},  \n")
                        searchResultRepositoryListMoreData.postValue(repositorySearchList)
                    }
                }
            }catch (e:Exception){
                Timber.tag(LOG_TAG).e("searchGithubRepository Error: $e")
            }

        }else{
            Timber.tag(LOG_TAG).d("Network Not Available.")
        }
    }

    private fun getUserData(mUserQuery: UserSearchQuery) = runBlocking { // this: CoroutineScope
        launch {
            busyBool.postValue(true)
            Timber.tag(LOG_TAG).d("Task from runBlocking to get search result")

            //delay(200L)
        }

        coroutineScope { // Creates a coroutine scope to get each user details
            launch {

                Timber.tag(LOG_TAG).d("Task from nested launch to get each user details")
                searchGithubUsers(mUserQuery)
                //delay(200L)
            }

        }
        //delay(200L)
        Timber.tag(LOG_TAG).d("getData() Coroutine scope is over") // This line is not printed until the nested launch completes
        busyBool.postValue(false)
    }


    @WorkerThread
    suspend fun searchGithubUsers(query: UserSearchQuery) {
//        val authToken = currentLocalUserInfo.githubAccessToken
        val authToken = query.token

        if(networkAvailable() && !authToken.isNullOrBlank()){
            //Timber.tag(LOG_TAG).d("Network Available: Getting Data with Token: $authToken")
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            //val authToken = "Basic " + Base64.getEncoder().encode("$username:$password".toByteArray()).toString(Charsets.UTF_8)


            val oKHttpClientBuilder = OkHttpClient.Builder()

            val oKHttpDispatcher = Dispatcher()
            oKHttpDispatcher.maxRequests = 1

            val networkInterceptor: Interceptor = object : Interceptor {
                @Throws(IOException::class)
                override fun intercept(chain: Interceptor.Chain): Response {
                    SystemClock.sleep(1000)
                    return chain.proceed(chain.request())
                }
            }

            oKHttpClientBuilder
                .addNetworkInterceptor(networkInterceptor)
                .dispatcher(oKHttpDispatcher)
                .addInterceptor{ chain ->
                        val original = chain.request()
                        val builder = original.newBuilder()
                            .header("Accept", "application/vnd.github.v3+json")
                            .header("Authorization", authToken)
                        val request = builder.build()
                        chain.proceed(request)
                    }


            val httpClient = oKHttpClientBuilder.build()

            val retrofit = Retrofit.Builder()
                .baseUrl(GITHUB_SERVICE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi)) //we used moshi builder to map the json to class property
                .client(httpClient)
                .build()

            val service = retrofit.create(GitHubUserSearchService::class.java)

            val serviceData = service.getUserSearchData(query.query, query.sort.value).body()?: UserSearchList(0, emptyList())

            if(serviceData.total_count>0){
                val totalResult = serviceData.total_count

                val userSearchList = serviceData.items
                Timber.tag(LOG_TAG).d("Total Result Found: $totalResult \n")

                searchResultUserList.postValue(userSearchList)

            }

        }else{
            Timber.tag(LOG_TAG).d("Network Not Available.")
        }

    }

    fun getDetails(query: UserDetailsQuery){
         CoroutineScope(Dispatchers.IO).launch {

             val userData = GitHubUser(query.mUserSearchResult, getUserDetails(query), getRepos(query))

             selectedUserData.postValue(userData)

         }
    }

    @WorkerThread
    suspend fun getUserDetails(query: UserDetailsQuery): UserDetails {
        val userName = query.mUserSearchResult.userName
        val authToken = query.token
        //val authToken = currentLocalUserInfo.githubAccessToken

        //Log.i(LOG_TAG, "GitHubRepository: getting data for (${userName})")

        if(networkAvailable() && !authToken.isNullOrBlank()){

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
//        val authToken = "Basic " + Base64.getEncoder().encode("$username:$password".toByteArray()).toString(Charsets.UTF_8)
            val httpClient = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val original = chain.request()
                    val builder = original.newBuilder()
                        .header("Accept", "application/vnd.github.v3+json")
                        .header("Authorization", authToken)
                    val request = builder.build()
                    chain.proceed(request)
                }
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(GITHUB_SERVICE_URL)
                .addConverterFactory(MoshiConverterFactory.create(moshi)) //we used moshi builder to map the json to class property
                .client(httpClient)
                .build()
            val service = retrofit.create(GitHubUserDetailsService::class.java)

            val serviceData = service.getUserDetailsData(userName).body()

            return serviceData ?: UserDetails("n/a","n/a","n/a","n/a","n/a","n/a",0,0,0)

        }else{
            return  UserDetails("n/a","n/a","n/a","n/a","n/a","n/a",0,0,0)
        }

    }

    @WorkerThread
    suspend fun getRepos(query: UserDetailsQuery): List<UserRepo>?{
        val userName = query.mUserSearchResult.userName
        val authToken = query.token
        //val username = localUserInfo.value?.userName
        //val password = localUserInfo.value?.password
        //val authToken = currentLocalUserInfo.githubAccessToken

        if(networkAvailable() && !authToken.isNullOrBlank()){
            try {
                Timber.tag(LOG_TAG).d("GitHubRepository: getting repo data for (${userName})")

                val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
//        val authToken = "Basic " + Base64.getEncoder().encode("$username:$password".toByteArray()).toString(Charsets.UTF_8)

                val httpClient = OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val original = chain.request()
                        val builder = original.newBuilder()
                            .header("Accept", "application/vnd.github.v3+json")
                            .header("Authorization", authToken)
                        val request = builder.build()
                        chain.proceed(request)
                    }
                    .build()

                val retrofit = Retrofit.Builder()
                    .baseUrl(GITHUB_SERVICE_URL)
                    .addConverterFactory(MoshiConverterFactory.create(moshi)) //we used moshi builder to map the json to class property
                    .client(httpClient)
                    .build()
                val service = retrofit.create(GitHubUserRepoService::class.java)
                val response = service.getUserRepos(userName).body()?: emptyList()

                if(response.isNotEmpty()){
                    Timber.tag(LOG_TAG).d("Repo Size: ${response.size}")
                }else{
                    Timber.tag(LOG_TAG).d( "No public repository found ")
                }

                return response
            }catch (e: Exception){
                Timber.tag(LOG_TAG).e("getRepos Error: $e")
                return emptyList()
            }

        }else{
            return emptyList()
        }

    }

    //This function check if the mobile is connected to the Wifi or Mobile internet, however
    // it does not work above Android O.
    private fun networkAvailable():Boolean{

        return isNetworkConnected

    }


    companion object {
        @Volatile
        private var instance: GitHubRepository? = null

        fun getInstance(context: Context): GitHubRepository? {
            return instance ?: synchronized(GitHubRepository::class.java) {
                if (instance == null) {
                    val database = UserDatabase.getInstance(context)
                    instance = GitHubRepository(database.userDao())
                }
                return instance
            }
        }
    }
}