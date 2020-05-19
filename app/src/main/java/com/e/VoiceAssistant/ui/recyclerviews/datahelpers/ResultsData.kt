package com.e.VoiceAssistant.ui.recyclerviews.datahelpers

open class ResultsData {}

data class ContactsData(val contact:String, val number:String):ResultsData()
data class PossibleMatches(val match:String ):ResultsData()