package com.mskdev.githubsearchingtool.ui.main.fragments.usersearch

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.mskdev.githubsearchingtool.R
import com.mskdev.githubsearchingtool.data.model.UserDetailsQuery
import com.mskdev.githubsearchingtool.data.model.UserSearchQuery
import com.mskdev.githubsearchingtool.databinding.UserSearchFragmentBinding
import com.mskdev.githubsearchingtool.enums.UserSearchSort
import com.mskdev.githubsearchingtool.ui.login.LoginActivity
import com.mskdev.githubsearchingtool.utilities.Global.Companion.LOG_TAG
import com.mskdev.githubsearchingtool.utilities.loadImage
import de.hdodenhof.circleimageview.CircleImageView
import timber.log.Timber

class UserSearchFragment : Fragment() {


    private lateinit var binding: UserSearchFragmentBinding
    private lateinit var viewModel: UserSearchViewModel
    private lateinit var navController: NavController
    private lateinit var adapter: UserSearchRecyclerAdapter
    private var showDetails: Boolean = false
    private lateinit var profileImage : CircleImageView

    private lateinit var masterKey: String
    private var secureSharedPrefFileName = "com.mskdev.githubsearchingtool"
    private lateinit var encryptedSharedPreferences : SharedPreferences

    private var token: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.user_search_fragment, container, false)

        val viewModelFactory = UserSearchViewModelFactory.createFactory(requireActivity())
        viewModel = ViewModelProvider(this, viewModelFactory).get(UserSearchViewModel::class.java)

        masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        encryptedSharedPreferences = EncryptedSharedPreferences.create(
            secureSharedPrefFileName,
            masterKey,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        binding.svSearchUser.queryHint = "Search for GitHub User"
        binding.svSearchUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()){
                    binding.svSearchUser.clearFocus()
                    adapter.clearData()
                    val newQueryString = query?:""
                    var sort : UserSearchSort= UserSearchSort.BEST_MATCH
                    val checkedRadioButtonId = binding.radioGroupUserSearch.checkedRadioButtonId
                    if (checkedRadioButtonId == R.id.rbBestMatch){
                        //Timber.tag(LOG_TAG).d("Searching by BestMatch.")
                        sort = UserSearchSort.BEST_MATCH
                    }else if (checkedRadioButtonId == R.id.rbFollower){
                        //Timber.tag(LOG_TAG).d("Searching by Follower.")
                        sort = UserSearchSort.FOLLOWERS
                    }else if (checkedRadioButtonId == R.id.rbRepositories){
                        //Timber.tag(LOG_TAG).d("Searching by Repositories.")
                        sort = UserSearchSort.REPOSITORIES
                    }

                    Timber.tag(LOG_TAG).d(" UserSearchFragment: New Query Search: $newQueryString")

                    val newQuery : UserSearchQuery = UserSearchQuery(newQueryString, sort, token)

                    viewModel.searchUser(newQuery)

                    return true
                }else{
                    return false
                }

            }

            override fun onQueryTextChange(newText: String?): Boolean {
                //viewModel.search(newText?:"")
                return false
            }

        })

        viewModel.busyBool.observe(viewLifecycleOwner, Observer {

            if(it){
                binding.progressBar.visibility = View.VISIBLE;
                //  recyclerView.visibility = View.GONE
            }else{

                binding.progressBar.visibility = View.INVISIBLE;
                // recyclerView.visibility = View.VISIBLE
                binding.recyclerView.adapter?.notifyDataSetChanged();
            }

        })


        val itemClickListener = UserItemClickListener { item ->
            Timber.tag(LOG_TAG).d("itemClicked: $item")
            showDetails = true
            viewModel.busyBool.postValue(true)
            val mUserDetailsQuery = UserDetailsQuery(item, token)
            viewModel.getSelectedUserData(mUserDetailsQuery)
        }

        adapter = UserSearchRecyclerAdapter(itemClickListener)

        viewModel.userOnlineData.observe(viewLifecycleOwner, Observer {
            adapter.clearData()
            adapter.addData(it)
            binding.recyclerView.adapter = adapter
            binding.swipeLayout.isRefreshing = false //turning of refreshing indicator off.
        })

        binding.swipeLayout.setOnRefreshListener {
            try {
                Handler(Looper.myLooper()!!).postDelayed({
                    adapter.clearData()
                    viewModel.refreshUserData()

                },2000)
            }catch (e: Exception){
                Timber.tag(LOG_TAG).e( "Error: $e")
            }

        }

        viewModel.selectedUserData.observe(viewLifecycleOwner, Observer {userData->
            if(showDetails){
                showDetails = false
                viewModel.busyBool.postValue(false)
                navController.navigate(UserSearchFragmentDirections.actionUserSearchFragmentToUserDetailsFragment(userData))
            }
        })

        viewModel.userProfileImage.observe(viewLifecycleOwner, Observer {url->
            Handler(Looper.myLooper()!!).postDelayed({
                try {
                    //Timber.tag(LOG_TAG).e( "url: $url")
                    loadImage(profileImage, url)
                }catch (e: Exception){
                    Timber.tag(LOG_TAG).e( "Profile image Loading Error: $e")
                }

            },500)
        })

        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        navController = Navigation.findNavController(
            requireActivity(), R.id.navigationHostFragment
        )

    }

    override fun onResume() {
        super.onResume()
        try {
            token = encryptedSharedPreferences.getString(resources.getString(R.string.encrypted_token), "")
            //Timber.tag(LOG_TAG).d(" Token Retrieved: $token")
        }catch (e: Exception){
            Timber.tag(LOG_TAG).d("Error fetching Token: $e")
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        val menuItem = menu.findItem(R.id.profile_menu)
        val view = menuItem.actionView
        profileImage = view.findViewById(R.id.toolbar_profile_image)

        viewModel.getCurrentUserProfilePicture()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_logout->{
                val success = viewModel.logout()
                if (success) {
                    val intent = Intent(requireContext(), LoginActivity::class.java)
                    startActivity(intent)
                }
            }
        }


        return super.onOptionsItemSelected(item)
    }

}