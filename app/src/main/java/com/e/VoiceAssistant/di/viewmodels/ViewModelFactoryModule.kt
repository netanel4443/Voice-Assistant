package com.e.VoiceAssistant.di.viewmodels

import androidx.lifecycle.ViewModelProvider
import com.e.VoiceAssistant.di.annotations.AppScope
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {

    @Binds
    @AppScope
    abstract fun bindsViewModelFactoryProvider(factory:ViewModelProviderFactory): ViewModelProvider.Factory


}