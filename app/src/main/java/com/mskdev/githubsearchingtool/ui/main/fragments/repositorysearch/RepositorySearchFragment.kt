package com.mskdev.githubsearchingtool.ui.main.fragments.repositorysearch

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.view.*
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.mskdev.githubsearchingtool.R
import com.mskdev.githubsearchingtool.`interface`.OnLoadMoreListener
import com.mskdev.githubsearchingtool.data.model.RepositorySearchQuery
import com.mskdev.githubsearchingtool.databinding.RepositorySearchFragmentBinding
import com.mskdev.githubsearchingtool.enums.RepositorySearchSort
import com.mskdev.githubsearchingtool.ui.login.LoginActivity
import com.mskdev.githubsearchingtool.utilities.Global.Companion.LOG_TAG
import com.mskdev.githubsearchingtool.utilities.RecyclerViewLoadMoreScroll
import com.mskdev.githubsearchingtool.utilities.loadImage
import de.hdodenhof.circleimageview.CircleImageView
import timber.log.Timber

class RepositorySearchFragment : Fragment() {

    private lateinit var binding: RepositorySearchFragmentBinding
    private lateinit var viewModel: RepositorySearchViewModel
    private lateinit var navController: NavController

    private lateinit var adapter: RepositorySearchRecyclerAdapter
    private lateinit var scrollListener: RecyclerViewLoadMoreScroll
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var recyclerViewState: Parcelable
    private lateinit var profileImage : CircleImageView

    private var newQueryString = ""
    private var sort : RepositorySearchSort = RepositorySearchSort.BEST_MATCH
    private var pageNumber = 0

    private lateinit var masterKey: String
    private var secureSharedPrefFileName = "com.mskdev.githubsearchingtool"
    private lateinit var encryptedSharedPreferences : SharedPreferences

    private var token: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.repository_search_fragment, container, false)

        val viewModelFactory = RepositorySearchViewModelFactory.createFactory(this.requireActivity())
        viewModel = ViewModelProvider(this, viewModelFactory).get(RepositorySearchViewModel::class.java)

        masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        encryptedSharedPreferences = EncryptedSharedPreferences.create(
            secureSharedPrefFileName,
            masterKey,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )



        binding.svSearchTopic.queryHint = "Search Any Topic"
        binding.svSearchTopic.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()){
                    binding.svSearchTopic.clearFocus()
                    adapter.clearData()
                    newQueryString = query?:""
                    pageNumber = 1

                    val checkedRadioButtonId = binding.radioGroupRepositorySearch.checkedRadioButtonId
                    if (checkedRadioButtonId == R.id.rbBestMatch){
                        Timber.tag(LOG_TAG).d("Searching by BestMatch.")
                        sort = RepositorySearchSort.BEST_MATCH
                    }else if (checkedRadioButtonId == R.id.rbStars){
                        Timber.tag(LOG_TAG).d("Searching by Stars.")
                        sort = RepositorySearchSort.STARS
                    }else if (checkedRadioButtonId == R.id.rbForks){
                        Timber.tag(LOG_TAG).d("Searching by Forks.")
                        sort = RepositorySearchSort.FORKS
                    }

                    Timber.tag(LOG_TAG).d(" RepositorySearchFragment: New Query Search: $newQueryString")

                    val newRepoQuery = RepositorySearchQuery(newQueryString, sort, pageNumber, token)

                    viewModel.searchRepository(newRepoQuery)

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
                binding.progressBar.visibility = View.VISIBLE
            }else{

                binding.progressBar.visibility = View.INVISIBLE
                binding.recyclerView.adapter?.notifyDataSetChanged()
            }

        })

        val itemClickListener = RepositoryItemClickListener { item ->
            Timber.tag(LOG_TAG).d( "Selected Repository: ${item.fullName}")
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse(item.htmlURL)
            startActivity(openURL)
        }

        adapter = RepositorySearchRecyclerAdapter(itemClickListener)

        setRVScrollListener()
        viewModel.repositoryOnlineData.observe(viewLifecycleOwner, Observer {
            try {
                adapter.clearData()
                adapter.addData(it)
                binding.recyclerView.adapter = adapter
                recyclerViewState = binding.recyclerView.layoutManager?.onSaveInstanceState()!!
                binding.swipeLayout.isRefreshing = false //turning of refreshing indicator off.

            }catch (e:Exception){
                Timber.tag(LOG_TAG).e( "Error: $e")
            }

        })

        viewModel.repositoryOnlineMoreData.observe(viewLifecycleOwner, Observer {
            try {
                Timber.tag(LOG_TAG).d( "More Data Observed. Size: ${it.size}")
                adapter.removeLoadingView()
                adapter.addData(it)
                scrollListener.setLoaded()
                binding.recyclerView.layoutManager?.onRestoreInstanceState(recyclerViewState)
                binding.swipeLayout.isRefreshing = false //turning of refreshing indicator off.
            }catch (e: Exception){
                Timber.tag(LOG_TAG).e( "Error: $e")
            }

        })

        binding.swipeLayout.setOnRefreshListener {
            try {
                Handler(Looper.myLooper()!!).postDelayed({
                    adapter.clearData()
                    viewModel.refreshRepositoryData()

                },2000)
            }catch (e: Exception){
                Timber.tag(LOG_TAG).e( "Error: $e")
            }

        }

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
            requireActivity(),R.id.navigationHostFragment
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

    private  fun setRVScrollListener() {
        mLayoutManager = binding.recyclerView.layoutManager as LinearLayoutManager
        scrollListener = RecyclerViewLoadMoreScroll(mLayoutManager)
        scrollListener.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                Timber.tag(LOG_TAG).d("Load More Data")
                loadMoreDataToRecyclerView()
            }
        })
        binding.recyclerView.addOnScrollListener(scrollListener)
    }

    private fun loadMoreDataToRecyclerView(){
        adapter.addLoadingView()
        recyclerViewState = binding.recyclerView.layoutManager?.onSaveInstanceState()!!
        pageNumber+=1
        val newRepoQuery = RepositorySearchQuery(newQueryString, sort, pageNumber, token)
        viewModel.searchRepository(newRepoQuery)
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