package com.e.VoiceAssistant.userscollectreddata

import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.di.annotations.AppScope
import javax.inject.Inject

@AppScope
class AppsDetailsSingleton @Inject constructor() {
    val appsDetailsHmap=HashMap<String,AppsDetails>()
    val storedAppsDetailsFromDB=HashMap<String,AppsDetails>()
    val appsAndStoredAppsDetails=HashMap<String,AppsDetails>()
    var countryLocaleDigits=""
}

