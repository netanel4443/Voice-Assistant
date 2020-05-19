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
import com.e.VoiceAssistant.ui.activities.MainActivity
import com.e.VoiceAssistant.ui.activities.TalkAndResultsActivity
import com.e.VoiceAssistant.ui.dialogs.FloatingRepresentOperationsDialog
import com.e.VoiceAssistant.ui.onboarding.OnBoardingActivity
import com.e.VoiceAssistant.ui.services.BaseService
import com.e.VoiceAssistant.ui.services.SpeechRecognizerService.notifications.ForegroundNotification
import com.e.VoiceAssistant.ui.splashScreen.LoadingSplashScreen
import com.e.VoiceAssistant.ui.uiHelpers.TouchHelper
import com.e.VoiceAssistant.ui.uiHelpers.touchListener.MultiTouchListener
import com.e.VoiceAssistant.userscollectreddata.AppsDetailsSingleton
import com.e.VoiceAssistant.utils.rxJavaUtils.throttle
import com.e.VoiceAssistant.utils.toast
import com.jakewharton.rxbinding.view.RxView
import kotlinx.android.synthetic.main.floating_dark_screen.view.*
import kotlinx.android.synthetic.main.floating_widget_layout.view.*
import javax.inject.Inject

class SpeechRecognizerService : BaseService(), SpeechRecognizerServicePresenterState {

    @Inject lateinit var presenter: SpeechRecognizerServicePresenter
    @Inject lateinit var sharedPrefs: SharedPreferences
    @Inject lateinit var appsDetailsSingleton: AppsDetailsSingleton
    private lateinit var floatingView: View
    private lateinit var floatingDarkScreen: View
    private lateinit var windowManager: WindowManager
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var darkScreenParams: WindowManager.LayoutParams
    private lateinit var parenOfDarkImage: RelativeLayout
    private lateinit var settings: Button
    private lateinit var closeIcon: Button
    private lateinit var help: Button
    private lateinit var floatingRepresentOperationsDialog: FloatingRepresentOperationsDialog
    private lateinit var darkImage : ImageView
    private lateinit var menu : Button
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

        +RxView.clicks(settings).throttle().subscribe {
            presenter.openSettingsActivity()
        }

        +RxView.clicks(menu).throttle().subscribe {
            presenter.handleMenuClick(settings.visibility)
        }

        +RxView.clicks(help).throttle().subscribe {
           floatingRepresentOperationsDialog.show()
        }

        +RxView.clicks(darkImage).subscribe {
            presenter.handleMenuClick(settings.visibility)
        }

        closeIcon.setOnClickListener { stopSelf() }

        talkBtn.setOnTouchListener(MultiTouchListener{ handleTalkBtnTouchEvents(it) })
    }

    private fun handleTalkBtnTouchEvents(touchHelper: TouchHelper) {
        when (touchHelper) {
            is TouchHelper.moveEvent -> {
                params.x = touchHelper.xPos
                params.y = touchHelper.yPos
                windowManager.updateViewLayout(floatingView, params)
            }
            is TouchHelper.TalkOrStopClickEvent -> {
                val intent=Intent(this,TalkAndResultsActivity::class.java)
                navigateToDesiredApp(intent)
            }
        }
    }

    private fun firstInits() {
        presenter.bindView(this)
        floatingRepresentOperationsDialog = FloatingRepresentOperationsDialog(this)
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
        floatingDarkScreen = LayoutInflater.from(this).inflate(R.layout.floating_dark_screen, null)
        closeIcon = floatingView.closeServiceBtn
        settings = floatingView.floatingSettingsButton
        help = floatingView.floatingHelp
        parenOfDarkImage = floatingDarkScreen.trashMainParent
        darkImage = floatingDarkScreen.floatingDarkImage
        menu = floatingView.floatingMenu
        talkBtn = floatingView.floatingTalkImg
    }

    private fun startLoginSplashScreen() {
        val intent = Intent(this, LoadingSplashScreen::class.java)
        navigateToDesiredApp(intent)
    }

    private fun closeLoginSplashScreen() {
        LocalBroadcastManager.getInstance(this).sendBroadcast(Intent("closeActivityWithBroadcast"))
    }

    private fun initAppAndPackageList() {
                presenter.initWindowManager()
                floatingRepresentOperationsDialog.show()
    }

    override fun initWindowManager(params: WindowManager.LayoutParams,
                                   trashParams: WindowManager.LayoutParams) {
        this.params = params
        darkScreenParams = trashParams
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.addView(floatingDarkScreen, trashParams)
        windowManager.addView(floatingView, params)
    }

    fun navigateToDesiredApp(intent: Intent){
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast(R.string.no_app_found)
        }
    }
    override fun openSettingsActivity() {
        val intent = Intent(this, MainActivity::class.java)
        navigateToDesiredApp(intent)
    }

    override fun handleMenuClick(visibility: Int) {
        settings.visibility = visibility
        closeIcon.visibility = visibility
        help.visibility = visibility
        parenOfDarkImage.visibility = visibility
    }

    override fun onDestroy() {
        super.onDestroy()
        val nm=  this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(1)
        presenter.dispose()
        windowManager.removeView(floatingView)
        windowManager.removeView(floatingDarkScreen)
    }
}