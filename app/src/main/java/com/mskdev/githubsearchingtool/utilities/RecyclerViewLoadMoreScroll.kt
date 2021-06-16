package com.mskdev.githubsearchingtool.utilities

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mskdev.githubsearchingtool.`interface`.OnLoadMoreListener
import com.mskdev.githubsearchingtool.utilities.Global.Companion.LOG_TAG
import timber.log.Timber

/*
 * Tutorial  Github: johncodeos-blog/LoadMoreAndroidExample
 */

class RecyclerViewLoadMoreScroll(layoutManager: LinearLayoutManager) :
    RecyclerView.OnScrollListener() {

    private var visibleThreshold = 1
    private lateinit var mOnLoadMoreListener: OnLoadMoreListener
    private var isLoading: Boolean = false
    private var lastVisibleItem: Int = 0
    private var totalItemCount:Int = 0
    private var mLayoutManager: RecyclerView.LayoutManager = layoutManager

    fun setLoaded() {
        isLoading = false
    }

    fun getLoaded(): Boolean {
        return isLoading
    }

    fun setOnLoadMoreListener(mOnLoadMoreListener: OnLoadMoreListener) {
        this.mOnLoadMoreListener = mOnLoadMoreListener
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        if (dy <= 0) return

        totalItemCount = mLayoutManager.itemCount
        //Timber.tag(LOG_TAG).d("totalItemCount: $totalItemCount")
         if (mLayoutManager is LinearLayoutManager) {
            lastVisibleItem = (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
            //Timber.tag(LOG_TAG).d("lastVisibleItem: $lastVisibleItem")
        }

        if (!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
            mOnLoadMoreListener.onLoadMore()
            isLoading = true
            Timber.tag(LOG_TAG).d("Reached at Threshold.........")
        }

    }

}