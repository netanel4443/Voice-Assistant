package com.e.VoiceAssistant.usecases

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Build
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.data.SavedAppsDetails
import com.e.VoiceAssistant.data.repo.RealmRepo
import io.reactivex.Completable
import io.reactivex.Observable
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
    fun getAppsDetails(pm: PackageManager): Single<HashMap<String, AppsDetails>> {

        return  Single.fromCallable {
            val tmphMap=HashMap<String,AppsDetails>()
            val intent = Intent(Intent.ACTION_MAIN, null)
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            val launchables = pm.queryIntentActivities(intent, 0)

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

    fun getCurrentLocale(resources: Resources): Observable<String> {
        return  Observable.fromCallable {
            val CountryID = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                resources.configuration.locales.get(0).country
            } else {
                resources.configuration.locale.country
            }

            var countryZipCode = ""

            val rl = resources.getStringArray(R.array.CountryCodes)
            for (i in rl.indices) {
                val g = rl[i].split(",").toTypedArray()
                if (g[1].trim { it <= ' ' } == CountryID.trim()) {
                    countryZipCode = g[0]
                    //    println("CountryZipCode $countryZipCode")
                    break
                }
            }
            countryZipCode
        }
    }
}