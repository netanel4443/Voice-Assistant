package com.e.VoiceAssistant.di

import android.app.Application
import com.e.VoiceAssistant.di.annotations.AppScope
import com.e.VoiceAssistant.di.modules.AppModule
import com.e.VoiceAssistant.di.screens.ActivitiesBuilders
import com.e.VoiceAssistant.di.screens.ServicesBuilder
import com.e.VoiceAssistant.di.viewmodels.ViewModelFactoryModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
@AppScope
@Component(modules = [
    AndroidSupportInjectionModule::class,
    AppModule::class,
    ActivitiesBuilders::class,
    ServicesBuilder::class])
interface AppComponent :AndroidInjector<BaseApplication> {

    @Component.Builder
    interface Builder{

        @BindsInstance
        fun application(application: Application):Builder

        fun build():AppComponent
    }
}