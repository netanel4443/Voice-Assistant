package com.e.VoiceAssistant.usecases

import android.content.Intent
import android.graphics.drawable.Drawable
import com.e.VoiceAssistant.data.SavedAppsDetails
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.data.repo.RealmRepo
import com.e.VoiceAssistant.di.annotations.ActivityScope
import com.e.VoiceAssistant.usecases.commons.RecognizerIntentInit
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

//todo add a proper scope
@ActivityScope
class SpeechRecognitionUseCases @Inject constructor(
    private val repo: RealmRepo
) {

    fun initRecognizerIntent():Single<Intent> {
      return  RecognizerIntentInit().init()
    }

    fun saveApp(newName:String,pckg:String,realName:String,activityName:String):Completable{
        return repo.saveOrUpdateAppList(newName,pckg,realName,activityName)
    }

    fun deleteApp(name:String):Completable{
    return repo.deleteApp(name)
    }



    fun getAppsListFromDB():Single<HashMap<String, SavedAppsDetails>>{
        return repo.getAppsList()
    }

     fun extractAddedAppsIconsFromAppList(
         list:HashMap<String, SavedAppsDetails>,
         appsDetailsHmap:HashMap<String, AppsDetails>) :Single<Pair<LinkedHashMap<String,Drawable?>,ArrayList<String>>>{
      return Single.fromCallable{
          val addedApps=LinkedHashMap<String,Drawable?>()
          val deletedAps=ArrayList<String>()
          list.forEach {
              val realAppName=it.value.realName
              val newName=it.key
              if(appsDetailsHmap.contains(realAppName))
                  addedApps[newName]=appsDetailsHmap[realAppName]!!.icon
              else
              /* if a user deleted an app for his device we won't
              show its name to it and remove it from the database to prevent bugs.*/
                  deletedAps.add(newName)
          }
         Pair(addedApps,deletedAps)
      }
    }


}
