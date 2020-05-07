package com.e.VoiceAssistant.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object CheckOnlyPerrmission {

    fun check(context: Context, permission: String):Boolean {
        return ContextCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED
    }
}