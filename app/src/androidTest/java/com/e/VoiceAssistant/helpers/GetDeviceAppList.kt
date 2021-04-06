package com.e.VoiceAssistant.helpers

import android.content.Intent
import android.content.pm.PackageManager
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.ui.activities.TalkAndResultsActivityMviTest
import io.reactivex.Single

class GetDeviceAppList {

    fun getAppsDetails(pm: PackageManager): Single<HashMap<String, AppsDetails>> {

        return  Single.fromCallable {
            val tmphMap=HashMap<String, AppsDetails>()
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val launchables = pm.queryIntentActivities(intent, 0)

            launchables.forEach{resolveInfo ->

                val activity = resolveInfo.activityInfo
                val appName=activity.loadLabel(pm).toString().toLowerCase()
                val pckg=activity.name
                val icon=resolveInfo.loadIcon(pm)
                val activityName=  activity.applicationInfo.packageName
                val appsDetails= AppsDetails(appName,pckg,activityName,icon)
                tmphMap[appName]=appsDetails
//                println(appsDetails)
//                 println("name $appName")
            }
            tmphMap
        }
    }
}