package com.e.VoiceAssistant.viewmodels

import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.di.annotations.ActivityScope
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData
import com.e.VoiceAssistant.usecases.OpenDesiredAppPresenterUseCase
import com.e.VoiceAssistant.usecases.TalkAndResultUseCases
import com.e.VoiceAssistant.utils.livedata.SingleLiveEvent
import com.e.VoiceAssistant.utils.printIfDebug
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.VoiceAssistant.viewmodels.commands.TalkAndResultCommands
import com.e.VoiceAssistant.viewmodels.effects.TalkAndResultEffects
import com.e.VoiceAssistant.viewmodels.states.TalkAndResultState
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.kaldi.Model
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@ActivityScope
class TalkAndResultViewModel @Inject constructor(
    private val talkAndResultUseCases: TalkAndResultUseCases,
    private val openDesiredAppPresenterUseCase: OpenDesiredAppPresenterUseCase
):BaseViewModel() {
    private val TAG=this::class.simpleName
    private var talkBtnIcon= R.drawable.ic_mic_white_background_24
    private var lastOperationIntent= Intent()
    private var operation=""
    private var message=""
    private var operationOfReplacedResult=""

    private val _command=PublishSubject.create<TalkAndResultCommands>()
    val command get() = _command

    private val _viewEffect=SingleLiveEvent<TalkAndResultEffects>()
    val viewEffect:LiveData<TalkAndResultEffects> get()=_viewEffect

    private var state=TalkAndResultState()

    private val _viewState= MutableLiveData<Pair<TalkAndResultState,TalkAndResultState>>(Pair(state,state))
    val viewState:LiveData<Pair<TalkAndResultState,TalkAndResultState>> get() = _viewState

    private fun updateStatesField(newState:TalkAndResultState){
        state=newState
        _viewState.postValue(Pair(_viewState.value!!.copy().second,newState))
    }

    fun globalInits(){
        val first= talkAndResultUseCases.loadKaldiLibrary()

        val second = talkAndResultUseCases.initRecognizerIntent()
            .doOnNext{ intent->
                printIfDebug (TAG,"are you here at intent?")
                _command.onNext(TalkAndResultCommands.SetIntent(intent))
            }

        val observableList = arrayListOf(second, first)

        +Observable.combineLatest(observableList) {}
            .subscribeOnIoAndObserveOnMain()
            .subscribe({
                /*according to docs, this should be initialized on mainThread after inits are completed*/
                _command.onNext(TalkAndResultCommands.InitSpeechRecognizer)
                //should happen after we load kaldi_jni library (we initialize it at activity's usecases class)
                _command.onNext(TalkAndResultCommands.StartKaldiRecognizer)
            }, {})
    }

    fun handleTalkOrStopClick() {
        _viewEffect.value=TalkAndResultEffects.HandleClick(talkBtnIcon)
    }

    fun changeTalkBtnIcon(){
        talkBtnIcon = if (talkBtnIcon==R.drawable.ic_mic_white_background_24){
            R.drawable.ic_pause_white_background_24
        }
        else{
            R.drawable.ic_mic_white_background_24
        }
        updateStatesField(state.copy(talkBtnIcon=talkBtnIcon))
    }

    fun checkIfTalkBtnIconChanged(){
        if (talkBtnIcon==R.drawable.ic_pause_white_background_24){
            _viewEffect.value=TalkAndResultEffects.HandleClick(talkBtnIcon)
        }
    }

    fun checkIfSecondListenRequired(matches: ArrayList<String>,
                                    appComponent: HashMap<String, AppsDetails>,
                                    contactList: HashMap<String,String> ){
        /*check if intent.action is null , if it is , skip second listen to user because, no contact number was found
        in user's contact list and we don't want to record a message to no one, it won't work.*/
        if ((operation=="הודעה"||operation=="text")&&lastOperationIntent.action!=null) {
            handleSecondListen(matches[0],::handleSecondListenForSms)
        }
        else if (( operation=="וואטסאפ" || operation=="whatsapp")&&lastOperationIntent.action!=null){
            handleSecondListen(matches[0],::handleSecondListenForWhatsApp)
        }
        else {
            returnRequiredOperationIntent(matches,appComponent,contactList)
        }
    }

    private fun handleSecondListen(match:String, handleFunc:()->Unit){
        val tmpIntent=lastOperationIntent
        tmpIntent.flags=Intent.FLAG_ACTIVITY_NEW_TASK //required new task or the app we want to open will replace our activity
        message=match //keep the message if the user wants to change contact
        handleFunc()
        _viewEffect.value=TalkAndResultEffects.NavigateToDesiredApp(tmpIntent)
        lastOperationIntent=Intent() //reset !
        operation="" //reset !
    }

    private fun handleSecondListenForWhatsApp(){
        val uri =  Uri.parse(
            String.format(lastOperationIntent.data.toString()+"&text=%s" ,message)
        )
        lastOperationIntent.data=uri
    }

    private fun handleSecondListenForSms(){
        lastOperationIntent.putExtra(Intent.EXTRA_TEXT, message)
    }

    private fun returnRequiredOperationIntent(
        matches: ArrayList<String>,
        appComponent: HashMap<String, AppsDetails>,
        contactList: HashMap<String,String> ){

        +talkAndResultUseCases.returnRequiredOperationIntent(matches)
            .subscribe( {
                //   println("operation ${it.first}")
                operationOfReplacedResult=it.first //keep if second listen is needed
                operation=it.first //keep if second listen is needed
                checkRequestedOperation(it.first,appComponent,contactList,matches,it.second)
            },{ printIfDebug(TAG,it.message) })
    }

    private fun requiredOperationIsWhatsApp(matches: ArrayList<String>, requiredOpration: String, contactList: HashMap<String, String>){
        +talkAndResultUseCases.sendWhatsApp(matches,requiredOpration,contactList)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({pair->checkIntentType(pair.first,pair.second as HashSet<ResultsData>)},{
                printIfDebug(TAG,it.message)
            })
    }

    private fun requiredOperationIsSendSms(matches: ArrayList<String>, requiredOpration: String, contactList: HashMap<String, String>){

        +talkAndResultUseCases.sendSms(matches,requiredOpration,contactList)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({ pair->
                checkIntentType(pair.first,pair.second as HashSet<ResultsData>) }
                , { printIfDebug(TAG,it.message) })
    }

    private fun requiredOperationIsCallTo(matches: ArrayList<String>, requiredOpration: String, contactList: HashMap<String, String>){
        +talkAndResultUseCases.callTo(matches,requiredOpration,contactList)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({ pair->
                updateStatesField(
                    state.copy(speechResults =
                            TalkAndResultState.SpeechResults(pair.second as HashSet<ResultsData>,0)
                    )
                )
                _viewEffect.value=TalkAndResultEffects.NavigateToDesiredAppWithResults( pair.first,pair.second as HashSet<ResultsData>,0)
            }
                ,{ printIfDebug(TAG,it.message) })
    }

    private fun requiredOperationIsSearchInYoutube(matches: ArrayList<String>,requiredOpration: String){
        +talkAndResultUseCases.searchInYoutube(matches,requiredOpration)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({ updateStatesField(state.copy(speechResults =
                            TalkAndResultState.SpeechResults(it.second as HashSet<ResultsData>,1)))},
                {
                printIfDebug(TAG,it.message)
            })
    }

    private fun requiredOperationIsSearchInSpotify( matches: ArrayList<String>, requiredOpration: String){
        +talkAndResultUseCases.searchInSpotify(matches,requiredOpration)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({  updateStatesField(state.copy(speechResults =
            TalkAndResultState.SpeechResults(it.second as HashSet<ResultsData>,1)))},{ printIfDebug(TAG,it.message) })
    }

    private fun requiredOperationIsSearchInWeb(matches: ArrayList<String>, requiredOpration: String){
        +talkAndResultUseCases.searchInWeb(matches,requiredOpration)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({  updateStatesField(state.copy(speechResults =
            TalkAndResultState.SpeechResults(it.second as HashSet<ResultsData>,1)))},{ printIfDebug(TAG,it.message) })
    }
    private fun requiredOperationIsNavigate(requiredOpration: String,matches: ArrayList<String>){
        +talkAndResultUseCases.navigateTo(requiredOpration, matches)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({_viewEffect.value=TalkAndResultEffects.NavigateToDesiredAppWithResults(it.first,it.second as HashSet<ResultsData>,1)},{ printIfDebug(TAG,it.message) })
    }

    private fun requiredOperationIsOpenAnApp(appComponent: HashMap<String, AppsDetails>, splitedResultsLset:LinkedHashSet<String>){
        +openDesiredAppPresenterUseCase.getDesiredIntent(appComponent,splitedResultsLset)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({ intent-> _viewEffect.value=TalkAndResultEffects.NavigateToDesiredAppWithResults(intent,HashSet(),1) },{ printIfDebug(TAG,it.message) })
    }
    fun requiredOperationIsMuteOrUnmute(audioManager: AudioManager){
        when (operation) {
            "mute", "השתק" -> { talkAndResultUseCases.mute(audioManager)   }
            "ring","צליל" -> { talkAndResultUseCases.unmute(audioManager) }
        }
    }

    private fun checkRequestedOperation(
        requiredOperation: String, appComponent: HashMap<String, AppsDetails>,
        contactList: HashMap<String, String>, matches: ArrayList<String>,
        splitedResultsLset: LinkedHashSet<String>){

        when (requiredOperation) {
            "open", "אופן", "פתח" -> {
                requiredOperationIsOpenAnApp(appComponent,splitedResultsLset)
            }
            "התקשר", "תתקשר", "תקשר", "call" -> {
                requiredOperationIsCallTo(matches,requiredOperation,contactList)
            }
            "spotify", "ספוטיפיי" -> {
                requiredOperationIsSearchInSpotify(matches,requiredOperation)
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
            "השתק","mute","צליל","ring"->{
                _command.onNext(TalkAndResultCommands.MuteOrUnMute)
            }
            else -> {
                requiredOperationIsSearchInWeb(matches,requiredOperation)
            }
        }
    }

    fun changeSelectedResult(resultsData: ResultsData){
        +talkAndResultUseCases.changeSelectedResult(operationOfReplacedResult,resultsData,message)
            .subscribeOnIoAndObserveOnMain()
            .subscribe({intent->_viewEffect.value=TalkAndResultEffects.NavigateToDesiredApp(intent)},{
                printIfDebug(TAG,it.message)
            })
    }

    private fun checkIntentType( intent: Intent,contacts:HashSet<ResultsData>){

        lastOperationIntent=intent
//        updateStatesField(states.copy(speechResultsWithIntent =
//              TalkAndResultState.SpeechResultsWithIntent(contacts,0,intent)))

        updateStatesField(state.copy(speechResults = TalkAndResultState.SpeechResults( contacts,0)))
        _viewEffect.value=TalkAndResultEffects.StartTimerAnimation
    }

    fun countDownTimerAnimation(): Observable<Int> {
        var counter=3
        return Observable.fromCallable{counter}
            .subscribeOn(AndroidSchedulers.mainThread())
            .doOnNext { count-> updateStatesField(state.copy(timerValue = count))}
            .delay(1, TimeUnit.SECONDS, Schedulers.io())
            .map {
                counter -= 1
                counter
            }.repeat(3)
            .observeOn(AndroidSchedulers.mainThread())
            .filter {count-> count==0 }
    }

    fun getKaldiModel(assetsDir: File): Single<Model> {
        return talkAndResultUseCases.getKaldiModel(assetsDir)
    }
}