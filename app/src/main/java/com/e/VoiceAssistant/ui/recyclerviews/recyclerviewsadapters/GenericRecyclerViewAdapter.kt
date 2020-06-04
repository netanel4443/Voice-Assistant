package com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.ui.recyclerviews.viewholders.GenericViewHolder

open class GenericRecyclerViewAdapter<T> : RecyclerView.Adapter<GenericViewHolder<T>>() {
    protected var items= HashSet<T>()
    var itemClick:((T)->Unit)?=null

    fun attachData(data:HashSet<T>){
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<T> {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(viewType, parent, false)
        return GenericViewHolder(view)
    }

    override fun getItemCount()=items.size

    override fun onBindViewHolder(holder: GenericViewHolder<T>, position: Int) {
        holder.bind(position)
    }

}
