package com.e.VoiceAssistant.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object RequestGlobalPermission {

    fun check(context: Context,permission:String,requestCode:Int):Boolean {
        if (ContextCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED
        )
            return true
        else
        {
            ActivityCompat.requestPermissions(( context as AppCompatActivity),
                Array<String>(1) { permission }, requestCode)
        }
        return false
    }
}