package com.e.VoiceAssistant.data

import android.content.Context
import android.content.Intent
import io.reactivex.Single
import kotlin.collections.HashMap

class DeviceAppsDetails{
    fun getAppsDetails(context: Context):Single<HashMap<String,AppsDetails>>{

    return  Single.fromCallable {
            val tmphMap=HashMap<String,AppsDetails>()
            val pm = context.packageManager
            val main = Intent(Intent.ACTION_MAIN, null)

                main.addCategory(Intent.CATEGORY_LAUNCHER)

            val launchables = pm.queryIntentActivities(main, 0)

            launchables.forEach{resolveInfo ->

                val activity = resolveInfo.activityInfo
                val appName=activity.loadLabel(pm).toString().toLowerCase()
                val pckg=activity.name
                val icon=resolveInfo.loadIcon(pm)
                val activityName=  activity.applicationInfo.packageName
                val appsDetails=AppsDetails(appName,pckg,activityName,icon)
                    tmphMap[appName]=appsDetails
               // println("name $appName")
            }
        tmphMap
        }
    }
}