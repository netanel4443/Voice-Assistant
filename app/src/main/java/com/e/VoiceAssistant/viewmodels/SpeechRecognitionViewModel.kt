package com.e.VoiceAssistant.viewmodels

import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.data.SavedAppsDetails
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.usecases.SpeechRecognitionUseCases
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.VoiceAssistant.utils.rxJavaUtils.throttle
import com.e.VoiceAssistant.viewmodels.states.SettingsViewModelStates
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class SpeechRecognitionViewModel @Inject constructor(
    private val  useCases: SpeechRecognitionUseCases
): BaseViewModel() {
    private val TAG="SpeechRecognitionViewModel"
    private var appsDetailsHmap=HashMap<String,AppsDetails>()
    private var addedApps= LinkedHashMap<String,Drawable?>()
    private var state=MutableLiveData<SettingsViewModelStates>()
    private var appsToDelete=ArrayList<String>()
    private var selectedApp=AppsDetails("","","",null)
    private var speechResultAppName=""
    private var talkBtnIcon=R.drawable.ic_mic_white_background_24
    private val clickSubject= PublishSubject.create<SettingsViewModelStates>()

    init {
        +clickSubject.throttle()
            .doOnNext { state.value=it }
            .subscribe({},{})
    }

    fun resetState(){
        state.setValue(SettingsViewModelStates.Idle)
    }

    fun getState()=state as LiveData<SettingsViewModelStates>

    fun showDialog(visibility: Int){
        state.value = SettingsViewModelStates.ShowDialog(visibility)
    }

    fun setAppResultText(appName:String){
        speechResultAppName=appName
        state.value = SettingsViewModelStates.SpeechResult(appName)
    }

    fun applySelectedApp(appsDetails: AppsDetails){
        selectedApp=appsDetails
        state.value = SettingsViewModelStates.ApplySelectedApp(appsDetails)
    }

    fun initRecognizerIntent(): Single<Intent> {
        return useCases.initRecognizerIntent()
    }

    fun saveApp(appToBeSaved: SavedAppsDetails, icon:Drawable?){
        /* because we immediately set a new value in MainActivity to appToBeSaved.newName(although kotlin is by value , the object.newname points to same place in memory) ,
       the value changes that's why we need to cache it before it changes*/
        val tmpNewName=appToBeSaved.newName
        +useCases.saveApp(appToBeSaved.newName,appToBeSaved.pckg,appToBeSaved.realName,appToBeSaved.activity)
            .subscribeOnIoAndObserveOnMain()
            .doOnSubscribe { showDialog(View.VISIBLE) }
            .doOnTerminate { showDialog(View.INVISIBLE) }
            .subscribe (
                {
                    if (addedApps.isNotEmpty()){
                        addedApps[tmpNewName]=icon
                    }

                    addItemToAppList(tmpNewName,appToBeSaved.activity,appToBeSaved.pckg,icon)
                },
                {
                    //   it.printStackTrace()
                })
    }

    fun deleteAppFromList(name: String){
        +useCases.deleteApp(name)
            .subscribeOnIoAndObserveOnMain()
            .doOnSubscribe { showDialog(View.VISIBLE) }
            .doOnTerminate { showDialog(View.INVISIBLE) }
            .subscribe(
                {val icon=  addedApps[name]
                    addedApps.remove(name)
                    removeItemFromAppList(name)
                },
                {
                    //   it.printStackTrace()
                })
    }

    private fun removeItemFromAppList(name:String){
        state.value=SettingsViewModelStates.RemoveItemFromAppList(name)
    }
    private fun addItemToAppList( name:String, activityName:String, pckg:String, icon:Drawable?){
        println("app to be saved pck ${pckg}")
        println("app to be saved real ${name}")
        println("app to be saved activity ${activityName}")
        state.value=SettingsViewModelStates.AddItemToAppList(name,activityName,pckg,icon)
    }


    fun getListOrCachedApplist(){
        if (addedApps.isEmpty()){
            getAppsList()
        }
        else
            getCachedAppList()
    }

    fun getCachedAppList():LiveData<LinkedHashMap<String, Drawable?>>{
        return MutableLiveData(addedApps)
    }

    private fun getAppsList(){
        +useCases.getAppsListFromDB()
            .subscribeOnIoAndObserveOnMain()
            .doOnSubscribe { state.setValue(SettingsViewModelStates.ShowDialog(View.VISIBLE)) }
            .subscribe(
                {
                    +useCases.extractAddedAppsIconsFromAppList(it,appsDetailsHmap)
                        .subscribeOnIoAndObserveOnMain()
                        .subscribe({
                            addedApps=it.first
                            appsToDelete=it.second
                            state.setValue(SettingsViewModelStates.PassAppsToFragment(it.first))
                            showDialog(View.INVISIBLE)
                        },{//it.printStackTrace()
                            showDialog(View.INVISIBLE) })
                },
                { //it.printStackTrace()
                    showDialog(View.INVISIBLE)})
    }

    fun initCachedSettingsActivityUI(){
        state.setValue( SettingsViewModelStates.GetCachedData(appsDetailsHmap,selectedApp,speechResultAppName))
    }

    fun initSettingsActivityUI(list:HashMap<String, AppsDetails>) {
        appsDetailsHmap=list
        state.setValue( SettingsViewModelStates.GetAppsDetails(list))
    }

    fun handleTalkOrStopClick() {
       // state.value=SettingsViewModelStates.HandleClick(talkBtnIcon)
        clickSubject.onNext(SettingsViewModelStates.HandleClick(talkBtnIcon))
    }

    fun changeTalkBtnIcon(){
        talkBtnIcon = if (talkBtnIcon== R.drawable.ic_mic_white_background_24){
            R.drawable.ic_pause_white_background_24
        } else{
            R.drawable.ic_mic_white_background_24
        }
        state.value=SettingsViewModelStates.ChangeTalkBtnIcon(talkBtnIcon)
    }

    fun checkIfTalkBtnIconChanged(){
        if (talkBtnIcon==R.drawable.ic_pause_white_background_24){
            clickSubject.onNext(SettingsViewModelStates.HandleClick(talkBtnIcon))
        }
    }

}