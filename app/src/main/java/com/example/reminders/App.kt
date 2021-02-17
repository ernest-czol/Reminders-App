package com.example.reminders

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.example.reminders.constants.ConstantsNotification.NOTIFICATION_CHANNEL_DESCRIPTION
import com.example.reminders.constants.ConstantsNotification.NOTIFICATION_CHANNEL_ID
import com.example.reminders.constants.ConstantsNotification.NOTIFICATION_CHANNEL_NAME

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        createNotificationChannelReminders()
    }

    private fun createNotificationChannelReminders() {
        val name = NOTIFICATION_CHANNEL_NAME
        val descriptionText = NOTIFICATION_CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}