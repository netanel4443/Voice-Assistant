package com.e.VoiceAssistant.di.modules

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProvider
import com.e.VoiceAssistant.di.annotations.AppScope
import com.e.VoiceAssistant.di.viewmodels.ViewModelProviderFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {

    @Provides @JvmStatic @AppScope
    fun provideApplication(app: Application): Context = app.applicationContext


    @Provides @JvmStatic @AppScope
    fun provideSharedPref(context: Context):SharedPreferences{
        return context.getSharedPreferences("Pref_File", Context.MODE_PRIVATE)
    }

    @Provides @JvmStatic @AppScope
    fun editorSharedPref(prefs:SharedPreferences):SharedPreferences.Editor{
        return prefs.edit()
    }

}