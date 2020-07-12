package com.e.VoiceAssistant.userscollecteddata

import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.di.annotations.AppScope
import javax.inject.Inject

@AppScope
class AppsDetailsSingleton @Inject constructor() {
    val appsDetailsHmap=HashMap<String,AppsDetails>()
    val storedAppsDetailsFromDB=HashMap<String,AppsDetails>()
    val appsAndStoredAppsDetails=HashMap<String,AppsDetails>()
    val contactList=HashMap<String, String>()
    var countryLocaleDigits=""
}

