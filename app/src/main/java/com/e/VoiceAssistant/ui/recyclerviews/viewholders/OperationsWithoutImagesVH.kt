package com.e.VoiceAssistant.ui.recyclerviews.viewholders

import android.view.View
import kotlinx.android.synthetic.main.operation_keyword_vh_design.view.*

class OperationsWithoutImagesVH(view: View,
                                private var items:HashSet<String>):GenericViewHolder<String>(view) {
    private val operation=view.operationKeyWord
    override fun bind(position: Int) {
        operation.text=items.elementAt(position)
    }


}