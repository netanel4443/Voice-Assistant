package com.e.VoiceAssistant.ui.splashScreen

import android.Manifest.permission
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.RequiresApi
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.ui.activities.BaseActivity
import com.e.VoiceAssistant.ui.services.SpeechRecognizerService.SpeechRecognizerService
import com.e.VoiceAssistant.userscollectreddata.AppsDetailsSingleton
import com.e.VoiceAssistant.utils.toastLong
import com.e.VoiceAssistant.viewmodels.LoadDataViewModel
import com.e.VoiceAssistant.viewmodels.commands.LoadDataCommands
import com.tbruyelle.rxpermissions2.RxPermissions
import javax.inject.Inject

class LoadDataSplashActivity : BaseActivity() {

    @Inject lateinit var appsDetailsSingleton: AppsDetailsSingleton
    private val viewModel: LoadDataViewModel by lazy(this::getViewModel)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        /*we check this because the user can click on the app again an the data will be reload*/
//        if (appsDetailsSingleton.countryLocaleDigits.isEmpty()) {
            setRxObserver()
            viewModel.getData(this.packageManager,this.resources)
//        } else{
//            finish()
//        }
    }

    private fun setRxObserver() {
        +viewModel.commands.subscribe { commands->
            when(commands){
                is LoadDataCommands.GetDeviceApps-> initAppsDetails(commands.list)
                is LoadDataCommands.StoredAppsDetails-> addSavedAppsFromMemoryToList(commands.list)
                is LoadDataCommands.GetCurrentLocaleDigits-> getLocaleDigits(commands.digits)
                is LoadDataCommands.LoadComplete-> onLoadDataComplete()
            }
        }
    }

    private fun getLocaleDigits(digits: String) {
        appsDetailsSingleton.countryLocaleDigits=digits
    }

    private fun initAppsDetails(apps: HashMap<String, AppsDetails>) {
        appsDetailsSingleton.appsDetailsHmap.putAll(apps)
        appsDetailsSingleton.appsAndStoredAppsDetails.putAll(apps)
    }

    private fun addSavedAppsFromMemoryToList(list:HashMap<String, AppsDetails>) {
        appsDetailsSingleton.storedAppsDetailsFromDB.putAll(list)
        appsDetailsSingleton.appsAndStoredAppsDetails.putAll(list)
    }

    private fun onLoadDataComplete(){
        startSpeechRecognizerService()
    }

    private fun startSpeechRecognizerService(){

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
            Settings.canDrawOverlays(this)) {
            checkRxPermissions()
        }
        else {
            runtimePermissionForUser()
            toastLong(R.string.floating_widget_permission)
        }
    }



   private fun checkRxPermissions(){
        val rxPermissions=RxPermissions(this)

       val recordAudioObservable=
           rxPermissions.requestEach(permission.RECORD_AUDIO)
            .doOnNext {
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
        finish()
   }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }
}
