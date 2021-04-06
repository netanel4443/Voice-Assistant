package com.e.VoiceAssistant.usecases

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.e.VoiceAssistant.data.AppsDetails
import com.e.VoiceAssistant.data.SavedAppsDetails
import com.e.VoiceAssistant.data.repo.RealmRepo
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.spy
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule


@RunWith(AndroidJUnit4::class)
class LoadDataUseCaseTest {
    @get:Rule
    val initRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    lateinit var mockedRepo: RealmRepo

    lateinit var loadDataUseCaseTest: LoadDataUseCase

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setup() {
        loadDataUseCaseTest = spy(LoadDataUseCase(mockedRepo))
    }

    @Test
    fun getAppsListFromDB() {
        val tmpMap = HashMap<String, SavedAppsDetails>()
        tmpMap["real name"] = SavedAppsDetails("real name")
        val returnedValue = Single.just(tmpMap)
        `when`(mockedRepo.getAppsList()).thenReturn(returnedValue)
        val expectedReturnedValue =
            loadDataUseCaseTest.getAppsListFromDB().test().values()[0]["real name"]?.realName
        val realName = tmpMap["real name"]?.realName
        assertEquals(expectedReturnedValue, realName)
    }

    @Test
    fun getAppsDetails() {
        val pm = context.packageManager
        val returnedVal = loadDataUseCaseTest.getAppsDetails(pm)

        println(returnedVal.test().values()[0])
    }

    @Test
    fun getCurrentLocale() {
        val returnedVal = loadDataUseCaseTest.getCurrentLocale(context.resources)
        val extractedString = returnedVal.test().values()[0]
        println(extractedString)
        // 1 is US (United states), it will return 1 because the emulator is recognized
        // like his location is in the US
        //TODO: test it with a real device and check if the test passes
        assertEquals(extractedString, "1")
    }



    @Test
    fun addToSavedAppsProperIcon() {
        val savedAppHmap = HashMap<String, SavedAppsDetails>()
        val mockedAppsListInstalledOnDevice = HashMap<String, AppsDetails>()
        mockedAppsListInstalledOnDevice["test 1"] = AppsDetails("test 1")
        savedAppHmap["test 1"] = SavedAppsDetails("test 1")
        savedAppHmap["test 3"] = SavedAppsDetails("test 3")

        `when`(mockedRepo.deleteAppsFromDB(arrayListOf("test 3"))).thenReturn(Completable.fromAction{})

        val returnedVal = loadDataUseCaseTest.addToSavedAppsProperIcon(
            savedAppHmap,
            mockedAppsListInstalledOnDevice
        )
        val returnedValTest=returnedVal.test()
            println(returnedValTest.values())
            println(returnedValTest.values().size)

            returnedValTest.assertNoErrors()
    }

    @Test
    fun someTest(){
       val value= Single.fromCallable{1}
            .flatMap { returnCompletable().toSingleDefault(it) }
            println(value.test().values())
    }

    fun returnCompletable():Completable{
        return Completable.fromAction{
            mockedRepo.deleteAppsFromDB(ArrayList())
        }
    }
}