package com.e.VoiceAssistant.di.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.e.VoiceAssistant.di.annotations.ActivityScope
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class OnBoardingViewModelModule {


    @Binds
    @ActivityScope
    abstract fun bindsViewModelFactoryProvider(factory:ViewModelProviderFactory): ViewModelProvider.Factory

}