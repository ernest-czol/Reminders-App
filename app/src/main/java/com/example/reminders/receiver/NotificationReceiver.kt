package com.example.reminders.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.reminders.constants.ConstantsNotification.ID_NOTIFICATION
import com.example.reminders.constants.ConstantsNotification.NOTIFICATION_DONE
import com.example.reminders.constants.ConstantsNotification.NOTIFICATION_SNOOZE
import com.example.reminders.constants.ConstantsReminder
import com.example.reminders.data.Reminder
import com.example.reminders.model.Repository
import com.example.reminders.service.AlarmService
import com.example.reminders.util.TAG

/**
 * Receiver for notifications
 */
class NotificationReceiver : BroadcastReceiver() {
    private lateinit var alarmService: AlarmService

    override fun onReceive(context: Context, intent: Intent?) {
        alarmService = AlarmService(context)

        val notificationId = intent?.getIntExtra(ID_NOTIFICATION, 0)

        NotificationManagerCompat.from(context).cancel(notificationId!!)

        val action = intent.action

        if (action == NOTIFICATION_DONE) {
            //TODO Implementation in the future move to history fragment
            Log.d(TAG, "done")
        } else if (action == NOTIFICATION_SNOOZE) {
            // Get idReminder from intent
            val idReminder = intent.getStringExtra(ConstantsReminder.ID_REMINDER)

            // Get the reminder
            idReminder?.let {
                Repository.getReminderDocument(idReminder).get().addOnSuccessListener {
                    it.toObject(Reminder::class.java)?.let { reminder ->
                        performSnooze(reminder)

                    }
                }
            }
        }
    }

    private fun performSnooze(reminder: Reminder) {
        // TODO get value from shared preferences
        val extraSnoozeTime = 120000

        // Update new time
        reminder.timeInMillis = System.currentTimeMillis() + extraSnoozeTime

        alarmService.updateAlarm(reminder)
    }
}