package com.e.VoiceAssistant.ui.recyclerviews.viewholders

import android.view.View
import kotlinx.android.synthetic.main.possible_matches_recycler_design.view.*

class OperationsWithoutImagesVH(view: View,
                                private var items:HashSet<String>):GenericViewHolder<String>(view) {
    private val operation=view.possibleMatchTview
    override fun bind(position: Int) {
        operation.text=items.elementAt(position)
    }


}