package com.mskdev.githubsearchingtool.ui.main.fragments.repositorysearch

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mskdev.githubsearchingtool.data.model.RepositoryItem
import com.mskdev.githubsearchingtool.databinding.ProgressLoadingBinding
import com.mskdev.githubsearchingtool.databinding.RepositorySearchListItemBinding
import com.mskdev.githubsearchingtool.objects.Constant
import com.mskdev.githubsearchingtool.utilities.Global.Companion.LOG_TAG
import timber.log.Timber

class RepositorySearchRecyclerAdapter (private val repositoryItemClickListener: RepositoryItemClickListener): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private var repositoryList = ArrayList<RepositoryItem?>()

    fun addData(dataViews: List<RepositoryItem>) {
        for (item in dataViews){
            this.repositoryList.add(item)
        }
        Timber.tag(LOG_TAG).d("Adapter: AddData: itemCount: $itemCount")
        notifyItemInserted(itemCount-1)
    }

    fun clearData(){
        repositoryList.clear()
        notifyDataSetChanged()
    }

    fun getItemAtPosition(position: Int): RepositoryItem? {
        return repositoryList[position]
    }

    fun addLoadingView() {
        //Add loading item
        Handler(Looper.myLooper()!!).post {
            Timber.tag(LOG_TAG).d("Loading View Added............")
            if (repositoryList.size>=0){
                repositoryList.add(null)
                notifyItemInserted(itemCount - 1)
            }

        }
    }

    fun removeLoadingView() {
        //Remove loading item
        if (repositoryList.size>0) {
            repositoryList.removeAt(itemCount - 1)
            notifyItemRemoved(repositoryList.size)
        }
    }


    override fun getItemCount(): Int {
        return repositoryList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (repositoryList[position] == null) {
            Constant.VIEW_TYPE_LOADING
        } else {
            Constant.VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == Constant.VIEW_TYPE_ITEM){
            return ItemViewHolder.from(parent)
        }else{
            return LoadingViewHolder.from(parent)
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder.itemViewType == Constant.VIEW_TYPE_ITEM){
            val repoItem = repositoryList[position]

            repoItem?.let {
                //holder.bind(repoItem, repositoryItemClickListener)
                val viewHolder = holder as ItemViewHolder
                viewHolder.bind(repoItem, repositoryItemClickListener)
            }


        }
    }

    class LoadingViewHolder internal constructor(private val binding: ProgressLoadingBinding) : RecyclerView.ViewHolder(binding.root){

        companion object{
            fun from(parent: ViewGroup): LoadingViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context) //The parent is the ViewGroup at the root of the layout.
                val view = ProgressLoadingBinding.inflate(layoutInflater, parent, false)
                return LoadingViewHolder(view)
            }
        }
    }

    class ItemViewHolder internal constructor(private val binding: RepositorySearchListItemBinding): RecyclerView.ViewHolder(binding.root) {
        //private val context = itemView.context

        fun bind(repositoryItem: RepositoryItem, repositoryItemClickListener: RepositoryItemClickListener){
            binding.repoItem = repositoryItem
            binding.itemClickListener = repositoryItemClickListener

            binding.repoLayout.setOnClickListener {
                repositoryItemClickListener.onClick(repositoryItem)
            }
        }

        companion object{
            fun from(parent: ViewGroup): ItemViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context) //The parent is the ViewGroup at the root of the layout.
                val view = RepositorySearchListItemBinding.inflate(layoutInflater, parent, false)
                return ItemViewHolder(view)
            }
        }

    }
}

class RepositoryItemClickListener(val clickListener: (repositoryItem: RepositoryItem)-> Unit){
    fun onClick(repositoryItem: RepositoryItem){
        clickListener(repositoryItem)
    }
}
