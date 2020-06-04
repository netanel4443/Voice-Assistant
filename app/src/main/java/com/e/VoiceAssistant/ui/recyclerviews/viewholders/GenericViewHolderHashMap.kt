package com.e.VoiceAssistant.ui.recyclerviews.viewholders

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.ui.recyclerviews.BindRecyclerViewHolder

open class GenericViewHolderHashMap<K,V>(var view: View):RecyclerView.ViewHolder(view) {

    open var itemClick:((K,V)->Unit)?=null

    open fun bind(itemKey: K,itemValue:V) {}

}