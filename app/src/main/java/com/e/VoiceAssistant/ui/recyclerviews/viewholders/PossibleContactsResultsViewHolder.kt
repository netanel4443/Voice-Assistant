package com.e.VoiceAssistant.ui.recyclerviews.viewholders

import android.content.Intent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ContactsData
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData
import kotlinx.android.synthetic.main.possible_contacts_results_recycler_design.view.*

class PossibleContactsResultsViewHolder(
                                        itemView: View,
                                        items:HashSet<ResultsData>,
                                        val intent: Intent) :BaseViewHolder(itemView) {
    var itemClick:((ResultsData)->Unit)?=null
    val items=items as HashSet<ContactsData>
    val contactDetailsCard=itemView.contactDetailsCardView
    val contactNumber=itemView.contactNumberTview
    val contactName=itemView.contactNameTview

    override fun bind(position:Int) {
        val key=items.elementAt(position)
        contactName.text=key.contact
        contactNumber.text=key.number
    }
    init {
        itemView.setOnClickListener{itemClick?.invoke(items.elementAt(adapterPosition))}
    }

}