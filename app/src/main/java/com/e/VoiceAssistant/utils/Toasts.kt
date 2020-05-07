package com.e.VoiceAssistant.utils

import android.content.Context
import android.widget.Toast

fun Context.toast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun  Context.toast(message: Int) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.toastLong(message: String)=
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun Context.toastLong(message: Int)=
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
