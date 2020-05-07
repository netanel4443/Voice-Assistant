package com.e.VoiceAssistant.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.view.WindowManager
import com.e.VoiceAssistant.R

class DescriptionDialog(val context: Context,message:String,title:String) {
 private val alert:AlertDialog
    init {
        val alertDialog=AlertDialog.Builder(context)

        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
        else WindowManager.LayoutParams.TYPE_PHONE
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alert=alertDialog.create()
        try {alert.window!!.setType(layoutFlag) }
        catch (e: Exception) { }
    }
    fun show(){

        alert.show()
    }

}