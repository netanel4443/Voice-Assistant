package com.e.VoiceAssistant.ui.services.SpeechRecognizerService

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import com.e.VoiceAssistant.data.ComponentObject
import io.reactivex.Observable
import java.util.*

class AppAndPackageList {

    fun init(context: Context):Observable< HashMap<String, ComponentObject>>{
        val appcomponent=HashMap<String, ComponentObject>()
     return Observable.fromCallable {

            val pm = context.packageManager
            val main = Intent(Intent.ACTION_MAIN, null)

            main.addCategory(Intent.CATEGORY_LAUNCHER)

            val launchables = pm.queryIntentActivities(main, 0)

            Collections.sort(launchables,
                ResolveInfo.DisplayNameComparator(pm)
            )
            launchables.forEachIndexed { index, resolveInfo ->
                val launchable: ResolveInfo = resolveInfo
                val activity = launchable.activityInfo

                val name = ComponentObject(
                    activity.applicationInfo.packageName,
                    activity.name
                )
                //println("activity: ${activity.name} \n ${activity.applicationInfo.packageName}")
                val appname=activity.loadLabel(pm).toString().toLowerCase()
                appcomponent[appname]=name
            }
            appcomponent
        }
    }
}