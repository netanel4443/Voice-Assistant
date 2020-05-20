package com.e.VoiceAssistant.usecases

import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.data.SavedAppsDetails
import com.e.VoiceAssistant.data.repo.RealmRepo
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class LoadDataUseCase @Inject constructor(
    private val repo: RealmRepo
) {

    fun getAppsListFromDB(): Single<HashMap<String, SavedAppsDetails>> {
        return repo.getAppsList()
    }

    fun addToSavedAppsProperIcon(
        list: HashMap<String, SavedAppsDetails>,
        appsDetailsHmap: HashMap<String, AppsDetails>
    ): Single<HashMap<String, AppsDetails>> {
        val appListToDelete = ArrayList<String>()
        return Single.fromCallable {
            val addedApps = HashMap<String, AppsDetails>()
            //val appsDetails=AppsDetails()//todo check if can be initialized once
            list.forEach {
                val realAppName = it.value.realName
                val newName = it.key
                if (appsDetailsHmap.contains(realAppName)) {
                    val pckg = appsDetailsHmap[realAppName]!!.pckg
                    val activityName = appsDetailsHmap[realAppName]!!.activity
                    val icon = appsDetailsHmap[realAppName]!!.icon
                    val appsDetails = AppsDetails(realAppName, pckg, activityName, icon)
                    addedApps[newName] = appsDetails
                } else
                /* if a user deleted an app for his device we won't
                show its name to it and remove it from the database to prevent bugs.*/
                    appListToDelete.add(newName)
            }
            addedApps
        }.flatMap { deleteApps(appListToDelete).toSingleDefault(it) }
    }

    private fun deleteApps(list:ArrayList<String>): Completable {
        return repo.deleteAppsFromDB(list)
    }
}