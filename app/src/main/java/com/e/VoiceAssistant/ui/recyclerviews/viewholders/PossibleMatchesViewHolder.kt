package com.e.VoiceAssistant.ui.recyclerviews.viewholders

import android.content.Intent
import android.view.View
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.PossibleMatches
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData
import kotlinx.android.synthetic.main.possible_matches_recycler_design.view.*

class PossibleMatchesViewHolder(view: View,items:HashSet<ResultsData>,val intent: Intent):BaseViewHolder(view) {
    val items = items as HashSet<PossibleMatches>
    val match = view.possibleMatchTview
    var itemClick:((ResultsData)->Unit)?=null

    override fun bind(position: Int) {
        val key = items.elementAt(position)
        match.text = key.match
    }
    init {
        itemView.setOnClickListener{itemClick?.invoke(items.elementAt(adapterPosition))}
    }

}