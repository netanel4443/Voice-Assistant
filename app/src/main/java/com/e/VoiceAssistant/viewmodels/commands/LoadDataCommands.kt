package com.e.VoiceAssistant.viewmodels.commands

import com.e.VoiceAssistant.data.AppsDetails

sealed class LoadDataCommands {

    data class StoredAppsDetails(val list:HashMap<String, AppsDetails>): LoadDataCommands()
    data class GetDeviceApps(val list:HashMap<String, AppsDetails>): LoadDataCommands()
    data class GetCurrentLocaleDigits(val digits:String) : LoadDataCommands()
    object LoadComplete :LoadDataCommands()
}