package com.mskdev.githubsearchingtool.utilities

import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.mskdev.githubsearchingtool.R
import com.mskdev.githubsearchingtool.utilities.Global.Companion.LOG_TAG
import timber.log.Timber
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@BindingAdapter("imageUrl")
fun loadImage(view: ImageView, imageUrl: String?){
    imageUrl?.let {
        Glide.with(view.context)
            .load(imageUrl)
            .into(view)
    }
}

@BindingAdapter("userName")
fun userName(view: TextView, value: String?){
    var text =" "
    if(value.equals(null)){
        text = "Not Specified"
    }else{
        text = "$value"

    }
    view.text = text
}

@BindingAdapter("userEmail")
fun userEmail(view: TextView, value: String?){
    var text =" "
    if(value.equals(null)){
        text = "Email: n/a "
        view.isVisible = false
    }else{
        text = "Email: $value"

    }

    view.text = text
}

@BindingAdapter("userBlog")
fun userBlog(view: TextView, value: String?){
    if(value.equals(null)){
        view.isVisible = false
    }else{
        view.text =  view.resources.getString(R.string.user_blog, value)

    }


}
@BindingAdapter("userLocation")
fun userLocation(view: TextView, value: String?){
    var text =" "
    if(value.equals(null)){
        text = "Location: n/a "
        view.isVisible = false
    }else{
        text = "Location: $value"
    }

    view.text = text
}

@BindingAdapter("userJoinDate")
fun userJoinDate(view: TextView, value: String?){
    if(value.equals(null)){
        view.isVisible = false
    }else{
        try {
            val inputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
            val outputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
            val date: LocalDate = LocalDate.parse(value, inputFormatter)
            val formattedDate: String = outputFormatter.format(date)
            view.text = view.resources.getString(R.string.join_on, formattedDate)

        }catch (e: Exception){
            Timber.tag(LOG_TAG).e("Error: $e")
        }
    }
}

@BindingAdapter("followers")
fun userFollower(view: TextView, value: Int){
    val formatter = NumberFormat.getNumberInstance()
    val text = "${formatter.format(value)}  Followers"
    view.text = text
}

@BindingAdapter("following")
fun userFollowing(view: TextView, value: Int){
    val formatter = NumberFormat.getNumberInstance()
    val text = "Following ${formatter.format(value)}"
    view.text = text
}

@BindingAdapter("userBio")
fun userBio(view: TextView, value: String?){

    if(value.equals(null)){
        view.isVisible = false
    }else{
        view.text = value

    }

}


@BindingAdapter("searchValue")
fun searchView(view: SearchView, value: String?){
    var text =""

}

@BindingAdapter("header")
fun header(view: TextView, value: String?){
    var text = ""
    value?.let {
        text = it
    }
    view.text = text
}

@BindingAdapter("forkCount")
fun forkCount(view: TextView, value: Int){
    view.text = value.toString()
}

@BindingAdapter("starCount")
fun starCount(view: TextView, value: Int){
    view.text = value.toString()
}

@BindingAdapter("fullName")
fun fullName(view: TextView, value: String?){
    var text = ""
    value?.let {
        text = it
    }
    view.text = text
}

@BindingAdapter("description")
fun description(view: TextView, value: String?){
    if (value.isNullOrBlank()){
        view.isVisible = false
    }else{
        view.text = value
    }
}


@BindingAdapter("topics")
fun topics(view: TextView, value: List<String>?){
    try {
        if (value.isNullOrEmpty()) {
            //Timber.tag(LOG_TAG).e("No Topic Found")
            view.isVisible = false
        } else {
            var topicListText = "Topics: "

            for (topic in value) {
                topicListText = topicListText.plus(topic).plus(", ")
            }
            //Timber.tag(LOG_TAG).e("Topics: $topicListText")
            view.text = topicListText
        }

    }catch (e: Exception){
        Timber.tag(LOG_TAG).e("Error: $e")
    }
}

@BindingAdapter("stargazersCount")
fun stargazersCount(view: TextView, value: Long){
    view.text = value.toString()
}

@BindingAdapter("language")
fun language(view: TextView, value: String?){
    var text = ""
    if (value.isNullOrBlank()){
        view.isVisible = false
    }else{

        text = value

        view.text = text
    }

}

@BindingAdapter("updatedAt")
fun updatedAt(view: TextView, value: String?){
    value?.let {
        try {

            val inputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
            val outputFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
            val date: LocalDate = LocalDate.parse(value, inputFormatter)
            val formattedDate: String = outputFormatter.format(date)
            view.text = view.resources.getString(R.string.updated_on, formattedDate)

        }catch (e: Exception){
            Timber.tag(LOG_TAG).e("Error: $e")
        }

    }

}