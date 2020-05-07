package com.e.VoiceAssistant.ui.recyclerviewsadapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.R
import kotlinx.android.synthetic.main.added_apps_recycler_design.view.*
import kotlin.collections.LinkedHashMap

class AddedAppsRecyclerAdapter(val list:LinkedHashMap<String, Drawable?>, val click:(String)->Unit):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater= LayoutInflater.from(parent.context)
        val view=inflater.inflate(R.layout.added_apps_recycler_design,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int =list.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
      if (holder is ViewHolder){
        val name=list.keys.elementAt(position)
        holder.appName.text=name
        holder.appIcon.setImageDrawable(list.get(name)!!)
      //    printMessage(UUID.randomUUID().toString(),list.get(name)==null)
      }
    }

    private inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
        val appName=view.appNameAddedAppsRecycler as TextView
        val appIcon=view.appIconAddedAppsRecycler as ImageView
        val deleteApp=view.deleteAppAddedAppsRecycler as Button

        init {
            deleteApp.setOnClickListener{click(appName.text.toString())}
        }
    }

}
