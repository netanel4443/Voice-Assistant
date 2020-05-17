package com.e.VoiceAssistant.ui.services.SpeechRecognizerService.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.e.VoiceAssistant.R
import com.e.VoiceAssistant.ui.services.SpeechRecognizerService.SpeechRecognizerService


class ForegroundNotification {

     fun notification(context: Context) {
         val intent = Intent(context,
             SpeechRecognizerService::class.java)
             intent.putExtra("stop",true)

         val channelId = context.packageName

         val pIntent = PendingIntent.getService(context,
             System.currentTimeMillis().toInt(),
             intent, PendingIntent.FLAG_CANCEL_CURRENT)

         val remote = RemoteViews(channelId, R.layout.notification_cancel_service)

         val builder = NotificationCompat.Builder(context, context.packageName).apply {
            setSmallIcon(R.drawable.ic_cancel_black_24dp)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setPriority(NotificationCompat.PRIORITY_DEFAULT)
            setCustomContentView(remote)
            remote.setOnClickPendingIntent(R.id.notification_cancel_btn, pIntent)
         }

            builder.setOngoing(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
                                            .apply { description="Tap to close"
                                            setSound(null,null)}
            val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager?.createNotificationChannel(channel)
        }
         (context as Service).startForeground(1,builder.build())
       //  NotificationManagerCompat.from(context).notify(1,builder.build())
    }
}