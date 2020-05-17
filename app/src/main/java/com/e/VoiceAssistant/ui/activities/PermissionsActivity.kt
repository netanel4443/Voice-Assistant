package com.e.VoiceAssistant.ui.activities

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.e.VoiceAssistant.permissions.RequestCodes
import com.e.VoiceAssistant.permissions.RequestGlobalPermission
import com.e.VoiceAssistant.utils.toastLong

class PermissionsActivity :AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(RequestGlobalPermission.check(this,android.Manifest.permission.RECORD_AUDIO,RequestCodes.RECORD_AUDIO))
        {
            finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when(requestCode) {
                2->{
                    finish()
                }
            }
        }
        else if (grantResults[0]== PackageManager.PERMISSION_DENIED)
        {
            toastLong("Allow in order to use the app")
            finish()
        }
    }
}