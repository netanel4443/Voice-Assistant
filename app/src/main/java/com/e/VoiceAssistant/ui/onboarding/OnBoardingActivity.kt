package com.e.VoiceAssistant.ui.onboarding


import android.content.SharedPreferences
import android.os.Bundle
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.utils.toast
import com.e.VoiceAssistant.utils.toastLong
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.fragment_on_boarding.*
import javax.inject.Inject

class OnBoardingActivity : DaggerAppCompatActivity() {

    @Inject lateinit var prefsEditor:SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_on_boarding)

        onBoardingSkipTview.setOnClickListener {
            if (onBoardingCheckBox.isChecked){
                setToSharedPerf()
                finishAndRemoveTask()
            }
            else
                toastLong(R.string.read_and_skip)
        }

    }

    private fun setToSharedPerf(){
        val editor = prefsEditor
        editor.putBoolean("seen", true)
        editor.apply()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAndRemoveTask()
    }
}
