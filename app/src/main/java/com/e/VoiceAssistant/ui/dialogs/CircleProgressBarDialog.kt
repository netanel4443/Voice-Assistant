package com.e.VoiceAssistant.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ProgressBar
import com.e.VoiceAssistant.R

class CircleProgressBarDialog(val context: Context) {
    private val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)
    private val inf: LayoutInflater = LayoutInflater.from(context)
    private val view: View
    private val progressbar: ProgressBar
    private val alert: AlertDialog

    init {
        view = inf.inflate(R.layout.loading_progressbar, null)
        progressbar = view.findViewById(R.id.loadingProgressBar) as ProgressBar
        alertDialog.setView(view)
        alert = alertDialog.create()
        alert.setCancelable(false)
        alert.setCanceledOnTouchOutside(false)

        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
        else WindowManager.LayoutParams.TYPE_PHONE

        try { alert.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

              alert.window!!.setType(layoutFlag)
        }
        catch (e: Exception) { }
    }
    fun showOrHide(visibility: Int){
        if (visibility==View.INVISIBLE)
            dismiss()
        else
            showprogressbar()
    }

    private fun showprogressbar() {
        alert.show()
    }

    private fun dismiss() {
        alert.dismiss()
    }
}