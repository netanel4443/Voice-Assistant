package com.e.VoiceAssistant.viewmodels

import android.content.Intent
import androidx.lifecycle.Observer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.usecases.OpenDesiredAppPresenterUseCase
import com.e.VoiceAssistant.usecases.TalkAndResultUseCases
import com.e.VoiceAssistant.viewmodels.effects.TalkAndResultEffects
import com.e.VoiceAssistant.viewmodels.states.TalkAndResultState
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.junit.Assert.assertEquals
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.e.VoiceAssistant.viewmodels.commands.TalkAndResultCommands
import io.reactivex.Observable
import io.reactivex.observers.TestObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.mockito.Mockito.*


@RunWith(AndroidJUnit4::class)
class TalkAndResultViewModelTest{

    @get:Rule
    val initRule:MockitoRule =MockitoJUnit.rule()// has to be public according to the docs

    @get:Rule
    val instantTaskExecutorRule=InstantTaskExecutorRule()

    @Mock
    lateinit var stateObserver: Observer<Pair<TalkAndResultState, TalkAndResultState>>

    @Mock
    lateinit var effectsObserver: Observer<TalkAndResultEffects>

    @Mock
    lateinit var talkAndResultUseCases: TalkAndResultUseCases

    @Mock
    lateinit var openDesiredAppPresenterUseCase: OpenDesiredAppPresenterUseCase

    var commandsObserver=TestObserver<TalkAndResultCommands>()

    private lateinit var viewModel:TalkAndResultViewModel

    @Before
    fun before(){
        viewModel= TalkAndResultViewModel(talkAndResultUseCases, openDesiredAppPresenterUseCase)
        viewModel.viewState.observeForever(stateObserver)
        viewModel.viewEffect.observeForever(effectsObserver)

       RxJavaPlugins.setIoSchedulerHandler{ Schedulers.trampoline()}

        viewModel.command.subscribe(commandsObserver)
    }

    @After
    fun after(){
        viewModel.viewState.removeObserver(stateObserver)
        viewModel.viewEffect.removeObserver(effectsObserver)

    }

    @Test
    fun changeTalkBtnIcon(){
        viewModel.changeTalkBtnIcon()
        val newState=viewModel.viewState.value!!.second
        assertEquals(newState.talkBtnIcon, R.drawable.ic_pause_white_background_24)
    }

    @Test
    fun globalInits(){
        `when`(talkAndResultUseCases.loadKaldiLibrary()).thenReturn(Observable.just(Unit))
        `when`(talkAndResultUseCases.initRecognizerIntent()).thenReturn(Observable.just(Intent()))

        viewModel.globalInits()
        println(commandsObserver.values().size)
        commandsObserver.values().forEach(::println)

    }

}