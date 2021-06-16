package com.mskdev.githubsearchingtool.ui.main.fragments.usersearch.details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mskdev.githubsearchingtool.data.model.UserRepo
import com.mskdev.githubsearchingtool.databinding.RepoListItemBinding

class UserDetailedRepoRecyclerAdapter(val itemListener: RepoItemListener): RecyclerView.Adapter<UserDetailedRepoRecyclerAdapter.ViewHolder>(){

    var userRepositoryData = listOf<UserRepo>()
        set(value) {
            field = value
            notifyDataSetChanged()
            userRepositoryFiltered.addAll(value)
        }

    private var userRepositoryFiltered = ArrayList<UserRepo>()


    override fun getItemCount(): Int {
        return userRepositoryFiltered.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder.from(parent)
    }



    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val repoItem = userRepositoryFiltered[position]

        holder.bind(repoItem, itemListener)
    }


    class ViewHolder internal constructor(private val binding: RepoListItemBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserRepo, itemClickListener: RepoItemListener){

            binding.repoItem = item
            binding.itemClickListener = itemClickListener

            binding.repoLayout.setOnClickListener {
                itemClickListener.onClick(item)
            }

        }

        companion object{
            fun from(parent: ViewGroup): ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = RepoListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(view)
            }
        }

    }



    fun getFilteredRepo(query: String) {
        userRepositoryFiltered.clear()

        if(query.isEmpty()){
            userRepositoryFiltered.addAll(userRepositoryData)
        }else{
            if(userRepositoryData.isNotEmpty()){
                userRepositoryFiltered = userRepositoryData.filter {
                    it.repoName!!.contains(query, true)
                } as ArrayList<UserRepo>
            }

        }
    }

    class RepoItemListener(val clickListener: (item: UserRepo)-> Unit){
        fun onClick(item: UserRepo){
            clickListener(item)
        }
    }

}

