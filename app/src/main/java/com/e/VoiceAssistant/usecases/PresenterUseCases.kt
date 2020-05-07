package com.e.VoiceAssistant.usecases

import android.app.SearchManager
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.view.WindowManager
import com.e.VoiceAssistant.ComponentObject
import com.e.VoiceAssistant.data.SavedAppsDetails
import com.e.VoiceAssistant.data.repo.RealmRepo
import com.e.VoiceAssistant.di.annotations.ServiceScope
import com.e.VoiceAssistant.usecases.commons.RecognizerIntentInit
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashSet

@ServiceScope
class PresenterUseCases @Inject constructor(
    private val repo:RealmRepo
) {
    private val requiredOperationsHset= hashSetOf("call","התקשר","תקשר","תתקשר","search","פתח","אופן","open","חפש")

    fun windowManagerAttributes():WindowManager.LayoutParams{
        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
        else WindowManager.LayoutParams.TYPE_PHONE
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
    }
    fun windowManagerFullScreenAttributes():WindowManager.LayoutParams{
        val layoutFlag: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
        else WindowManager.LayoutParams.TYPE_PHONE

        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
    }

    fun initRecognizerIntent():Single<Intent> {
        return  RecognizerIntentInit().init()
    }
    fun deleteApps(list:ArrayList<String>): Completable {
        return repo.deleteAppsFromDB(list)
    }

    fun getAppsListFromDB():Single<HashMap<String, SavedAppsDetails>>{
        return repo.getAppsList()
    }

    fun extractAddedAppsFromAppList(
        list:HashMap<String, SavedAppsDetails>,
        appsDetailsHmap:MutableSet<String>) :Single<Pair<HashMap<String,Pair<String,String>>,ArrayList<String>>>{
        return Single.fromCallable{
            val addedApps=HashMap<String,Pair<String,String>>()//<newName,Pair<activity,package>>
            val deletedAps=ArrayList<String>()
            list.forEach {
                val realAppName=it.value.realName
                val newName=it.value.newName
                if(appsDetailsHmap.contains(realAppName)){
                    val activityName=list[newName]!!.activity
                    val pckg=list[newName]!!.pckg
                    addedApps[newName]=Pair(activityName,pckg)
                }
                else
                /* if a user deleted an app for his device we won't
                show its name to it and remove it from the database to prevent bugs.*/
                    deletedAps.add(newName)
            }
            Pair(addedApps,deletedAps)
        }
    }

    fun returnRequiredOperationIntent(
        matches: ArrayList<String>,
        appComponent: HashMap<String, ComponentObject>,
        contactList: HashMap<String,String>): Maybe<Intent> {

        val splitedResultsLhset = LinkedHashSet<String>()
        var requiredOperation = ""

        return Single.fromCallable {
            matches.forEach {
             //   println("matches $it")
            val st = StringTokenizer(it, " ")

            while (st.hasMoreTokens()) {
                val nextToken = st.nextToken().toLowerCase()

                if (requiredOperation.isEmpty() && requiredOperationsHset.contains(nextToken))
                    requiredOperation = nextToken
                  //  println("next token $requiredOperation")

                    splitedResultsLhset.add(nextToken)
                 }
            }
            Pair(requiredOperation,splitedResultsLhset)
        }
         .filter{requiredOperation.isNotEmpty()}
         .map { whichOperationToPerform(it.first,it.second,matches,appComponent,contactList) }
    }

    private fun whichOperationToPerform(requiredOperation:String,
                                splitedMatches:LinkedHashSet<String>,
                                matches: ArrayList<String>,
                                appComponent: HashMap<String, ComponentObject>,
                                contactList: HashMap<String, String>):Intent{
      return if(requiredOperation=="open"||requiredOperation=="אופן"||requiredOperation=="פתח"){
                    OpenDesiredAppPresenterUseCase().getDesiredIntent(appComponent,splitedMatches)
           }
           else if (requiredOperation=="התקשר"||requiredOperation=="תתקשר" ||
                    requiredOperation=="תקשר" || requiredOperation=="call"){

                    callTo(matches[0],requiredOperation,contactList)
           }
           else  {
                    searchInWeb(matches[0],requiredOperation)
           }
    }

    private fun callTo(stringToSearch:String,requiredOperation: String,contactList: HashMap<String, String>):Intent{
        var operation=requiredOperation
        var contactName:String?=""
        if (Locale.getDefault().displayCountry=="ישראל")
        {
            operation=requiredOperation+" "+"ל"
            contactName=stringToSearch.substring(stringToSearch.indexOf(operation,0)).run{
               removePrefix(operation)
               .removePrefix(" ")
               .removeSuffix(" ")
            }
        }else
            contactName=stringToSearch.substring(stringToSearch.indexOf(operation,0)).run {
                replace(operation,"")
                .removePrefix(" ")
            }

            contactName=if (contactList[contactName]==null) { "" }
                        else { contactList[contactName] }


        val intent=Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$contactName")
        return intent
    }

    private fun searchInWeb(stringToSearch:String,requiredOperation: String):Intent{
        val finalStringToSearch=
            stringToSearch.substring(stringToSearch.indexOf(requiredOperation,0))
            .run {
                replace(requiredOperation,"")
            }

        val intent =  Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, finalStringToSearch)
        return intent
    }
}