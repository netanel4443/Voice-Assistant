package com.e.VoiceAssistant.ui.splashScreen

import android.Manifest.*
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.permissions.RequestCodes
import com.e.VoiceAssistant.permissions.SpeechRecognitionPermission
import com.e.VoiceAssistant.ui.activities.BaseActivity
import com.e.VoiceAssistant.ui.services.SpeechRecognizerService.SpeechRecognizerService
import com.e.VoiceAssistant.utils.toast
import com.e.VoiceAssistant.utils.toastLong
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import java.util.jar.Manifest

class WelcomeSplashActivity : DaggerAppCompatActivity() {

    private val compositeDisposable=CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startSpeechRecognizerService()

    }
    private fun startSpeechRecognizerService(){

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            Settings.canDrawOverlays(this))
        {
//           SpeechRecognitionPermission.check(this,permission.READ_CONTACTS,RequestCodes.READ_CONTACTS)
//           checkPermissionAndStartService()
            checkRxPermissions()
        }
        else {
            runtimePermissionForUser()
            Toast.makeText(this, "System Alert Window Permission Is Required For Floating Widget.", Toast.LENGTH_LONG).show();
        }
    }

   private fun checkPermissionAndStartService(){
       if (SpeechRecognitionPermission.check(this,permission.RECORD_AUDIO,RequestCodes.RECORD_AUDIO)) {
            startService( Intent(this, SpeechRecognizerService::class.java))
            finish()
       }
   }
    fun checkRxPermissions(){
        val rxPermissions=RxPermissions(this)

       val recordAudioObservable=
           rxPermissions.requestEach(  permission.RECORD_AUDIO)
            .doOnNext {
                println("numbers")
                 when {
                     it.granted -> {
                         startServiceAndFinishActivity()
                    }
                    it.shouldShowRequestPermissionRationale -> {
                        startServiceAndFinishActivity()
                    }
                    else -> {
                        toastLong(R.string.permission_ask_never_again)
                        finish()
                    }
                }
            }
               compositeDisposable.add(rxPermissions.requestEach(permission.READ_CONTACTS)
                   .doOnNext {
                       when {
                           it.granted ->{}
                           it.shouldShowRequestPermissionRationale ->{}
                           else ->  toastLong(R.string.permission_ask_never_again)
                       }
                   }
                   .concatWith(recordAudioObservable)
                   .subscribe ({},{}))
    }
    private fun startServiceAndFinishActivity(){
        startService( Intent(this, SpeechRecognizerService::class.java))
        finish()
    }


    @RequiresApi(Build.VERSION_CODES.M)
   private fun runtimePermissionForUser() {
        val permissionIntent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(permissionIntent, 1)
        finish()
   }

//    override fun onRequestPermissionsResult(
//        requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            when(requestCode) {
//               RequestCodes.RECORD_AUDIO->{
//                    startService( Intent(this, SpeechRecognizerService::class.java))
//                    finish()
//                }
//            }
//        }
//        else if (grantResults[0]==PackageManager.PERMISSION_DENIED)
//        {
//            when(requestCode) {
//                RequestCodes.RECORD_AUDIO->{
//                    toastLong("Allow in order to use the app")
//                    finish()
//                }
//            }
//
//        }
//
//    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
