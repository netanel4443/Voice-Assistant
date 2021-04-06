package com.e.VoiceAssistant.usecases

import android.app.SearchManager
import android.content.ComponentName
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.e.VoiceAssistant.di.annotations.ActivityScope
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ContactsData
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.PossibleMatches
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData
import com.e.VoiceAssistant.usecases.commons.RecognizerIntentInit
import com.e.VoiceAssistant.userscollecteddata.AppsDetailsSingleton
import com.e.VoiceAssistant.utils.printIfDebug
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.kaldi.Assets
import org.kaldi.Model
import org.kaldi.Vosk
import java.io.File
import java.io.IOException
import java.lang.Exception
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet

@ActivityScope
class TalkAndResultUseCases @Inject constructor(
    private val loadedData:AppsDetailsSingleton
) {
    private val requiredOperationsHset=
        hashSetOf("הודעה","text","וואטסאפ","whatsapp","השתק","mute","ring","צליל",
                  "youtube","יוטיוב","ספוטיפיי","spotify",
                  "call","התקשר","תקשר","תתקשר","search", "פתח","אופן","open","חפש")
    //todo fix send sms/whatsapp bug , sends sms  even if I canceled the second record
    private var model:Model?=null

    fun initRecognizerIntent():Observable<Intent> {
        return  RecognizerIntentInit().init()
    }

    fun returnRequiredOperationIntent(
        matches: ArrayList<String>): Observable<Pair<String,LinkedHashSet<String>>> {

        val splitedResultsLhset = LinkedHashSet<String>()
        var requiredOperation = ""

        return Single.fromCallable {
            matches.forEach {
                val st = StringTokenizer(it, " ")

                while (st.hasMoreTokens()) {
                    val nextToken = st.nextToken().toLowerCase(Locale.getDefault())
                    //todo change contains to substring algorithm
                    if (requiredOperation.isEmpty() && requiredOperationsHset.contains(nextToken)) {
//                       requiredOperationsHset.filter {it.contains(nextToken)  }
//                           .map { requiredOperation=it.substring(it.indexOf(nextToken),0) }
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
        return findContact(matches,contactList,requiredOperation,::sendSmsIntent)

    }

    private fun sendSmsIntent(contactNumber: String):Intent{
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$contactNumber")
        }
    }

    fun sendWhatsApp(matches:ArrayList<String>, requiredOperation: String, contactList: HashMap<String, String>):Observable<Pair<Intent,HashSet<ContactsData>>>{
        val result=findContact(matches,contactList,requiredOperation,::sendWhatsAppIntent)
        return result
    }

    private fun sendWhatsAppIntent(contactNumber: String):Intent{
        val contactWithCountryDigits=addCountryPhoneDigits(contactNumber)
        val uri =  Uri.parse(
            String.format("https://api.whatsapp.com/send?phone=%s", contactWithCountryDigits)
        )
        return Intent(Intent.ACTION_VIEW).apply { data= uri }
    }

    private fun sendWhatsAppIntent(contactNumber: String,message:String):Intent {
        val contactWithCountryDigits=addCountryPhoneDigits(contactNumber)
        val uri =  Uri.parse(
            String.format("https://api.whatsapp.com/send?phone=%s&text=%s", contactWithCountryDigits,message)
        )
        return Intent(Intent.ACTION_VIEW).apply { data= uri }
    }
    private fun addCountryPhoneDigits(contactNumber: String):String{
       val digits=loadedData.countryLocaleDigits

       return if (!contactNumber.startsWith(digits)) {
         val  contactWithCountryDigits=digits+contactNumber
              contactWithCountryDigits
       } else {
           contactNumber
       }
    }

    fun callTo(matches:ArrayList<String>, requiredOperation: String, contactList: HashMap<String, String>):Observable<Pair<Intent,HashSet<ContactsData>>>{
        return findContact(matches,contactList,requiredOperation,::callToIntent)
    }

    private fun callToIntent(contactNumber: String):Intent{
        return Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$contactNumber")
        }
    }

    fun searchInWeb(matches:ArrayList<String>, requiredOperation: String):Observable<Pair<Intent,HashSet<PossibleMatches>>>{
       return genericSearch(::searchInWebIntent,matches,requiredOperation)
    }

    private fun searchInWebIntent(query: String):Intent{
        return  Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
        }
    }

    fun searchInSpotify(matches: ArrayList<String>, requiredOperation: String):Observable<Pair<Intent,HashSet<PossibleMatches>>>{
       return genericSearch(::searchInSpotifyIntent,matches,requiredOperation)
    }

    private fun searchInSpotifyIntent(query: String):Intent{
        return loadedData.appsDetailsHmap["spotify"]?.run {
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

    fun searchInYoutube(matches: ArrayList<String>, requiredOperation: String):Observable<Pair<Intent,HashSet<PossibleMatches>>>{
          return genericSearch(::searchInYoutubeIntent,matches,requiredOperation)
    }

    private fun searchInYoutubeIntent(query:String):Intent{
        return Intent(Intent.ACTION_SEARCH).apply {
            setPackage("com.google.android.youtube")
            putExtra(SearchManager.QUERY, query)
        }
    }

    fun navigateTo(requiredOperation: String,matches: ArrayList<String>):Observable<Pair<Intent,HashSet<PossibleMatches>>> {
          return genericSearch(::navigateToIntent,matches,requiredOperation)
    }

    private fun genericSearch(intentFunc:(String)->Intent, matches: ArrayList<String>, requiredOperation: String):Observable<Pair<Intent,HashSet<PossibleMatches>>>{
        val possibleMatches=getPossibleMatchesWithout(matches,requiredOperation)
        val finalQuery=possibleMatches.first()
        val intent=intentFunc(finalQuery.match)
        return Observable.just(Pair(intent,possibleMatches))
    }

    private fun navigateToIntent(location:String):Intent{
        val url="https://waze.com/ul?q=$location"
        return  Intent(Intent.ACTION_VIEW).apply { data=Uri.parse(url) }
    }

    private fun getPossibleMatchesWithout(matches:ArrayList<String>,requiredOperation: String):HashSet<PossibleMatches>{
        val possibleMatches=HashSet<PossibleMatches>()
        matches.forEach { match->
            getFinalQuery(match.toLowerCase(),requiredOperation).let {

                if (it.isNotBlank()) {
                    possibleMatches.add(PossibleMatches(it))
                }
            }
        }
        return possibleMatches
    }

    /** this function deletes the keyword in order to get pure string the user wants to search.
     *  In addition it deletes all previous words before the desired keyword
     *  if [query] string doesn't contain [requiredOperation] then,
     * [return] an empty string and don't execute if's block,
     * because [indexOf] will return -1 and error will occur */
    private fun getFinalQuery(query: String,requiredOperation: String):String{

        return if (query.contains(requiredOperation))
        {
            query.substring(query.indexOf(requiredOperation,0))
                .run { replace(requiredOperation,"")
                    .removeSuffix(" ")
                    .removePrefix(" ")
                }
        }
        else { "" } // if "" is returned , we need to handle this case at the place which this func was called from.
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

    private fun findContact(matches: ArrayList<String>,
                            contactList: HashMap<String, String>,
                            requiredOperation: String,
                            intentFunc:(String)->Intent):Observable<Pair<Intent,HashSet<ContactsData>>>{
        val namesAndNumbers=HashSet<ContactsData>()

        return Observable.fromCallable {

            val tmpNameAndNumberHmap=HashSet<ContactsData>()

            matches.forEach {
                val stringToSearch=it.toLowerCase()
                val contactName=removeUnwantedPrefixFromResult(requiredOperation,stringToSearch)
                if (contactList.containsKey(contactName)){
                    tmpNameAndNumberHmap.add(ContactsData(contactName,contactList[contactName].toString()))
                }
                else if (contactName.isNotEmpty())
                {
                    contactList.keys.forEach {key->
                        if (key.contains(contactName)){
                            val number=contactList[key]
                            tmpNameAndNumberHmap.add(ContactsData(key,number.toString()))
                        }
                    }
                }
            }
            tmpNameAndNumberHmap
        }
            .filter { it.isNotEmpty() }
            .map { contacts->
                  namesAndNumbers.addAll(contacts)
                  contacts.first().number
            }
            .map { contactNumber->
                val intent=intentFunc(contactNumber)
                Pair(intent,namesAndNumbers)
            }
    }

    fun changeSelectedResult(requiredOperation:String,
                             resultData: ResultsData,
                             message: String ):Observable<Intent>{
        val intent= when (requiredOperation) {
            "התקשר", "תתקשר", "תקשר", "call" -> {
                callToIntent((resultData as ContactsData).number)
            }
            "spotify", "ספוטיפיי" -> {
                searchInSpotifyIntent((resultData as PossibleMatches).match)
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
            "חפש","search" -> {
                searchInWebIntent((resultData as PossibleMatches).match)
            }
            else->Intent()
        }
        return Observable.just(intent)
    }
    fun unmute(audioManager: AudioManager){
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),AudioManager.FLAG_SHOW_UI );
        audioManager.setStreamVolume(AudioManager.STREAM_RING,audioManager.getStreamMaxVolume(AudioManager.STREAM_RING),AudioManager.FLAG_SHOW_UI );
    }

    fun mute(audioManager: AudioManager) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,0,AudioManager.FLAG_SHOW_UI );
        audioManager.setStreamVolume(AudioManager.STREAM_RING,0,AudioManager.FLAG_SHOW_UI );
    }

    fun getKaldiModel(assetDir: File):Single<Model> {
       return Single.fromCallable {
              try {
                  printIfDebug("KaldiDemo", "Sync files in the folder $assetDir")
                  Vosk.SetLogLevel(0)
                  model=  Model("$assetDir/model-android")
              }catch (e:Exception){
                  printIfDebug("",e.message)
              }
           model
        }
    }

    fun loadKaldiLibrary():Observable<Unit> {
       return Observable.fromCallable {
            System.loadLibrary("kaldi_jni")
        }
    }
}