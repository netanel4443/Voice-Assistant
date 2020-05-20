package com.e.VoiceAssistant.di.modules

import androidx.lifecycle.ViewModelProvider
import com.e.VoiceAssistant.di.annotations.ActivityScope
import com.e.VoiceAssistant.di.viewmodels.ViewModelProviderFactory
import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {
    @Binds
    @ActivityScope
    abstract fun bindsViewModelFactoryProvider(factory: ViewModelProviderFactory): ViewModelProvider.Factory

}