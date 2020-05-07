package com.e.VoiceAssistant.viewmodels.states

import android.graphics.drawable.Drawable
import com.e.VoiceAssistant.data.AppsDetails

sealed class SettingsViewModelStates {

    object Idle:SettingsViewModelStates()
    data class GetAppsDetails(val list:HashMap<String, AppsDetails>):SettingsViewModelStates()
    data class ShowDialog(val visibility: Int) : SettingsViewModelStates()
    data class SpeechResult(val appName:String) : SettingsViewModelStates()
    data class ApplySelectedApp(val appsDetails: AppsDetails) : SettingsViewModelStates()
    data class PassAppsToFragment(val list:LinkedHashMap<String,Drawable?>) : SettingsViewModelStates()
  //  data class RefreshList(val name:String,val icon:Drawable? ,val addOrRemove: AddOrRemove) : SettingsViewModelStates()
    data class GetCachedData(val appDetailsHmap:HashMap<String,AppsDetails>,
                             val selectedApp: AppsDetails,
                             val speechResultAppName:String ) : SettingsViewModelStates()
    data class RemoveItemFromAppList(val name:String) : SettingsViewModelStates()
    data class AddItemToAppList(val name:String,val activityName:String,val pckg:String,val icon:Drawable?) : SettingsViewModelStates()
    data class ChangeTalkBtnIcon(val icon:Int) : SettingsViewModelStates()
    data class HandleClick(val icon:Int) : SettingsViewModelStates()
}

