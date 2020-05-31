package com.e.VoiceAssistant.ui.services.SpeechRecognizerService

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.presenters.SpeechRecognizerServicePresenter
import com.e.VoiceAssistant.presenters.presentersStates.SpeechRecognizerServicePresenterState
import com.e.VoiceAssistant.ui.activities.AddCustomAppNameActivity
import com.e.VoiceAssistant.ui.activities.TalkAndResultsActivity
import com.e.VoiceAssistant.ui.dialogs.FloatingRepresentOperationsDialog
import com.e.VoiceAssistant.ui.onboarding.OnBoardingActivity
import com.e.VoiceAssistant.ui.services.BaseService
import com.e.VoiceAssistant.ui.services.SpeechRecognizerService.notifications.ForegroundNotification
import com.e.VoiceAssistant.ui.uiHelpers.TouchHelper
import com.e.VoiceAssistant.ui.uiHelpers.touchListener.MultiTouchListener
import com.e.VoiceAssistant.userscollectreddata.AppsDetailsSingleton
import com.e.VoiceAssistant.utils.toast
import kotlinx.android.synthetic.main.floating_trash_screen.view.*
import kotlinx.android.synthetic.main.floating_widget_layout.view.*
import javax.inject.Inject

class SpeechRecognizerService : BaseService(), SpeechRecognizerServicePresenterState {

    @Inject lateinit var presenter: SpeechRecognizerServicePresenter
    @Inject lateinit var sharedPrefs: SharedPreferences
    @Inject lateinit var appsDetailsSingleton: AppsDetailsSingleton
    private lateinit var floatingView: View
    private lateinit var floatingTrashScreen: View
    private lateinit var windowManager: WindowManager
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var darkScreenParams: WindowManager.LayoutParams
    private lateinit var parenOfTrashImage: RelativeLayout
    private lateinit var trash : ImageView
    private lateinit var talkBtn : Button
    private val TAG = "SpeechRecognizerService"

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val shouldStop = intent?.getBooleanExtra("stop", false)

        shouldStop?.let {
            if (shouldStop) { stopSelf() }
        }
        return Service.START_NOT_STICKY
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()

        firstInits()
        initViews()
        initAppAndPackageList()

        talkBtn.setOnTouchListener(MultiTouchListener{ handleTalkBtnTouchEvents(it) })
    }

    private fun handleTalkBtnTouchEvents(touchHelper: TouchHelper) {
        when (touchHelper) {
            is TouchHelper.downEvent->
                presenter.showOrDismissTrash(parenOfTrashImage.visibility)
            is TouchHelper.moveEvent -> {
                params.x = touchHelper.xPos
                params.y = touchHelper.yPos
                windowManager.updateViewLayout(floatingView, params)
            }
            is TouchHelper.upEvent->{
               if (shouldDeleteView())
                   stopSelf()
                presenter.showOrDismissTrash(parenOfTrashImage.visibility)
            }
            is TouchHelper.TalkOrStopClickEvent -> {
                presenter.showOrDismissTrash(parenOfTrashImage.visibility)
                val intent=Intent(this,TalkAndResultsActivity::class.java)
                navigateToDesiredApp(intent)
            }
        }
    }

    private fun shouldDeleteView(): Boolean {
        val xWidth = trash.x + trash.width
        val yWidth=trash.y+trash.height
        val talkBtnHeight=talkBtn.height
        val talkBtnWidth=talkBtn.width

        return   params.y > (trash.y-(talkBtnHeight/2)) &&
                 params.y < (yWidth+(talkBtnHeight/2)) &&
                 params.x > (trash.x-(talkBtnWidth/2)) &&
                 params.x < (xWidth+(talkBtnWidth/2))
    }

    private fun firstInits() {
        presenter.bindView(this)
        shouldShowOnBoarding()
        ForegroundNotification().notification(this)
    }

    private fun shouldShowOnBoarding() {
        val prefs = sharedPrefs.getBoolean("seen", false)
        if (!prefs) {
            val intent = Intent(this, OnBoardingActivity::class.java)

            navigateToDesiredApp(intent)
        }
    }

    private fun initViews() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_widget_layout, null)
        floatingTrashScreen = LayoutInflater.from(this).inflate(R.layout.floating_trash_screen, null)
        parenOfTrashImage = floatingTrashScreen.trashMainParent
        trash = floatingTrashScreen.floatingTrashImage
        talkBtn = floatingView.floatingTalkImg
    }

    private fun closeLoginSplashScreen() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("closeActivityWithBroadcast"))
    }

    private fun initAppAndPackageList() {
        presenter.initWindowManager()
        FloatingRepresentOperationsDialog(this).show()
    }

    override fun initWindowManager(params: WindowManager.LayoutParams,
                                   trashParams: WindowManager.LayoutParams) {
        this.params = params
        darkScreenParams = trashParams
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingTrashScreen, trashParams)
        windowManager.addView(floatingView, params)
    }

    private fun navigateToDesiredApp(intent: Intent){
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast(R.string.no_app_found)
        }
    }

    override fun openSettingsActivity() {
        val intent = Intent(this, AddCustomAppNameActivity::class.java)
        navigateToDesiredApp(intent)
    }

    override fun showOrDismissTrash(visibility: Int) {
        parenOfTrashImage.visibility = visibility
    }

    override fun onDestroy() {
        super.onDestroy()
        val nm=  this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(1)
        presenter.dispose()
        windowManager.removeView(floatingView)
        windowManager.removeView(floatingTrashScreen)
    }
}