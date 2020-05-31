package com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.ui.recyclerviews.viewholders.GenericViewHolder
import com.e.VoiceAssistant.ui.recyclerviews.viewholders.OperationsWithoutImagesVH

class OperationsKeyWordsAdapter: GenericRecyclerViewAdapter<String>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<String> {
        val inflater=LayoutInflater.from(parent.context)
        val view=inflater.inflate(R.layout.operation_keyword_vh_design,parent,false)
        return OperationsWithoutImagesVH(view,items)
    }

}