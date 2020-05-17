package com.e.VoiceAssistant.ui.splashScreen

import android.Manifest.permission
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.data.DeviceAppsDetails
import com.e.VoiceAssistant.permissions.RequestCodes
import com.e.VoiceAssistant.permissions.RequestGlobalPermission
import com.e.VoiceAssistant.ui.activities.BaseActivity
import com.e.VoiceAssistant.ui.services.SpeechRecognizerService.SpeechRecognizerService
import com.e.VoiceAssistant.userscollectreddata.AppsDetailsSingleton
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.VoiceAssistant.utils.toastLong
import com.e.VoiceAssistant.viewmodels.SpeechRecognitionViewModel
import com.e.VoiceAssistant.viewmodels.states.SettingsViewModelStates
import com.tbruyelle.rxpermissions2.RxPermissions
import javax.inject.Inject

class WelcomeSplashActivity : BaseActivity() {

    @Inject lateinit var appsDetailsSingleton: AppsDetailsSingleton
    private val viewModel:SpeechRecognitionViewModel by lazy(this::getViewModel)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initAppsDetails()
        setLiveDataObserver()
    }

    private fun setLiveDataObserver() {
        viewModel.getState().observe(this, Observer{state->
            when(state){
                is SettingsViewModelStates.StoredAppsDetails->addSavedAppsFromMemoryToList(state.list)
            }
        })
    }

    private fun addSavedAppsFromMemoryToList(list:HashMap<String, AppsDetails>) {
          appsDetailsSingleton.storedAppsDetailsFromDB.putAll(list)
          appsDetailsSingleton.appsAndStoredAppsDetails.putAll(list)
          startSpeechRecognizerService()
    }

    private fun startSpeechRecognizerService(){

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            Settings.canDrawOverlays(this)) {
            checkRxPermissions()
        }
        else {
            runtimePermissionForUser()
            Toast.makeText(this, "System Alert Window Permission Is Required For Floating Widget.", Toast.LENGTH_LONG).show();
        }
    }

   private fun checkPermissionAndStartService(){
       if (RequestGlobalPermission.check(this,permission.RECORD_AUDIO,RequestCodes.RECORD_AUDIO)) {
            startService( Intent(this, SpeechRecognizerService::class.java))
           finishAndRemoveTask()
       }
   }

    private fun initAppsDetails() {
        +DeviceAppsDetails().getAppsDetails(this)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({
                    appsDetailsSingleton.appsDetailsHmap.putAll(it)
                    appsDetailsSingleton.appsAndStoredAppsDetails.putAll(it)
                    viewModel.getStoredAppsDetails(it)
                },
                {  // it.printStackTrace()
                })
    }


   private fun checkRxPermissions(){
        val rxPermissions=RxPermissions(this)

       val recordAudioObservable=
           rxPermissions.requestEach(permission.RECORD_AUDIO)
            .doOnNext {
             //   println("numbers")
                 when {
                    it.granted -> {
                         startServiceAndFinishActivity()
                    }
                    it.shouldShowRequestPermissionRationale -> {
                        startServiceAndFinishActivity()
                    }
                    else -> {
                        toastLong(R.string.permission_ask_never_again)
                        finishAndRemoveTask()
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
        finishAndRemoveTask()
    }

   @RequiresApi(Build.VERSION_CODES.M)
   private fun runtimePermissionForUser() {
        val permissionIntent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(permissionIntent, 1)
       finishAndRemoveTask()
   }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
