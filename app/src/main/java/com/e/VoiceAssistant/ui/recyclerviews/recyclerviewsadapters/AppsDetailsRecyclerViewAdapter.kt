package com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.data.AppsDetails
import kotlinx.android.synthetic.main.app_details_recyclerview_design.view.*

class AppsDetailsRecyclerViewAdapter(val detailsMap: HashMap<String, AppsDetails>,
                                     val click:(AppsDetails)->Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater=LayoutInflater.from(parent.context)
    val view=inflater.inflate(R.layout.app_details_recyclerview_design,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount():Int=detailsMap.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder){
            val key=detailsMap.keys.elementAt(position)
            holder.appName.text=key
            holder.appIcon.setImageDrawable(detailsMap[key]!!.icon)
        }
    }

    private inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val appName=view.appName
        val appIcon=view.appIcon
        val mainLayout=view.mainLayoutDetailsRecyclerView

        init {
            mainLayout.setOnClickListener {
                val appNameText=appName.text.toString()
                val appDetails=detailsMap[appNameText]!!
                click(appDetails)
            }
        }
    }
}