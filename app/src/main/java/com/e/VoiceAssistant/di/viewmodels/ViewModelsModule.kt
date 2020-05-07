package com.e.VoiceAssistant.di.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.e.VoiceAssistant.di.annotations.ActivityScope
import com.e.VoiceAssistant.di.annotations.AppScope
import com.e.VoiceAssistant.viewmodels.SpeechRecognitionViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelsModule {

    @Binds
    @ActivityScope
    abstract fun bindsViewModelFactoryProvider(factory:ViewModelProviderFactory):ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ActivityScope
    @ViewModelKey(SpeechRecognitionViewModel::class)
    abstract fun bindSpeechRecognitionViewModel(speechRecognitionViewModel: SpeechRecognitionViewModel):ViewModel



}
