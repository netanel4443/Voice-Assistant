package com.e.VoiceAssistant.permissions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.e.VoiceAssistant.ui.activities.PermissionsActivity

object StartActivityToCheckPermission {

    fun check(context: Context,permission: String):Boolean {
        return if (ContextCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED)
            true
        else{
            startActivityToAskForPermissions(context)
            false
        }
    }

   private fun startActivityToAskForPermissions(context:Context){
        val intent=Intent(context,PermissionsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                           Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
            context.startActivity(intent)
    }
}