package com.e.VoiceAssistant.viewmodels.commands

import com.e.VoiceAssistant.data.AppsDetails

sealed class LoadDataCommands {

    data class StoredAppsDetails(val list:HashMap<String, AppsDetails>): LoadDataCommands()
}