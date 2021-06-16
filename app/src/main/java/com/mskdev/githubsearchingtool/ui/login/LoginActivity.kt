package com.mskdev.githubsearchingtool.ui.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.*
import com.mskdev.githubsearchingtool.R
import com.mskdev.githubsearchingtool.databinding.ActivityLoginBinding
import com.mskdev.githubsearchingtool.ui.main.MainActivity
import com.mskdev.githubsearchingtool.utilities.Global.Companion.LOG_TAG
import com.mskdev.githubsearchingtool.utilities.Global.Companion.isNetworkConnected
import timber.log.Timber

@Keep
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var masterKey: String
    private var secureSharedPrefFileName = "com.mskdev.githubsearchingtool"
    private lateinit var encryptedSharedPreferences : SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)

        val viewModelFactory = LoginViewModelFactory.createFactory(this)
        viewModel = ViewModelProvider(this, viewModelFactory).get(LoginViewModel::class.java)


        masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        encryptedSharedPreferences = EncryptedSharedPreferences.create(
            secureSharedPrefFileName,
            masterKey,
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        Timber.tag(LOG_TAG).d("Login Page: onCreate()")

        firebaseAuth = FirebaseAuth.getInstance()

        checkNetworkConnectivity()
        val currentUser = firebaseAuth.currentUser
        updateUI(currentUser)

        binding.btnGithubLogin.setOnClickListener {
            if (isNetworkConnected){
                signInWithGithubProvider()
            }else{
                Toast.makeText(this@LoginActivity, "You are offline.", Toast.LENGTH_SHORT).show()
                checkNetworkConnectivity()
            }

        }
    }


    private fun signInWithGithubProvider(){


        when{
            TextUtils.isEmpty(binding.etUserName.text.toString().trim { it <= ' ' }) ->{

                Toast.makeText(this@LoginActivity, "Please enter email", Toast.LENGTH_SHORT).show()
            }
            else->{
                binding.simpleProgressBar.visibility = ProgressBar.VISIBLE
                val gitEmail : String = binding.etUserName.text.toString().trim{it <= ' '}

                val provider = OAuthProvider.newBuilder("github.com")
                // Target specific email with login hint.
                provider.addCustomParameter("login", gitEmail)


                // Request read access to a user's email addresses.
                // This must be preconfigured in the app's API permissions.

                val scopes = ArrayList<String>()
                scopes.add("user:email")
                provider.scopes = scopes


                val pendingResultTask: Task<AuthResult>? = firebaseAuth.pendingAuthResult
                if (pendingResultTask != null) {
                    // There's something already here! Finish the sign-in for your user.
                    pendingResultTask
                        .addOnSuccessListener(
                            OnSuccessListener {authResult->
                                // User is signed in.
                                // IdP data available in
                                // authResult.getAdditionalUserInfo().getProfile().
                                // The OAuth access token can also be retrieved:
                                // authResult.getCredential().getAccessToken().
                                val credential = authResult.credential as OAuthCredential
                                val token = credential.accessToken!!

                                //Timber.tag(LOG_TAG).d("Token: $token")

                                val user : FirebaseUser? = firebaseAuth.currentUser
                                user?.let {githubUser->

                                    saveUser(gitEmail, githubUser, token)
                                    updateUI(githubUser)

                                }

                            })
                        .addOnFailureListener {
                            // Handle failure.
                            Toast.makeText(this@LoginActivity, "Error_1: ${it.message}", Toast.LENGTH_LONG)
                                .show()
                            Timber.tag(LOG_TAG).e("Error_1: ${it.message}")
                        }
                } else {
                    // There's no pending result so you need to start the sign-in flow.
                    // See below.
                    Timber.tag(LOG_TAG).e("New Authentication process starting")
                    firebaseAuth.startActivityForSignInWithProvider(this@LoginActivity, provider.build())
                        .addOnSuccessListener {authResult->
                            // User is signed in.
                            // IdP data available in
                            // authResult.getAdditionalUserInfo().getProfile().
                            // The OAuth access token can also be retrieved:
                            // authResult.getCredential().getAccessToken().

                            Timber.tag(LOG_TAG).e("Getting New Token.....")

                            val credential = authResult.credential as OAuthCredential
                            val token = credential.accessToken!!

                            //Timber.tag(LOG_TAG).e("Token: $token")

                            val user : FirebaseUser? = firebaseAuth.currentUser
                            user?.let {githubUser->
                                saveUser(gitEmail, githubUser, token)
                                updateUI(githubUser)
                            }

                        }
                        .addOnFailureListener {
                            Toast.makeText(this@LoginActivity, "Error_2: $it", Toast.LENGTH_LONG).show()
                            Timber.tag(LOG_TAG).e("Error_2: $it")
                        }
                }
            }
        }

    }


    private fun updateUI(currentUser: FirebaseUser?) {

        if (currentUser!=null){
            binding.btnGithubLogin.isEnabled = false
            val userUID = currentUser.uid
            Timber.tag(LOG_TAG).d("Github: Login Successful with User : ${currentUser.displayName},  with UserUID: $userUID")
            Toast.makeText(this@LoginActivity, "Welcome ${currentUser.displayName}", Toast.LENGTH_LONG).show()
            goMainActivity()

        }else{
            binding.btnGithubLogin.isEnabled = true
            firebaseAuth.signOut()
            Timber.tag(LOG_TAG).d("Login: Please Login")
            Toast.makeText(this@LoginActivity, "Please Login.", Toast.LENGTH_LONG).show()

        }

    }

    private fun checkNetworkConnectivity() {
        if (isNetworkConnected) {
            binding.simpleProgressBar.visibility = ProgressBar.GONE
        }else{

            Handler(Looper.myLooper()!!).postDelayed({

                binding.simpleProgressBar.visibility = ProgressBar.GONE

                Toast.makeText(this,
                    "You are offline.",
                    Toast.LENGTH_SHORT
                ).show()

                checkNetworkConnectivity()

            }, 3000)

        }
    }

    private fun saveUser(userEmail:String, gitHubUser: FirebaseUser, githubAccessToken: String){
        //Timber.tag(LOG_TAG).i("Saving to DB with User : Email: $userEmail, with UserUID: ${gitHubUser.uid} and Profile URL: UserProfileImage: ${gitHubUser.photoUrl}")
        viewModel.saveUserInDB(userEmail, gitHubUser)
        saveTokenToSharedPref(githubAccessToken)
    }

    private fun saveTokenToSharedPref(token: String){
        val editor = encryptedSharedPreferences.edit()

        editor.putString(getString(R.string.encrypted_token), token)
        editor.apply()

        //Timber.tag(LOG_TAG).d("Token Saved to SharedPreference")
    }

    private fun goMainActivity() {
            val mainIntent = Intent(this, MainActivity::class.java)
            startActivity(mainIntent)
            finish()
    }
}