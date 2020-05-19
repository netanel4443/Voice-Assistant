package com.e.VoiceAssistant.di.screens

import com.e.VoiceAssistant.ui.activities.MainActivity
import com.e.VoiceAssistant.di.annotations.ActivityScope
import com.e.VoiceAssistant.di.viewmodels.OnBoardingViewModelModule
import com.e.VoiceAssistant.di.viewmodels.ViewModelsModule
import com.e.VoiceAssistant.ui.activities.TalkAndResultsActivity
import com.e.VoiceAssistant.ui.onboarding.OnBoardingActivity
import com.e.VoiceAssistant.ui.splashScreen.WelcomeSplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivitiesBuilders {

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [ViewModelsModule::class,
                    FragmentBuilders::class])
    abstract fun contributeMainActivity(): MainActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        OnBoardingViewModelModule::class])
    abstract fun contributeOnBoardingActivity():OnBoardingActivity

    @ActivityScope
    @ContributesAndroidInjector(modules = [
        ViewModelsModule::class])
    abstract fun contributeWelcomeSplashScreen():WelcomeSplashActivity

    @ActivityScope
    @ContributesAndroidInjector
    abstract fun contributeTalkAndResultsActivity(): TalkAndResultsActivity
}