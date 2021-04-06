package com.e.VoiceAssistant.ui.activities

import android.content.Context
import android.widget.Button
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.toPackage
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.helpers.GetDeviceAppList
import com.e.VoiceAssistant.ui.recyclerviews.viewholders.GenericViewHolder
import com.e.VoiceAssistant.ui.recyclerviews.viewholders.PossibleMatchesViewHolder
import com.e.VoiceAssistant.utils.printIfDebug
import junit.framework.Assert.assertEquals
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TalkAndResultsActivityMviTest{

    @get:Rule
    var activityrule = ActivityScenarioRule(TalkAndResultsActivityMvi::class.java)

    @get:Rule
    val instantTaskExecutorRule= InstantTaskExecutorRule()

    var activity:TalkAndResultsActivityMvi?=null


    companion object {
         var context= ApplicationProvider.getApplicationContext<Context>()
         var deviceAppList=HashMap<String, AppsDetails>()
        @BeforeClass
        @JvmStatic fun beforeClass(){
           val pm = context.packageManager
            deviceAppList =
                GetDeviceAppList().getAppsDetails(pm)
                    .test()
                    .values()[0]
        }
    }

    @Before fun setup(){
      activityrule.scenario.onActivity { activity=it  }

    }

    @Test fun startSettingsActivity() {
        Intents.init()
        onView(withId(R.id.settings)).perform(click())
        intended(hasComponent(AddCustomAppNameActivity::class.java.name))
    }


    @Test fun showFloatingHelpDialog(){
        onView(withId(R.id.floatingHelp)).perform(click())
        onView(withText(R.string.how_to_use))
            .inRoot(withDecorView(not(`is`(activity!!.window.decorView))))
            .check(matches(isDisplayed()))
    }

    @Test fun clickToTalkOrStopBtn(){
        onView(withId(R.id.clickToTalkOrStopBtn)).perform(click())

        val view=activity!!.findViewById(R.id.clickToTalkOrStopBtn) as Button
        val background=view.background.current
        val expected= activity!!.getDrawable(R.drawable.ic_mic_white_background_24)
        assertEquals(background, expected)
    }

    @Test fun test_operation_words_recycler_view(){
        val operationsArray=activity!!.resources.getStringArray(R.array.operations_keywords)
            onView(withId(R.id.operationsWordsRecyclerView)).perform(
                RecyclerViewActions.scrollTo<GenericViewHolder<String>>(
                    hasDescendant(withText(operationsArray[operationsArray.size-1]))
                )
            )


//            onView(withId(R.id.operationsWordsRecyclerView)).perform(
//                RecyclerViewActions.scrollToPosition<GenericViewHolder<String>>(
//                   operationsArray.size+2
//                )
//            )
    }

    @Test fun test_callToContact(){
        Intents.init()

        activity!!.viewModel.checkIfSecondListenRequired(arrayListOf("call test contact"),
            activity!!.appsDetailsSingleton.appsAndStoredAppsDetails,
            hashMapOf(Pair("test contact","123"))
        )
        println(  activity!!.appsDetailsSingleton.appsAndStoredAppsDetails)

        intending(toPackage("com.android.phone"))

    }

  @Test fun test_navigateToSomeApp_fromDevicesApps(){
        Intents.init()
        activity!!.viewModel.checkIfSecondListenRequired(
            arrayListOf("open camera"), deviceAppList, hashMapOf(Pair("test contact","123"))
        )
        intending(toPackage("com.android.camera2"))
    }

    @Test fun test_openSpotify(){
        Intents.init()
        activity!!.viewModel.checkIfSecondListenRequired(
            arrayListOf("spotify akon"), deviceAppList, hashMapOf(Pair("test contact","123"))
        )
        onView(withId(R.id.possibleResultsRecyclerView))
            .check(matches(hasDescendant(withText("akon"))))

        onView(withId(R.id.possibleResultsRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition<PossibleMatchesViewHolder>(0, click()))
        println(Intents.getIntents())

        if(deviceAppList.containsKey("spotify")){
            intended(toPackage(deviceAppList["spotify"]!!.activity))
        }
        else{
            intended(toPackage(deviceAppList["chrome"]!!.activity))
        }

    }
    @Test fun test_openYoutube(){
        Intents.init()
        activity!!.viewModel.checkIfSecondListenRequired(
            arrayListOf("youtube akon"), deviceAppList, hashMapOf(Pair("test contact","123"))
        )
        onView(withId(R.id.possibleResultsRecyclerView))
                .check(matches(hasDescendant(withText("akon"))))

        onView(withId(R.id.possibleResultsRecyclerView))
            .perform(RecyclerViewActions.actionOnItemAtPosition<PossibleMatchesViewHolder>(0, click()))

        println(Intents.getIntents())


        if(deviceAppList.containsKey("youtube")){
            intended(toPackage("com.google.android.youtube"))
        }

//        else{
//            // need to check where it navigates
//        }

    }

    @Test fun test_sendSms(){
        Intents.init()
        activity!!.run {

            viewModel.checkIfSecondListenRequired(
                arrayListOf("text hello you"), deviceAppList, hashMapOf(Pair("hello you","123"))
            )

            onView(withId(R.id.possibleResultsRecyclerView)).check(matches(hasDescendant(withText("hello you"))))

            viewModel.checkIfSecondListenRequired(
            arrayListOf("test message"), deviceAppList, hashMapOf(Pair("hello you","123"))
            )
            intended(toPackage(deviceAppList["messages"]!!.activity))

        }
    }

    @Test fun test_searchInWeb(){
        Intents.init()
        activity!!.run{
            viewModel.checkIfSecondListenRequired(
                arrayListOf("search banana"), deviceAppList,hashMapOf(Pair("hello you","123"))
            )
            onView(withId(R.id.possibleResultsRecyclerView))
                .check(matches(hasDescendant(withText("banana"))))

            onView(withId(R.id.possibleResultsRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition<PossibleMatchesViewHolder>(0, click()))
            println(Intents.getIntents())

            intended(toPackage(deviceAppList["google"]!!.activity))
        }
    }

}