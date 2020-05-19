package com.e.VoiceAssistant.ui.recyclerviews.viewholders

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.ui.recyclerviews.BindRecyclerViewHolder

open class BaseViewHolder(val view: View):RecyclerView.ViewHolder(view),BindRecyclerViewHolder {
    override fun bind(position: Int) {}

}