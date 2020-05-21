package com.e.VoiceAssistant.ui.recyclerviews.viewholders

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.ui.recyclerviews.BindRecyclerViewHolder

open class GenericViewHolder<T>(var view: View):RecyclerView.ViewHolder(view), BindRecyclerViewHolder {


   open var itemClick:((T)->Unit)?=null

   override fun bind(position: Int) {}


}