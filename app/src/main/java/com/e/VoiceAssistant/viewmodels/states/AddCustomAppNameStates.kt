package com.e.VoiceAssistant.viewmodels.states

import android.graphics.drawable.Drawable
import com.e.VoiceAssistant.data.AppsDetails

sealed class AddCustomAppNameStates {

    object Idle:AddCustomAppNameStates()
    data class GetAppsDetails(val list:HashMap<String, AppsDetails>):AddCustomAppNameStates()
    data class ShowDialog(val visibility: Int) : AddCustomAppNameStates()
    data class SpeechResult(val appName:String) : AddCustomAppNameStates()
    data class ApplySelectedApp(val appsDetails: AppsDetails) : AddCustomAppNameStates()
  //  data class RefreshList(val name:String,val icon:Drawable? ,val addOrRemove: AddOrRemove) : AddCustomAppNameStates()
    data class GetCachedData(val appDetailsHmap:HashMap<String,AppsDetails>,
                             val selectedApp: AppsDetails,
                             val speechResultAppName:String ) : AddCustomAppNameStates()
    data class RemoveItemFromAppList(val name:String) : AddCustomAppNameStates()
    data class AddItemToAppList(val name:String,val activityName:String,val pckg:String,val icon:Drawable?,val realName:String) : AddCustomAppNameStates()
    data class ChangeTalkBtnIcon(val icon:Int) : AddCustomAppNameStates()
    data class HandleClick(val icon:Int) : AddCustomAppNameStates()
}

