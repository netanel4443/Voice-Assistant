package com.e.VoiceAssistant.ui.recyclerviews.viewholders

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.PossibleMatches
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData
import com.e.VoiceAssistant.utils.printIfDebug
import com.e.VoiceAssistant.utils.rxJavaUtils.throttle
import com.jakewharton.rxbinding3.view.clicks
import com.jakewharton.rxbinding3.view.detaches
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.possible_matches_recycler_design.view.*
import java.util.concurrent.TimeUnit

class PossibleMatchesViewHolder(view: View,items:HashSet<ResultsData>,val intent: Intent,val parent:ViewGroup):BaseViewHolder(view) {
    val items = items as HashSet<PossibleMatches>
    val match = view.possibleMatchTview
    var itemClick:((ResultsData)->Unit)?=null
    override fun bind(position: Int) {
        val key = items.elementAt(position)
        match.text = key.match
    }
    init {
  //      itemView.clicks().throttle().subscribe({
        itemView.setOnClickListener {
            itemClick?.invoke(items.elementAt(adapterPosition))
        }
   //     }) {
     //       printIfDebug("PossibleMatchesViewHolder",it.message)
     //   }
    }
}