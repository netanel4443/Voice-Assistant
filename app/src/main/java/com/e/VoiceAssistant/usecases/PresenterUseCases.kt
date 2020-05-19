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
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ContactsData
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.PossibleMatches
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData
import com.e.VoiceAssistant.usecases.commons.RecognizerIntentInit
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet

//@ServiceScope
//todo add scope
class PresenterUseCases @Inject constructor() {
    private val requiredOperationsHset=
        hashSetOf("הודעה","text","וואטסאפ","whatsapp",
            "נווט","navigate","youtube","יוטיוב","ספוטיפיי","spotify",
            "call","התקשר","תקשר","תתקשר","search", "פתח","אופן","open","חפש")
//todo fix send sms/whatsapp bug , sends sms  even if I canceled the second record
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
        matches: ArrayList<String>): Observable<Pair<String,LinkedHashSet<String>>> {

        val splitedResultsLhset = LinkedHashSet<String>()
        var requiredOperation = ""

        return Single.fromCallable {
            matches.forEach {
            val st = StringTokenizer(it, " ")

            while (st.hasMoreTokens()) {//todo check the locality if works properly on en-US
                val nextToken = st.nextToken().toLowerCase(Locale.getDefault())

                if (requiredOperation.isEmpty() && requiredOperationsHset.contains(nextToken)) {
                    requiredOperation = nextToken
                }
                    splitedResultsLhset.add(nextToken)
                }
            }
            Pair(requiredOperation,splitedResultsLhset)
        }
         .filter{requiredOperation.isNotEmpty()}.toObservable()
    }

    fun sendSms(matches:ArrayList<String>, requiredOperation: String, contactList: HashMap<String, String>):Observable<Pair<Intent,HashSet<ContactsData>>>{
        val namesAndNumbers=HashSet<ContactsData>()
        return findContact(matches,contactList,requiredOperation)
           .map { contacts->
               namesAndNumbers.addAll(contacts)
               contacts.first().number
           }
           .map {contactNumber->
                val intent=sendSmsIntent(contactNumber)
                 Pair(intent,namesAndNumbers)
           }
    }

    fun sendSmsIntent(contactNumber: String):Intent{
      return Intent(Intent.ACTION_SENDTO).apply {
          data = Uri.parse("smsto:$contactNumber")
      }
    }

    fun sendWhatsApp(matches:ArrayList<String>, requiredOperation: String, contactList: HashMap<String, String>):Observable<Pair<Intent,HashSet<ContactsData>>>{
        val namesAndNumbers=HashSet<ContactsData>()
        println("whatsapp 1")
        return findContact(matches,contactList,requiredOperation)
            .map { contacts->
                namesAndNumbers.addAll(contacts)
                contacts.first().number
            }
            .map { contactNumber->
                val intent=sendWhatsAppIntent(contactNumber)
                Pair(intent,namesAndNumbers)
            }
    }

    private fun sendWhatsAppIntent(contactNumber: String):Intent{
        println("whatsapp 2")
        val uri =  Uri.parse(
            String.format("https://api.whatsapp.com/send?phone=%s", contactNumber)
        )
        return Intent(Intent.ACTION_VIEW).apply { data= uri }
    }

    private fun sendWhatsAppIntent(contactNumber: String,message:String):Intent {
        val uri =  Uri.parse(
            String.format("https://api.whatsapp.com/send?phone=%s&text=%s", contactNumber,message)
        )
        return Intent(Intent.ACTION_VIEW).apply { data= uri }
    }



    fun callTo(matches:ArrayList<String>, requiredOperation: String, contactList: HashMap<String, String>):Observable<Pair<Intent,HashSet<ContactsData>>>{
         val namesAndNumbers=HashSet<ContactsData>()

     return findContact(matches,contactList,requiredOperation)
             .map { contacts->
                 println("here at first map?")
                     namesAndNumbers.addAll(contacts)
                     contacts.first().number
             }
             .map {contactNumber->
                 val intent=callToIntent(contactNumber)
                 Pair(intent,namesAndNumbers)
             }
     }

     fun callToIntent(contactNumber: String):Intent{
         return Intent(Intent.ACTION_DIAL).apply {
             data = Uri.parse("tel:$contactNumber")
         }
     }

     fun searchInWeb(query: String,requiredOperation: String):Observable<Intent>{
        val finalQuery=getFinalQuery(query,requiredOperation)
        return Observable.just(searchInWebIntent(finalQuery))
    }

    fun searchInWebIntent(query: String):Intent{
        return  Intent(Intent.ACTION_WEB_SEARCH).apply {
                 putExtra(SearchManager.QUERY, query)
        }
    }

    fun searchInSpotify( appComponent: HashMap<String, AppsDetails>,query: String,requiredOperation: String):Observable<Intent>{
       val finalQuery=getFinalQuery(query,requiredOperation)
       return Observable.just(searchInSpotifyIntent(finalQuery,appComponent))
    }

    fun searchInSpotifyIntent(query: String,appComponent: HashMap<String, AppsDetails>):Intent{
        return appComponent["spotify"]?.run {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.action = MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH
            intent.component = ComponentName(activity, pckg)
            intent.putExtra(SearchManager.QUERY, query)
            intent
        } ?:
        run{
            val url = "https://open.spotify.com/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            intent
        }
    }

     fun searchInYoutube(query:String,requiredOperation: String):Observable<Intent>{
        val finalQuery= getFinalQuery(query,requiredOperation)
        return Observable.just(searchInYoutubeIntent(finalQuery))
     }

     fun searchInYoutubeIntent(query:String):Intent{
         return Intent(Intent.ACTION_SEARCH).apply {
             setPackage("com.google.android.youtube")
             putExtra(SearchManager.QUERY, query)
             flags = Intent.FLAG_ACTIVITY_NEW_TASK
         }
     }

     fun navigateTo(requiredOperation: String,location:String):Observable<Intent> {
         val finalLocation=removeUnwantedPrefixFromResult(requiredOperation,location)
         return Observable.just(navigateToIntent(finalLocation))
     }

     fun navigateToIntent(location:String):Intent{
        val url="https://waze.com/ul?q=$location"
        return  Intent(Intent.ACTION_VIEW).apply { data=Uri.parse(url) }
     }

    /** this function deletes the keyword in order to get pure string the use wants to search.
     * In addition it deletes all previous words before desired keyword
     * @param query if query string doesn't contain
     * @param requiredOperation then,
     * @return empty string
     * and don't execute if's block because index of will return -1 and error will occur */
    private fun getFinalQuery(query: String,requiredOperation: String):String{

          return if (query.contains(requiredOperation))
                   {
                      query.substring(query.indexOf(requiredOperation,0))
                          .run { replace(requiredOperation,"")
                              .removeSuffix(" ")
                              .removePrefix(" ")
                          }
                   }
                 else { "" }
    }

    private fun removeUnwantedPrefixFromResult(requiredOperation: String, result:String):String{
        var operation=requiredOperation
        var finalResult=""
        if (Locale.getDefault().displayCountry=="ישראל")
        {
            operation=requiredOperation+" "+"ל"
            /**if results doesn't contain operation, error will occur because indexOf returns -1 in such cases*/
            if (result.contains(operation))
            {
                finalResult=result.substring(result.indexOf(operation,0)).run{
                    removePrefix(operation)
                        .removePrefix(" ")
                        .removeSuffix(" ")
                }
            }
        }
        else {
            finalResult=getFinalQuery(result,operation)
        }
        return finalResult
    }
    private fun findContact(matches: ArrayList<String>, contactList: HashMap<String, String>, requiredOperation: String):Observable<HashSet<ContactsData>>{

        return Observable.fromCallable {

            val tmpNameAndNumberHmap=HashSet<ContactsData>()
            matches.forEach {
                val stringToSearch=it.toLowerCase(Locale.getDefault())
                val contactName=removeUnwantedPrefixFromResult(requiredOperation,stringToSearch)
                if (contactList.containsKey(contactName)){
                    tmpNameAndNumberHmap.add(ContactsData(contactName,contactList[contactName].toString()))
                    //tmpNameAndNumberHmap[contactName]=contactList[contactName].toString()
                }
                else if (contactName.isNotEmpty())
                {
                    contactList.keys.forEach {key->
                        if (key.contains(contactName)){
                            val number=contactList[key]
                            println("possible $key $number")
                          //  tmpNameAndNumberHmap[key]=number.toString()
                            tmpNameAndNumberHmap.add(ContactsData(key,number.toString()))
                        }
                    }
                }
            }
            println("size $tmpNameAndNumberHmap")
            tmpNameAndNumberHmap
        }
            .filter { it.isNotEmpty() }
    }

    fun changeSelectedResult(requiredOperation:String,
                             resultData: ResultsData,appComponent: HashMap<String, AppsDetails>,
                             message: String ):Observable<Intent>{
       val intent= when (requiredOperation) {
            "התקשר", "תתקשר", "תקשר", "call" -> {
              callToIntent((resultData as ContactsData).number)
            }
            "spotify", "ספוטיפיי" -> {
               searchInSpotifyIntent((resultData as PossibleMatches).match,appComponent)
            }
            "youtube", "יוטיוב" -> {
                searchInYoutubeIntent((resultData as PossibleMatches).match)
            }
            "navigate", "נווט" -> {
                navigateToIntent((resultData as PossibleMatches).match)
            }
            "text", "הודעה" -> {
                sendSmsIntent((resultData as ContactsData).number)
                    .putExtra(Intent.EXTRA_TEXT,message)
            }
            "וואטסאפ", "whatsapp" -> {
                sendWhatsAppIntent((resultData as ContactsData).number,message)
            }
            else -> {
                searchInWebIntent((resultData as ContactsData).number)
            }
        }
        return Observable.just(intent)
    }
}