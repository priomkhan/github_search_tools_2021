package com.mskdev.githubsearchingtool.ui.main.fragments.usersearch.details

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.mskdev.githubsearchingtool.R
import com.mskdev.githubsearchingtool.data.model.UserRepo
import com.mskdev.githubsearchingtool.databinding.FragmentUserDetailsBinding
import com.mskdev.githubsearchingtool.ui.main.fragments.usersearch.UserSearchViewModelFactory
import com.mskdev.githubsearchingtool.utilities.Global.Companion.LOG_TAG
import timber.log.Timber


class UserDetailsFragment : Fragment() {

    private lateinit var binding: FragmentUserDetailsBinding
    private lateinit var viewModel: UserDetailsViewModel
    private lateinit var navController: NavController
    private lateinit var adapter: UserDetailedRepoRecyclerAdapter

    private lateinit var repoList: List<UserRepo>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user_details, container, false)

        val argument = UserDetailsFragmentArgs.fromBundle(requireArguments())

        val viewModelFactory = UserSearchViewModelFactory.createFactory(this.requireActivity())
        viewModel = ViewModelProvider(this, viewModelFactory).get(UserDetailsViewModel::class.java)



        binding.viewModel = viewModel

        val itemClickListener = UserDetailedRepoRecyclerAdapter.RepoItemListener { item ->

            Timber.tag(LOG_TAG).d( "Selected Repository: ${item.repoName}")
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse(item.html_url)
            startActivity(openURL)
        }

        adapter = UserDetailedRepoRecyclerAdapter(itemClickListener)

        try {
            val userData =  argument.selectedUserItem
            Timber.tag(LOG_TAG).d( "Selected User Name: ${userData.userDetails?.name}")

            binding.githubUser = userData

            if(userData.userRepoList!=null){
                repoList = userData.userRepoList!!
                Timber.tag(LOG_TAG).d("Number of Repository Found: ${userData.userRepoList!!.size}")
                adapter.userRepositoryData = repoList
                binding.repoRecyclerView.adapter = adapter
            }else{
                Timber.tag(LOG_TAG).d( "No Public Repository Found")
            }

        }catch (e: Exception){
            Timber.tag(LOG_TAG).e("Argument Error: $e")
        }

        (requireActivity() as AppCompatActivity).run {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        setHasOptionsMenu(true)

        return binding.root
    }


    override fun onStart() {
        super.onStart()
        navController = Navigation.findNavController(
            requireActivity(), R.id.navigationHostFragment
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.user_detailes_menubar,menu)

        val menuItem = menu.findItem(R.id.repo_search)
        val searchView = menuItem.actionView as SearchView
        searchView.queryHint = "Search Repo"

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(requireContext(), " onQueryTextSubmit ", Toast.LENGTH_LONG).show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.toString().trim()
                if(query.isNotBlank() && query.isNotEmpty()){
                    //Toast.makeText(requireContext(), "Searching for:  ${query} ",Toast.LENGTH_LONG).show()
                    adapter.getFilteredRepo(query)

                }else{
                    adapter.getFilteredRepo("")

                }
                adapter.notifyDataSetChanged()
                return false
            }

        })


        super.onCreateOptionsMenu(menu, inflater)
    }




    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        if(id == android.R.id.home){
            navController.navigateUp()
        }

        return super.onOptionsItemSelected(item)
    }


}