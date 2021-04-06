package com.e.VoiceAssistant.di.screens

import com.e.VoiceAssistant.ui.activities.AddCustomAppNameActivity
import com.e.VoiceAssistant.di.annotations.ActivityScope
import com.e.VoiceAssistant.di.modules.ViewModelFactoryModule
import com.e.VoiceAssistant.di.modules.ViewModelsModule
import com.e.VoiceAssistant.ui.activities.TalkAndResultsActivityMvi
import com.e.VoiceAssistant.ui.onboarding.OnBoardingActivity
import com.e.VoiceAssistant.ui.splashScreen.LoadDataSplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivitiesBuilders {

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [ViewModelsModule::class,
                   FragmentBuilders::class,
                   ViewModelFactoryModule::class
        ])
    abstract fun contributeAddCustomAppNameActivity(): AddCustomAppNameActivity

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [  ViewModelsModule::class,
                     ViewModelFactoryModule::class
                  ]
    )
    abstract fun contributeTalkAndResultActivityMvi():TalkAndResultsActivityMvi

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [ViewModelFactoryModule::class
        ])
    abstract fun contributeOnBoardingActivity():OnBoardingActivity

    @ActivityScope
    @ContributesAndroidInjector(
        modules = [ViewModelFactoryModule::class
        ])
    abstract fun contributeLoadDataSplashActivity(): LoadDataSplashActivity


}