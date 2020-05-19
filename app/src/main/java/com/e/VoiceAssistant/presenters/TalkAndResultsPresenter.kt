package com.e.VoiceAssistant.presenters

import android.content.Intent
import android.net.Uri
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.di.annotations.ActivityScope
import com.e.VoiceAssistant.presenters.presentersStates.TalkAndResultsPresenterView
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.PossibleMatches
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData
import com.e.VoiceAssistant.usecases.OpenDesiredAppPresenterUseCase
import com.e.VoiceAssistant.usecases.TalkAndResultUseCases
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.VoiceAssistant.utils.rxJavaUtils.throttle
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@ActivityScope
class TalkAndResultsPresenter@Inject constructor(
    private val useCases:TalkAndResultUseCases){
    private val compositeDisposable=CompositeDisposable()
    private var talkBtnIcon=R.drawable.ic_mic_white_background_24
    private val clickSubject= PublishSubject.create<Int>()
    private var lastOperationIntent=Intent()
    private var operation=""
    private var message=""
    private var operationOfReplacedResult=""

    init {
        +clickSubject
            .observeOn(AndroidSchedulers.mainThread())
            .throttle()
            .doOnNext { view.handleClick(it) }
            .subscribe({},{})
    }

    lateinit var view:TalkAndResultsPresenterView

    fun bindView(view: TalkAndResultsPresenterView){ this.view=view }

    fun initRecognizerIntent(): Single<Intent> {
        return useCases.initRecognizerIntent()
    }

    fun handleTalkOrStopClick() {
       clickSubject.onNext(talkBtnIcon)
    }

    fun changeTalkBtnIcon(){
       talkBtnIcon = if (talkBtnIcon==R.drawable.ic_mic_white_background_24){
           R.drawable.ic_pause_white_background_24
       }
       else{
           R.drawable.ic_mic_white_background_24
       }
       view.changeTalkIcon(talkBtnIcon)
    }

    fun checkIfTalkBtnIconChanged(){
        if (talkBtnIcon==R.drawable.ic_pause_white_background_24){
            clickSubject.onNext(talkBtnIcon)
        }
    }

    fun checkIfSecondListenRequired(matches: ArrayList<String>,
                                appComponent: HashMap<String, AppsDetails>,
                                contactList: HashMap<String,String> ){
        //todo add cancel option to sms and whatsapp
        if (operation=="הודעה"||operation=="text") {
            operation=""
            message=matches[0]
            lastOperationIntent.putExtra(Intent.EXTRA_TEXT, message)
            view.navigateToDesiredApp(lastOperationIntent)
        }
        else if ( operation=="וואטסאפ"){
            operation=""
            message=matches[0]
            val uri =  Uri.parse(
                String.format(lastOperationIntent.data.toString()+"&text=%s" ,message)
            )
            lastOperationIntent.data=uri
            view.navigateToDesiredApp(lastOperationIntent)
        }
        else {
            returnRequiredOperationIntent(matches,appComponent,contactList)
        }
    }

    fun returnRequiredOperationIntent(
        matches: ArrayList<String>,
        appComponent: HashMap<String, AppsDetails>,
        contactList: HashMap<String,String> ){

        +useCases.returnRequiredOperationIntent(matches)
            .subscribe( {
                operationOfReplacedResult=it.first
                operation=it.first
                checkRequestedOperation(it.first,appComponent,contactList,matches,it.second)
            },{it.printStackTrace()})
    }

    private fun requiredOperationIsWhatsApp(matches: ArrayList<String>, requiredOpration: String, contactList: HashMap<String, String>){
        +useCases.sendWhatsApp(matches,requiredOpration,contactList)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({pair->checkIntentType(pair.first,pair.second as HashSet<ResultsData>)},{})
    }

    private fun requiredOperationIsSendSms(matches: ArrayList<String>, requiredOpration: String, contactList: HashMap<String, String>){
        +useCases.sendSms(matches,requiredOpration,contactList)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({ pair->checkIntentType(pair.first,pair.second as HashSet<ResultsData>) }, {})
    }

    private fun requiredOperationIsCallTo(matches: ArrayList<String>, requiredOpration: String, contactList: HashMap<String, String>){
        +useCases.callTo(matches,requiredOpration,contactList)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({pair->view.navigateToDesiredApp(pair.first,pair.second as HashSet<ResultsData>,0)},{it.printStackTrace()})
    }

    private fun requiredOperationIsSearchInYoutube(matches: ArrayList<String>,requiredOpration: String){
        +useCases.searchInYoutube(matches,requiredOpration)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({ view.navigateToDesiredApp(it.first,it.second as HashSet<ResultsData>,1)},{})
    }
    private fun requiredOperationIsSearchInSpotify(appComponent: HashMap<String, AppsDetails>, matches: ArrayList<String>, requiredOpration: String){
        +useCases.searchInSpotify(appComponent, matches,requiredOpration)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({ view.navigateToDesiredApp(it.first,it.second as HashSet<ResultsData>,1)},{})
    }
    private fun requiredOperationIsSearchInWeb(matches: ArrayList<String>, requiredOpration: String){
        val hashSet= hashSetOf<PossibleMatches>() as HashSet<ResultsData>
            matches.forEach {
                val key=PossibleMatches(it)
                hashSet.add(key)
            }
        +useCases.searchInWeb(matches,requiredOpration)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({ view.navigateToDesiredApp(it.first,it.second as HashSet<ResultsData>,1)},{})
    }
    private fun requiredOperationIsNavigate(requiredOpration: String,matches: ArrayList<String>){
        +useCases.navigateTo(requiredOpration, matches[0])
            .subscribeOnIoAndObserveOnMain()
            .subscribe({intent->view.navigateToDesiredApp(intent,HashSet(),1)},{})
    }

    private fun requiredOperationIsOpenAnApp(appComponent: HashMap<String, AppsDetails>, splitedResultsLset:LinkedHashSet<String>){
        +OpenDesiredAppPresenterUseCase().getDesiredIntent(appComponent,splitedResultsLset)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({ intent->view.navigateToDesiredApp(intent,HashSet(),1) },{})
    }

    private fun checkRequestedOperation(
                    requiredOperation: String,appComponent: HashMap<String, AppsDetails>,
                    contactList: HashMap<String, String>,matches: ArrayList<String>,
                    splitedResultsLset: LinkedHashSet<String>){

        when (requiredOperation) {
            "open", "אופן", "פתח" -> {
                requiredOperationIsOpenAnApp(appComponent,splitedResultsLset)
            }
            "התקשר", "תתקשר", "תקשר", "call" -> {
                requiredOperationIsCallTo(matches,requiredOperation,contactList)
            }
            "spotify", "ספוטיפיי" -> {
                requiredOperationIsSearchInSpotify(appComponent,matches,requiredOperation)
            }
            "youtube", "יוטיוב" -> {
                requiredOperationIsSearchInYoutube(matches,requiredOperation)
            }
            "navigate", "נווט" -> {
                requiredOperationIsNavigate(requiredOperation, matches)
            }
            "text", "הודעה" -> {
                requiredOperationIsSendSms(matches,requiredOperation,contactList)
            }
            "וואטסאפ", "whatsapp" -> {
                requiredOperationIsWhatsApp(matches,requiredOperation,contactList)
            }
            else -> {
                requiredOperationIsSearchInWeb(matches,requiredOperation)
            }
        }
    }
    fun changeSelectedResult(resultsData: ResultsData,
                             appComponent: HashMap<String, AppsDetails>){
        +useCases.changeSelectedResult(operationOfReplacedResult,resultsData,appComponent,message)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({intent->view.navigateToDesiredApp(intent)},{it.printStackTrace() })
    }

    private fun checkIntentType( intent: Intent,contacts:HashSet<ResultsData>){
              lastOperationIntent=intent
              view.secondListenToUser(contacts,0,intent)
    }

    private operator fun Disposable.unaryPlus(){
        compositeDisposable.add(this)
    }

    fun dispose(){
        compositeDisposable.clear()
    }
}