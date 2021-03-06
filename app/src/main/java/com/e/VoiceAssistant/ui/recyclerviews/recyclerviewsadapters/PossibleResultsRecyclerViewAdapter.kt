package com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData
import com.e.VoiceAssistant.ui.recyclerviews.viewholders.BaseViewHolder
import com.e.VoiceAssistant.ui.recyclerviews.viewholders.PossibleContactsResultsViewHolder
import com.e.VoiceAssistant.ui.recyclerviews.viewholders.PossibleMatchesViewHolder
import kotlin.collections.HashSet

class PossibleResultsRecyclerViewAdapter():RecyclerView.Adapter<BaseViewHolder>() {
   private var items= HashSet<ResultsData>()
   private var dataType=0
   private var intent:Intent=Intent()
   var itemClick:((ResultsData)->Unit)?=null

    fun attachData(data:HashSet<ResultsData>,type:Int,intent: Intent){
        this.intent=intent
        attachData(data,type)
    }

    fun attachData(data:HashSet<ResultsData>,type:Int){
        dataType=type
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val view: View
        val holder= when(viewType){
            0->{
                view=inflater.inflate(R.layout.possible_contacts_results_recycler_design,parent,false)
                val viewHolder= PossibleContactsResultsViewHolder(view,items,parent)
                viewHolder.itemClick={itemClick?.invoke(it)}
                viewHolder
            }
            else->{
                view= inflater.inflate(R.layout.possible_matches_recycler_design,parent,false)
                val viewHolder=PossibleMatchesViewHolder(view,items,parent)
                viewHolder.itemClick={itemClick?.invoke(it)}
                viewHolder
            }
        }
        return holder
    }

    override fun getItemCount()=items.size

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemViewType(position: Int): Int {
        when (dataType) {
            0 -> return 0
            else -> return 1
        }
    }
}