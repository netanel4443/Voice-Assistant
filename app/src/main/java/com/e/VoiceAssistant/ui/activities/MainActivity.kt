package com.e.VoiceAssistant.ui.activities

import android.Manifest.*
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.data.SavedAppsDetails
import com.e.VoiceAssistant.permissions.RequestGlobalPermission
import com.e.VoiceAssistant.sensors.AddAppSpeechRecognitionHelper
import com.e.VoiceAssistant.ui.fragments.AddedAppsFragment
import com.e.VoiceAssistant.ui.dialogs.CircleProgressBarDialog
import com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters.AppsDetailsRecyclerViewAdapter
import com.e.VoiceAssistant.permissions.RequestCodes
import com.e.VoiceAssistant.permissions.StartActivityToCheckPermission
import com.e.VoiceAssistant.sensors.SpeechStates
import com.e.VoiceAssistant.ui.ads.Adrequest
import com.e.VoiceAssistant.userscollectreddata.AppsDetailsSingleton
import com.e.VoiceAssistant.utils.*
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.VoiceAssistant.utils.rxJavaUtils.throttle
import com.e.VoiceAssistant.viewmodels.SpeechRecognitionViewModel
import com.e.VoiceAssistant.viewmodels.states.SettingsViewModelStates
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject lateinit var appsDetailsSingleton:AppsDetailsSingleton
    private val TAG="MainActivity"
    private lateinit var selectedApp: SavedAppsDetails
    private var selectedIcon:Drawable?=null
    private lateinit var adapter:AppsDetailsRecyclerViewAdapter
    private lateinit var talkIntent: SpeechRecognizer
    private lateinit var intnt: Intent
    private var appsDetailsHmap=HashMap<String,AppsDetails>()
    private val viewModel:SpeechRecognitionViewModel by lazy(this::getViewModel)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        initAds()
        selectedApp= SavedAppsDetails("", "", "", "")
        selectedIcon=getDrawable(R.drawable.ic_launcher_background)
        progressBar = CircleProgressBarDialog(this)

        viewModel.getState().observe(this, Observer{state->
            when(state){ 
                is SettingsViewModelStates.GetAppsDetails->initUI(state.list)
                is SettingsViewModelStates.ShowDialog->showOrHideProgressBar(state.visibility)
                is SettingsViewModelStates.SpeechResult->{
                    newAppNameSettignsActivity.text=state.appName
                    selectedApp.newName=state.appName
                }
                is SettingsViewModelStates.ApplySelectedApp->SetSelectedApp(state.appsDetails)
                is SettingsViewModelStates.GetCachedData->setCachedSettings(state.appDetailsHmap,state.selectedApp,state.speechResultAppName)
                is SettingsViewModelStates.AddItemToAppList-> addItemToServiceAppList(state.name,state.activityName,state.pckg,state.icon,state.realName)
                is SettingsViewModelStates.RemoveItemFromAppList->removeItemfromServiceAppList(state.name)
                is SettingsViewModelStates.ChangeTalkBtnIcon->changeTalkIcon(state.icon)
                is SettingsViewModelStates.HandleClick-> handleClick(state.icon)
            }
        })

        if (savedInstanceState==null){
            initAppsDetails()
        }
        else{
            viewModel.initCachedSettingsActivityUI()
        }

        +RxView.clicks(startTalkSettingsActivity).observeOn(rx.android.schedulers.AndroidSchedulers.mainThread())
            .filter { RequestGlobalPermission.
              check(this, permission.RECORD_AUDIO,RequestCodes.RECORD_AUDIO)
            }
            .subscribe {
                viewModel.handleTalkOrStopClick()
            }

        +RxView.clicks(addBtnSettingsActivity).throttle().subscribe {
            val newName=newAppNameSettignsActivity.text.toString()
            if ( newName.isNotBlank()&&selectedApp.realName.isNotBlank() ){
                 viewModel.saveApp(selectedApp,selectedIcon)
                 viewModel.setAppResultText("")
            }
            else {
                toast("choose an app and record a name")
            }
        }

        +RxView.clicks(showAddedAppsListBtn).throttle().subscribe {
            addFragment(AddedAppsFragment(), R.id.frame_layout,"AddedAppsFragment")
        }
    }

    private fun showOrHideProgressBar(visibility: Int) {
        progressBarLayout.visibility=visibility
    }


    private fun initAds() {
        adView.loadAd(Adrequest().request())
    }

    private fun handleClick(icon: Int) {
        if (StartActivityToCheckPermission.check(this, permission.RECORD_AUDIO)) {
            if (R.drawable.ic_mic_white_background_24 ==icon) {
                talkIntent.startListening(intnt)
                viewModel.changeTalkBtnIcon()
            }
            else {
                talkIntent.stopListening()
                viewModel.changeTalkBtnIcon()
            }
        }
    }

    private fun changeTalkIcon(icon:Int){
        startTalkSettingsActivity.setBackgroundResource(icon)
    }

    private fun addItemToServiceAppList(newName:String, activityName:String, pckg:String, icon:Drawable?, realName:String) {
        val appsDetails=AppsDetails(realName,pckg,activityName,icon)
        appsDetailsSingleton.storedAppsDetailsFromDB[newName]=appsDetails
        appsDetailsSingleton.appsAndStoredAppsDetails[newName]=appsDetails
    }

    private fun removeItemfromServiceAppList(name:String){
        appsDetailsSingleton.storedAppsDetailsFromDB.remove(name)
        appsDetailsSingleton.appsAndStoredAppsDetails.remove(name)
    }

    private fun setCachedSettings(
        appDetailsHmap: HashMap<String, AppsDetails>,
        appDetails: AppsDetails, speechResultAppName: String) {

        selectedApp.realName=appDetails.realName
        selectedApp.activity=appDetails.activity
        selectedApp.pckg=appDetails.pckg
        selectedApp.newName=speechResultAppName
        selectedIcon=appDetails.icon

        selectedAppSettingsActivity.setImageDrawable(selectedIcon)

        newAppNameSettignsActivity.text=speechResultAppName

        initUI(appDetailsHmap)
    }

    private fun SetSelectedApp(appDetails: AppsDetails) {
        selectedApp.realName=appDetails.realName
        selectedApp.activity=appDetails.activity
        selectedApp.pckg=appDetails.pckg
        selectedIcon=appDetails.icon
        selectedAppSettingsActivity.setImageDrawable(selectedIcon)
    }

    private fun initAppsDetails() {
        viewModel.initSettingsActivityUI(appsDetailsSingleton.appsDetailsHmap)
    }

    private fun initUI(list:HashMap<String, AppsDetails>){
        initRecyclerView(list)
        initSpeechRecognizer()
        viewModel.showDialog(View.INVISIBLE)
    }

    private fun initRecyclerView(hMap:HashMap<String,AppsDetails>) {
        appsDetailsHmap=hMap
      //  println("hmp size :${hMap.size}")
        adapter= AppsDetailsRecyclerViewAdapter(hMap){appDetails->
            viewModel.applySelectedApp(appDetails)
        }
        appListRecycler.adapter=adapter
        appListRecycler.layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        appListRecycler.setHasFixedSize(true)
    }

    private fun initSpeechRecognizer(){
        talkIntent= SpeechRecognizer.createSpeechRecognizer(this)
        talkIntent.setRecognitionListener(AddAppSpeechRecognitionHelper {state,result->

            when(state) {
                SpeechStates.Error -> toast(result)
                SpeechStates.ChangeTalkIcon -> {
                    if (result == "changeToTalk")
                        viewModel.checkIfTalkBtnIconChanged()
                }
                SpeechStates.Success -> viewModel.setAppResultText(result)
            }
        })

        +viewModel.initRecognizerIntent()
            .subscribeOnIoAndObserveOnMain()
            .subscribe(
                { intnt=it },
                { //it.printStackTrace()
                })
    }
    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onPause() {
        super.onPause()
        adView.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (adView!=null){
            adView.removeAllViews()
            adView.destroy()
        }
        viewModel.resetState()
     //   println("destroyed")

        talkIntent.destroy()
    }
}
