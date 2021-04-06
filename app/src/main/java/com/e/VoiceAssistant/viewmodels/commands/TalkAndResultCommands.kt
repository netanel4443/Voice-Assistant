package com.e.VoiceAssistant.viewmodels.commands

import android.content.Intent

sealed class TalkAndResultCommands {
   data class SetIntent(val intent: Intent):TalkAndResultCommands()
   object InitSpeechRecognizer :TalkAndResultCommands()
   object StartKaldiRecognizer: TalkAndResultCommands()
   object MuteOrUnMute: TalkAndResultCommands()

}