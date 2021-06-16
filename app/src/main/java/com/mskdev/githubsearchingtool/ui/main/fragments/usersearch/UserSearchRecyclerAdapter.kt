package com.mskdev.githubsearchingtool.ui.main.fragments.usersearch

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mskdev.githubsearchingtool.data.model.UserSearchResult
import com.mskdev.githubsearchingtool.databinding.UserSearchListItemBinding
import com.mskdev.githubsearchingtool.utilities.Global
import timber.log.Timber


class UserSearchRecyclerAdapter(private val userItemClickListener: UserItemClickListener): RecyclerView.Adapter<UserSearchRecyclerAdapter.ViewHolder>(){

//    var userList = listOf<UserSearchResult>()
//        set(value) {
//            field = value
//            notifyDataSetChanged()
//        }
    private var userList = ArrayList<UserSearchResult?>()

    fun addData(dataViews: List<UserSearchResult>) {
        for (item in dataViews){
            this.userList.add(item)
        }
        Timber.tag(Global.LOG_TAG).d("Adapter: AddData: itemCount: $itemCount")
        notifyItemInserted(itemCount-1)
    }

    fun clearData(){
        userList.clear()
        notifyDataSetChanged()
    }


    override fun getItemCount(): Int {
        return userList.size
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {


        return ViewHolder.from(parent)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]

        user?.let {
            holder.bind(it, userItemClickListener)
        }

    }

    class ViewHolder internal constructor(private val binding: UserSearchListItemBinding): RecyclerView.ViewHolder(binding.root) {
        //private val context = itemView.context

        fun bind(item: UserSearchResult, userItemClickListener: UserItemClickListener){
            binding.item = item
            binding.itemClickListener = userItemClickListener
            //Here used Binding Adapter to bind all views. see search_grid_item.xml
            binding.itemLayout.setOnClickListener {
                userItemClickListener.onClick(item)
            }
        }

        companion object{
            fun from(parent: ViewGroup): ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context) //The parent is the ViewGroup at the root of the layout.
                val view = UserSearchListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(view)
            }
        }

    }

}

class UserItemClickListener(val clickListener: (item: UserSearchResult)-> Unit){
    fun onClick(item: UserSearchResult){
        clickListener(item)
    }
}