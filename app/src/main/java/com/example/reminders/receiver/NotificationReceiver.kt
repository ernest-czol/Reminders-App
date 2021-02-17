package com.example.reminders.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.reminders.constants.ConstantsAlarm.ID_ALARM
import com.example.reminders.constants.ConstantsNotification.DONE
import com.example.reminders.constants.ConstantsNotification.ID_NOTIFICATION
import com.example.reminders.constants.ConstantsNotification.SNOOZE
import com.example.reminders.constants.ConstantsReminder.DESCRIPTION_REMINDER
import com.example.reminders.constants.ConstantsReminder.TITLE_REMINDER
import com.example.reminders.service.AlarmService

class NotificationReceiver: BroadcastReceiver() {
    private lateinit var alarmService: AlarmService

    override fun onReceive(context: Context, intent: Intent?) {
        alarmService = AlarmService(context)

        val title = intent?.getStringExtra(TITLE_REMINDER)
        val description = intent?.getStringExtra(DESCRIPTION_REMINDER)
        val idAlarm = intent?.getIntExtra(ID_ALARM, 0)
        val notificationId = intent?.getIntExtra(ID_NOTIFICATION, 0)

        NotificationManagerCompat.from(context).cancel(notificationId!!)

        val action = intent.action
        if (action == DONE) {
            Log.d("Not", "done")
        } else if (action == SNOOZE)
        {
           performSnooze(title, description, idAlarm)
        }
    }

    private fun performSnooze(title: String?, description: String?, idAlarm: Int?) {
        val timeInMillis = System.currentTimeMillis() + 120000

        alarmService.updateAlarm(idAlarm!!, timeInMillis, title!!, description!!)
    }
}