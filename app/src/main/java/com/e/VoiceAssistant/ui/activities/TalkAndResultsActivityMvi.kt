package com.e.VoiceAssistant.ui.activities

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.VisibleForTesting
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.permissions.CheckOnlyPerrmission
import com.e.VoiceAssistant.permissions.RequestCodes
import com.e.VoiceAssistant.permissions.RequestGlobalPermission
import com.e.VoiceAssistant.permissions.StartActivityToCheckPermission
import com.e.VoiceAssistant.sensors.CommandsForSpeechListenerService
import com.e.VoiceAssistant.sensors.HandleSpeechRecognition
import com.e.VoiceAssistant.ui.dialogs.FloatingRepresentOperationsDialog
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData
import com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters.OperationsKeyWordsAdapter
import com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters.PossibleResultsRecyclerViewAdapter
import com.e.VoiceAssistant.ui.uihelpers.KaldiRecognizer
import com.e.VoiceAssistant.userscollecteddata.AppsDetailsSingleton
import com.e.VoiceAssistant.utils.livedata.toObservable
import com.e.VoiceAssistant.utils.printIfDebug
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.VoiceAssistant.utils.rxJavaUtils.throttle
import com.e.VoiceAssistant.utils.toast
import com.e.VoiceAssistant.utils.toastLong
import com.e.VoiceAssistant.viewmodels.TalkAndResultViewModel
import com.e.VoiceAssistant.viewmodels.commands.TalkAndResultCommands
import com.e.VoiceAssistant.viewmodels.effects.TalkAndResultEffects
import com.e.VoiceAssistant.viewmodels.states.TalkAndResultState
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding3.view.clicks
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_talk_and_results.*
import org.kaldi.Assets
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.HashMap

class TalkAndResultsActivityMvi : BaseAdsActivity() {

    @Inject lateinit var appsDetailsSingleton: AppsDetailsSingleton

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    val viewModel: TalkAndResultViewModel by lazy(this::getViewModel)

    private lateinit var talkIntent: SpeechRecognizer
    private lateinit var intnt: Intent
    private var appsDetailsHmap = HashMap<String, AppsDetails>()
    private val TAG = "TalkAndResultsActivityMvi"
    private lateinit var adapter: PossibleResultsRecyclerViewAdapter
    private lateinit var kaldiRecognizer: KaldiRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talk_and_results)

        attachStateObservable()
        attachCommandObservable()
        attachEffectObserver()
        uiInits()
        generalNonUIinits()

        +clickToTalkOrStopBtn.clicks().throttle()
            .map {
                RequestGlobalPermission.check(
                    this, Manifest.permission.RECORD_AUDIO, RequestCodes.RECORD_AUDIO
                )
            }
            .filter { granted -> granted }
            .subscribe { viewModel.handleTalkOrStopClick() }

        +settings.clicks().throttle().subscribe {
            val intent = Intent(this, AddCustomAppNameActivity::class.java)
            startActivity(intent)
        }

        +floatingHelp.clicks().throttle().subscribe {
            FloatingRepresentOperationsDialog(this).show()
        }
    }

    private fun attachStateObservable() {
        +viewModel.viewState.toObservable(this)
//            .scan { prev, now -> renderUi(prev, now)  }
            .subscribe({ renderUi(it.first,it.second) }){}
    }

    private fun attachCommandObservable() {
        +viewModel.command.subscribeOnIoAndObserveOnMain()
            .subscribe { command ->
                when (command) {
                    is TalkAndResultCommands.SetIntent -> attachIntent(command.intent)
                    is TalkAndResultCommands.InitSpeechRecognizer -> initSpeechRecognizer()
                    is TalkAndResultCommands.StartKaldiRecognizer -> startKaldiRecognizer()
                    is TalkAndResultCommands.MuteOrUnMute -> muteOrUnmute()
                }
            }
    }

    private fun attachEffectObserver() {
        viewModel.viewEffect.observe(this, Observer { effect ->
            when (effect) {
                is TalkAndResultEffects.NavigateToDesiredApp -> navigateToDesiredApp(effect.intent)
                is TalkAndResultEffects.NavigateToDesiredAppWithResults ->
                    navigateToDesiredApp(effect.intent, effect.results, effect.dataType)
                is TalkAndResultEffects.HandleClick-> handleClick(effect.icon)
                is TalkAndResultEffects.StartTimerAnimation-> startTimer()
            }
        })
    }

    private fun renderUi(prev: TalkAndResultState, now: TalkAndResultState): TalkAndResultState {
        println("prev $prev \n now $now")
        if (prev.timerValue != now.timerValue) {
            updateTimerValueAnimation(now.timerValue)
        }
        showResults(now.speechResults.results, now.speechResults.dataType)

        changeTalkIcon(now.talkBtnIcon)

        return now
    }

    private fun uiInits() {
        appsDetailsHmap = appsDetailsSingleton.appsAndStoredAppsDetails
        initRecyclerView()
        initOperationsRecyclerView()
        loadAd(adContainer, fakeUnitId)
    }

    private fun initOperationsRecyclerView() {
        val operationAdapter = OperationsKeyWordsAdapter()
        operationsWordsRecyclerView.adapter = operationAdapter
        operationsWordsRecyclerView.layoutManager =
            LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        operationsWordsRecyclerView.setHasFixedSize(true)
        operationAdapter.attachData(
            resources.getStringArray(R.array.operations_keywords).toHashSet()
        )
    }

    private fun initRecyclerView() {
        adapter = PossibleResultsRecyclerViewAdapter()
        adapter.itemClick = { viewModel.changeSelectedResult(it) }
        possibleResultsRecyclerView.adapter = adapter
        possibleResultsRecyclerView.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        possibleResultsRecyclerView.setHasFixedSize(true)
    }

    private fun generalNonUIinits() {
        initKaldiRecognizer()
        viewModel.globalInits()
    }

    private fun initKaldiRecognizer() {
        kaldiRecognizer = KaldiRecognizer() { kaldiAction ->
            when (kaldiAction) {
                KaldiRecognizer.KaldiActions.ERROR -> showSnackBar()
                KaldiRecognizer.KaldiActions.DETECTED -> viewModel.handleTalkOrStopClick()
            }
        }
    }

    private fun startKaldiRecognizer() {
        val permissionCheck =
            ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RequestCodes.RECORD_AUDIO
            )
        } else {
            setupTask()
        }
    }

    private fun attachIntent(intent: Intent) {
        intnt = intent
    }

    private fun initSpeechRecognizer() {
        talkIntent = SpeechRecognizer.createSpeechRecognizer(this)
        talkIntent.setRecognitionListener(
            HandleSpeechRecognition(object : CommandsForSpeechListenerService {
                override fun onError(message: String) {
                    toast(message)
                }

                override fun changeTalkIcon() {
                   viewModel.checkIfTalkBtnIconChanged()
                }

                override fun checkForResults(matches: ArrayList<String>) {
                   viewModel.checkIfSecondListenRequired(
                        matches,
                        appsDetailsHmap,
                        appsDetailsSingleton.contactList
                   )
                }
            })
        )
    }

    private fun handleClick(icon: Int) {
        if (StartActivityToCheckPermission.check(this, Manifest.permission.RECORD_AUDIO)) {

            if (R.drawable.ic_mic_white_background_24 == icon) {
                kaldiRecognizer.stopRecognizer()
                talkIntent.startListening(intnt)
                viewModel.changeTalkBtnIcon()
            } else {
                talkIntent.stopListening()
                /*timer added to cancel because the "speech recognizer" api sometimes stuck at "Busy state" and we can't
                *perform a new operation. 800 millis because we can click every 1 sec so the range needs to be 0-1s
                * 800 millis delay works fine.*/
                +Observable.timer(800, TimeUnit.MILLISECONDS)
                    .subscribeOnIoAndObserveOnMain()
                    .subscribe {
                        talkIntent.cancel()
                        kaldiRecognizer.startListening()
                        kaldiRecognizer.once = true
                    }
                viewModel.changeTalkBtnIcon()
            }
        }
    }

    private fun changeTalkIcon(icon: Int) {
        clickToTalkOrStopBtn.setBackgroundResource(icon)
    }

    private fun navigateToDesiredApp(intent: Intent, results: HashSet<ResultsData>, dataType: Int) {
        printIfDebug(TAG, intent.data.toString())
        if (checkIfPermissionRequired(intent)) {
            try {

                startActivity(intent)
//                adapter.attachData(results, dataType, intent)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            }
        } else {
            toastLong(R.string.permission_required)
        }
    }

    private fun navigateToDesiredApp(intent: Intent) {
        if (checkIfPermissionRequired(intent)) {
            try {
                startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            }
        } else {
            toastLong(R.string.permission_required)
        }
    }

    private fun showResults(results: HashSet<ResultsData>, dataType: Int) {
        adapter.attachData(results, dataType)
    }

    private fun showResultsWithIntent(results: HashSet<ResultsData>, dataType: Int, intent: Intent){
        adapter.attachData(results, dataType, intent)
    }

    private fun secondListenToUser(contacts: HashSet<ResultsData>, dataType: Int, intent: Intent) {
        /** timer is needed because of throttle click(rxBinding) .
        After a user clicks on the [talk] button when he wants to pause the speech recognition*/
        adapter.attachData(contacts, 0, intent)


    }

    private fun startTimer(){
        +viewModel.countDownTimerAnimation()
            .subscribe({
                counterTalkAndResultParent.visibility = View.GONE
                viewModel.handleTalkOrStopClick()
            }, {})
    }

    private fun updateTimerValueAnimation(counter: Int) {
        counterTalkAndResultParent.visibility = View.VISIBLE
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        counterTalkAndResult.startAnimation(fadeOut)
        counterTalkAndResult.text = getString(R.string.record_in) + " " + "$counter"
    }

    private fun checkIfPermissionRequired(intent: Intent): Boolean {
        return when (intent.action) {
            Intent.ACTION_DIAL -> CheckOnlyPerrmission.check(
                this,
                Manifest.permission.READ_CONTACTS
            )
            else -> true
        }
    }

    private fun openSettingsActivity() {
        val intent = Intent(this, AddCustomAppNameActivity::class.java)
        startActivity(intent)
    }

    private fun muteOrUnmute() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            viewModel.requiredOperationIsMuteOrUnmute(audioManager)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestCodes.RECORD_AUDIO) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupTask()
            } else {
                toastLong(R.string.skip_record_audio_permission)
            }
        }
    }

    private fun showSnackBar() {
        Snackbar.make(talkAndResultSnackBarParent, R.string.mic_is_busy, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.retry) {
                kaldiRecognizer.stopRecognizer()
                kaldiRecognizer.startListening()
            }.show()
    }

    private fun setupTask() {
        try {
            val assets = Assets(this)
            val assetDir = assets.syncAssets()
            +viewModel.getKaldiModel(assetDir)
                .subscribeOnIoAndObserveOnMain()
                .subscribe(
                    { model -> kaldiRecognizer.recognizeMicrophone(model) },
                    { error -> printIfDebug(TAG, error.message) }
                )
        } catch (e: IOException) {
            printIfDebug(TAG, e.message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        talkIntent.destroy()
        kaldiRecognizer.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        kaldiRecognizer.onStart()
    }

    override fun onStop() {
        super.onStop()
        kaldiRecognizer.onStop()
    }
}