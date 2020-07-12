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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.permissions.CheckOnlyPerrmission
import com.e.VoiceAssistant.permissions.RequestCodes
import com.e.VoiceAssistant.permissions.RequestGlobalPermission
import com.e.VoiceAssistant.permissions.StartActivityToCheckPermission
import com.e.VoiceAssistant.presenters.TalkAndResultsPresenter
import com.e.VoiceAssistant.presenters.presentersStates.TalkAndResultsPresenterView
import com.e.VoiceAssistant.sensors.CommandsForSpeechListenerService
import com.e.VoiceAssistant.sensors.HandleSpeechRecognition
import com.e.VoiceAssistant.ui.dialogs.FloatingRepresentOperationsDialog
import com.e.VoiceAssistant.ui.recyclerviews.datahelpers.ResultsData
import com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters.OperationsKeyWordsAdapter
import com.e.VoiceAssistant.ui.recyclerviews.recyclerviewsadapters.PossibleResultsRecyclerViewAdapter
import com.e.VoiceAssistant.ui.uihelpers.KaldiRecognizer
import com.e.VoiceAssistant.userscollecteddata.AppsDetailsSingleton
import com.e.VoiceAssistant.utils.printIfDebug
import com.e.VoiceAssistant.utils.rxJavaUtils.subscribeOnIoAndObserveOnMain
import com.e.VoiceAssistant.utils.rxJavaUtils.throttle
import com.e.VoiceAssistant.utils.toast
import com.e.VoiceAssistant.utils.toastLong
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

class TalkAndResultsActivity : BaseAdsActivity() , TalkAndResultsPresenterView {

    @Inject lateinit var presenter: TalkAndResultsPresenter
    @Inject lateinit var appsDetailsSingleton: AppsDetailsSingleton
    private lateinit var talkIntent: SpeechRecognizer
    private lateinit var intnt: Intent
    private var appsDetailsHmap=HashMap<String, AppsDetails>()
    private val TAG = "TalkAndResultsActivity"
    private  lateinit var adapter: PossibleResultsRecyclerViewAdapter
    private lateinit var kaldiRecognizer:KaldiRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_talk_and_results)

        uiInits()
        generalNonUIinits()

        +clickToTalkOrStopBtn.clicks().throttle()
            .map { RequestGlobalPermission.check(
                this,Manifest.permission.RECORD_AUDIO,RequestCodes.RECORD_AUDIO
                   )}
            .filter {granted-> granted }
            .subscribe {
            presenter.handleTalkOrStopClick()
        }

        +settings.clicks().throttle().subscribe {
            val intent = Intent(this, AddCustomAppNameActivity::class.java)
            startActivity(intent)
        }

        +floatingHelp.clicks().throttle().subscribe {
            FloatingRepresentOperationsDialog(this).show()
        }
    }

    private fun uiInits() {
        presenter.bindView(this)
        appsDetailsHmap=appsDetailsSingleton.appsAndStoredAppsDetails
        initRecyclerView()
        initOperationsRecyclerView()
        loadAd(adContainer,talkAndResultActivityUnitId)
    }

    private fun initOperationsRecyclerView() {
        val operationAdapter= OperationsKeyWordsAdapter()
        operationsWordsRecyclerView.adapter=operationAdapter
        operationsWordsRecyclerView.layoutManager=LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        operationsWordsRecyclerView.setHasFixedSize(true)
        operationAdapter.attachData(resources.getStringArray(R.array.operations_keywords).toHashSet())
    }

    private fun initRecyclerView() {
        adapter= PossibleResultsRecyclerViewAdapter()
        adapter.itemClick={presenter.changeSelectedResult(it)}
        possibleResultsRecyclerView.adapter=adapter
        possibleResultsRecyclerView.layoutManager=LinearLayoutManager(this,RecyclerView.VERTICAL,false)
        possibleResultsRecyclerView.setHasFixedSize(true)
    }

    private fun generalNonUIinits() {
        initKaldiRecognizer()
        presenter.globalInits()
    }

    private fun initKaldiRecognizer(){
        kaldiRecognizer=KaldiRecognizer(){kaldiAction->
            when(kaldiAction){
                KaldiRecognizer.KaldiActions.ERROR-> showSnackBar()
                KaldiRecognizer.KaldiActions.DETECTED-> presenter.handleTalkOrStopClick()
            }
        }
    }

   override fun startKaldiRecognizer(){
        val permissionCheck = ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), RequestCodes.RECORD_AUDIO)
        }
        else {
            setupTask()
        }
    }

    override fun setIntent(intent: Intent) {
        intnt = intent
    }

    override fun initSpeechRecognizer() {
        talkIntent = SpeechRecognizer.createSpeechRecognizer(this)
        talkIntent.setRecognitionListener(
            HandleSpeechRecognition(object : CommandsForSpeechListenerService {
                override fun onError(message: String) {
                    toast(message)
                }

                override fun changeTalkIcon() {
                    presenter.checkIfTalkBtnIconChanged()
                }

                override fun checkForResults(matches: ArrayList<String>) {
                    presenter.checkIfSecondListenRequired(matches, appsDetailsHmap, appsDetailsSingleton.contactList)
                }
            })
        )
    }

    override fun handleClick(icon: Int) {
        if (StartActivityToCheckPermission.check(this, Manifest.permission.RECORD_AUDIO)) {

            if (R.drawable.ic_mic_white_background_24 == icon) {
                kaldiRecognizer.stopRecognizer()

                talkIntent.startListening(intnt)
                presenter.changeTalkBtnIcon()
            } else {
                talkIntent.stopListening()

                /*timer added to cancel because the api sometimes stuck at "Busy state" and we can't
                *perform a new operation. 800 millis because we can click every 1 sec so the range need to be 0-1s
                * 800 millis delay works fine.*/
                +Observable.timer(800,TimeUnit.MILLISECONDS)
                    .subscribeOnIoAndObserveOnMain()
                    .subscribe{
                          talkIntent.cancel()
                          kaldiRecognizer.startListening()
                          kaldiRecognizer.once=true
                    }
                presenter.changeTalkBtnIcon()
            }
        }
    }

    override fun changeTalkIcon(icon: Int) {
        clickToTalkOrStopBtn.setBackgroundResource(icon)
    }

    override fun navigateToDesiredApp(intent: Intent, results: HashSet<ResultsData>,dataType: Int) {
        printIfDebug(TAG, intent.data.toString())
        if (checkIfPermissionRequired(intent))
        {
            try {
                startActivity(intent)
                adapter.attachData(results,dataType,intent)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            }
        }
        else {
            toastLong(R.string.permission_required)
        }
    }

    override fun navigateToDesiredApp(intent: Intent) {
        if (checkIfPermissionRequired(intent)) {
            try {
                startActivity(intent)
            }
            catch (e: ActivityNotFoundException) {
                toast(R.string.no_app_found)
            }
        }
        else {
            toastLong(R.string.permission_required)
        }
    }

    override fun showResults(results: HashSet<ResultsData>, dataType: Int) {
        adapter.attachData(results,dataType)
    }

    override fun secondListenToUser(contacts:HashSet<ResultsData>, dataType: Int, intent: Intent) {
        /** timer is needed because of throttle click(rxBinding) .
        After a user clicks on the [talk] button when he wants to pause the speech recognition*/
        adapter.attachData(contacts,0,intent)

        +presenter.countDownTimerAnimation()
            .subscribe({
                counterTalkAndResultParent.visibility= View.GONE
                presenter.handleTalkOrStopClick()
            }, {})
    }

    override  fun timerAnimation(counter:Int){
        counterTalkAndResultParent.visibility= View.VISIBLE
        val fadeOut=   AnimationUtils.loadAnimation(this, R.anim.fade_out)
        counterTalkAndResult.startAnimation(fadeOut)
        counterTalkAndResult.text=getString(R.string.record_in) +" " + "$counter"
    }

    private fun checkIfPermissionRequired(intent: Intent):Boolean {
        return when(intent.action){
            Intent.ACTION_DIAL-> CheckOnlyPerrmission.check(this, Manifest.permission.READ_CONTACTS)
            else ->true
        }
    }

    override fun openSettingsActivity() {
        val intent = Intent(this, AddCustomAppNameActivity::class.java)
        startActivity(intent)
    }

    override fun muteOrUnmute() {
        val audioManager=getSystemService(Context.AUDIO_SERVICE) as AudioManager
        presenter.requiredOperationIsMuteOrUnmute(audioManager)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RequestCodes.RECORD_AUDIO) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupTask()
            } else {
              toastLong(R.string.skip_record_audio_permission)
            }
        }
    }

    private fun showSnackBar(){
        Snackbar.make(talkAndResultSnackBarParent,R.string.mic_is_busy,Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.retry) {
               kaldiRecognizer.stopRecognizer()
               kaldiRecognizer.startListening()
            }.show()
    }

    private  fun setupTask(){
       try {
           val assets = Assets(this)
           val assetDir = assets.syncAssets()
           +presenter.getKaldiModel(assetDir)
               .subscribeOnIoAndObserveOnMain()
               .subscribe(
                   {model-> kaldiRecognizer.recognizeMicrophone(model) },
                   {error-> printIfDebug(TAG,error.message) }
               )
        }catch (e:IOException){ printIfDebug(TAG,e.message)}
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.dispose()
        talkIntent.destroy()
        compositeDisposable.clear()
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