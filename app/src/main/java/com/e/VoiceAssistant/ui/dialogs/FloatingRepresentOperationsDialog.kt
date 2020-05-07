package com.e.VoiceAssistant.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.ui.recyclerviewsadapters.RepresentOperationsRecyclerAdapter
import kotlinx.android.synthetic.main.recyclerview_layout.view.*

class FloatingRepresentOperationsDialog(val context: Context) {

    private lateinit var adapter:RepresentOperationsRecyclerAdapter
    private val images=context.resources.obtainTypedArray(R.array.operations_images)

    fun show(){
       val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
       val inflater = LayoutInflater.from(context)
       val view=inflater.inflate(R.layout.recyclerview_layout,null)
       val recyclerView=view.representOperationsRecyclerview

        initRecyclerView(recyclerView)

        alertDialog.setView(view)

       val alert=alertDialog.create()

       val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
       }
       else WindowManager.LayoutParams.TYPE_PHONE

       try { alert.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            alert.window!!.setType(layoutFlag)
       }

       catch (e: Exception) { }

        alert.show()

        alert.setOnDismissListener {  }
    }

    private fun initRecyclerView(recyclerView: RecyclerView) {

        val expalanations=context.resources.getStringArray(R.array.operations_explanation)
        adapter=RepresentOperationsRecyclerAdapter(images,expalanations)
        recyclerView.adapter=adapter
        recyclerView.layoutManager=LinearLayoutManager(context,RecyclerView.HORIZONTAL,false)
        recyclerView.setHasFixedSize(true)
    }

//    private fun recycleTypedArray() {
//        images.recycle()
//    }
}