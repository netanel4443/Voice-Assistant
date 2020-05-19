package com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters

import android.content.res.TypedArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.R
import kotlinx.android.synthetic.main.represent_operations_recycler_design.view.*

class RepresentOperationsRecyclerAdapter(
            val images:TypedArray,
            val explanations:Array<String>):RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    val inflater=LayoutInflater.from(parent.context)
    val view=inflater.inflate(R.layout.represent_operations_recycler_design,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount()=explanations.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ViewHolder){
            holder.image.setImageResource(images.getResourceId(position,R.drawable.ic_android_black_24dp))
            holder.explanationText.text= explanations[position]
        }
    }

    private inner class ViewHolder(view: View):RecyclerView.ViewHolder(view){
       val image=view.representOperationIconImgView
       val explanationText=view.explainOperationTview
    }
}