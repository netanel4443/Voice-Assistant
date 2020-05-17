package com.e.VoiceAssistant.usecases

import android.app.SearchManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.WindowManager
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.di.annotations.ServiceScope
import com.e.VoiceAssistant.usecases.commons.RecognizerIntentInit
import io.reactivex.Maybe
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import kotlin.collections.LinkedHashSet

@ServiceScope
class PresenterUseCases @Inject constructor() {
    private val requiredOperationsHset=
        hashSetOf("הודעה","text","וואטסאפ","whatsapp",
            "נווט","navigate","youtube","יוטיוב","ספוטיפיי","spotify",
            "call","התקשר","תקשר","תתקשר","search", "פתח","אופן","open","חפש")

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

    fun returnRequiredOperationIntent(
        matches: ArrayList<String>,
        appComponent: HashMap<String, AppsDetails>,
        contactList: HashMap<String,String>): Maybe<Pair<String,Intent>> {

        val splitedResultsLhset = LinkedHashSet<String>()
        var requiredOperation = ""

        return Single.fromCallable {
            matches.forEach {
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
         .map {
             val intent= whichOperationToPerform(it.first,it.second,matches,appComponent,contactList)
            Pair(requiredOperation,intent)
         }
    }

    private fun whichOperationToPerform(requiredOperation:String,
                                        splitedMatches:LinkedHashSet<String>,
                                        matches: ArrayList<String>,
                                        appComponent: HashMap<String, AppsDetails>,
                                        contactList: HashMap<String, String>):Intent{

      return if(requiredOperation=="open"||requiredOperation=="אופן"||requiredOperation=="פתח"){
                    OpenDesiredAppPresenterUseCase().getDesiredIntent(appComponent,splitedMatches)
           }
           else if (requiredOperation=="התקשר"||requiredOperation=="תתקשר" ||
                    requiredOperation=="תקשר" || requiredOperation=="call"){

                    callTo(matches,requiredOperation,contactList)
           }
           else if (requiredOperation=="spotify" || requiredOperation=="ספוטיפיי"){
                    searchInSpotify(appComponent,matches[0],requiredOperation)
            }
           else if (requiredOperation=="youtube" || requiredOperation=="יוטיוב"){
                    searchInYoutube(matches[0],requiredOperation)
           }
           else if (requiredOperation=="navigate"|| requiredOperation=="נווט"){
                    navigateTo(requiredOperation, matches[0])
           }
           else if (requiredOperation=="text" ||requiredOperation=="הודעה"){
                    sendSms(matches,requiredOperation,contactList)
           }
           else if (requiredOperation=="וואטסאפ"||requiredOperation=="whatsapp"){
                    sendWhatsApp(matches,requiredOperation,contactList)
           }
           else  {
                    searchInWeb(matches[0],requiredOperation)
           }
    }

    fun sendSms(matches:ArrayList<String>, requiredOperation: String, contactList: HashMap<String, String>):Intent{
        var contactNamee:String?=""
        var k=findContact(matches,contactList,requiredOperation)
            .subscribe({
                contactNamee=contactList[it]
                println(it)
            },{//it.printStackTrace()
            })
        println("concatname $contactNamee")
        val intent=Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("smsto:$contactNamee")
        return intent
    }

    private fun callTo(matches:ArrayList<String>, requiredOperation: String, contactList: HashMap<String, String>):Intent{
        var contactNamee:String?=""
        var k=findContact(matches,contactList,requiredOperation)
             .subscribe({
                 contactNamee=contactList[it]
                 println(it)
             },{it.printStackTrace()})
//        if (Locale.getDefault().displayCountry=="ישראל")
//        {
//            operation=requiredOperation+" "+"ל"
//            contactName=stringToSearch.substring(stringToSearch.indexOf(operation,0)).run{
//               removePrefix(operation)
//               .removePrefix(" ")
//               .removeSuffix(" ")
//            }
//        }else
//            contactName=stringToSearch.substring(stringToSearch.indexOf(operation,0)).run {
//                replace(operation,"")
//                .removePrefix(" ")
//            }
//
//            contactName=if (contactList[contactName]==null) { "" }
//                        else { contactList[contactName] }
      //  println("concatname $contactNamee")
        val intent=Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$contactNamee")
        return intent
    }

    private fun searchInWeb(query: String,requiredOperation: String):Intent{
        val finalQuery=getFinalQuery(query,requiredOperation)
        val intent =  Intent(Intent.ACTION_WEB_SEARCH)
        intent.putExtra(SearchManager.QUERY, finalQuery)
        return intent
    }

   private fun searchInSpotify( appComponent: HashMap<String, AppsDetails>,query: String,requiredOperation: String):Intent{
       val finalQuery=getFinalQuery(query,requiredOperation)
       return  appComponent["spotify"]?.run {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.action = MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH
                    intent.component = ComponentName(activity, pckg)
                    intent.putExtra(SearchManager.QUERY, finalQuery)
                    intent
           } ?:
          return run{
               val url = "https://open.spotify.com/"
               val intent = Intent(Intent.ACTION_VIEW)
                   intent.data = Uri.parse(url)
                   intent
        }
    }

    private fun searchInYoutube(query:String,requiredOperation: String):Intent{
        val finalQuery= getFinalQuery(query,requiredOperation)
        return Intent(Intent.ACTION_SEARCH).run {
           setPackage("com.google.android.youtube")
           putExtra(SearchManager.QUERY, finalQuery)
           flags = Intent.FLAG_ACTIVITY_NEW_TASK
           this
        }
    }

    private fun navigateTo(requiredOperation: String,location:String):Intent {
         val finalLocation=removeUnwantedPrefixFromResult(requiredOperation,location)
        println("finalLocation $finalLocation")
         val intent=Intent(Intent.ACTION_VIEW)
         val url="https://waze.com/ul?q=$finalLocation"
             intent.data=Uri.parse(url)
         return intent
    }

    private fun sendWhatsApp(matches:ArrayList<String>, requiredOperation: String, contactList: HashMap<String, String>):Intent{
        var contactName:String?=""
        var dispose= findContact(matches,contactList,requiredOperation)
           .subscribe({contactName=contactList[it]},{})
        val uri =  Uri.parse(
            String.format("https://api.whatsapp.com/send?phone=%s", contactName)
        )
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data= uri
        return intent
    }

    /* this function deletes the keyword in order to get pure string the use wants to search.
* In addition it deletes all previous words before desired keyword*/
    private fun getFinalQuery(query: String,requiredOperation: String):String{
          return  query.substring(query.indexOf(requiredOperation,0))
                .run { replace(requiredOperation,"")
                    .removeSuffix(" ")
                    .removePrefix(" ")
                }
    }

    private fun removeUnwantedPrefixFromResult(requiredOperation: String, result:String):String{
        var operation=requiredOperation
        var finalResult:String?=""
        if (Locale.getDefault().displayCountry=="ישראל")
        {
            operation=requiredOperation+" "+"ל"
            finalResult=result.substring(result.indexOf(operation,0)).run{
                removePrefix(operation)
                    .removePrefix(" ")
                    .removeSuffix(" ")
            }
        }
        else {
            finalResult=getFinalQuery(result,operation)
          //  println("finalResult$finalResult")
        }
        return finalResult
    }
    private fun findContact(matches: ArrayList<String>, contactList: HashMap<String, String>, requiredOperation: String):Observable<String?>{
      return Observable.fromIterable(matches)
            .map {
                val stringToSearch=it.toLowerCase()
                val contactName:String?=removeUnwantedPrefixFromResult(requiredOperation,stringToSearch)
                contactName
            }.filter {  contactList.containsKey(it) }
            .takeWhile{ contactList.containsKey(it) }
    }
}