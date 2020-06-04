package com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.ui.recyclerviews.viewholders.GenericViewHolder
import com.e.VoiceAssistant.ui.recyclerviews.viewholders.GenericViewHolderHashMap

open class GenericRecyclerViewAdapterHashMap<K,V> : RecyclerView.Adapter<GenericViewHolderHashMap<K, V>>() {
    protected var items= HashMap<K,V>()
    var itemClick:((K,V)->Unit)?=null

    fun attachData(data:HashMap<K,V>){
        items.clear()
        items.putAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolderHashMap<K,V> {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(viewType, parent, false)
        return GenericViewHolderHashMap(view)
    }

    override fun getItemCount()=items.size

    override fun onBindViewHolder(holder: GenericViewHolderHashMap<K,V>, position: Int) {
        holder.bind(items.keys.elementAt(position),items.values.elementAt(position))
    }

}
