package com.example.reminders.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.reminders.R
import com.example.reminders.constants.ConstantsAlarm.ACTION_SET_MAIN_ALARM
import com.example.reminders.constants.ConstantsAlarm.ACTION_SIMPLE_ALARM
import com.example.reminders.constants.ConstantsAlarm.DESCRIPTION_ALARM
import com.example.reminders.constants.ConstantsAlarm.TITLE_ALARM
import com.example.reminders.constants.ConstantsNotification.ID_NOTIFICATION
import com.example.reminders.constants.ConstantsNotification.NOTIFICATION_CHANNEL_ID
import com.example.reminders.constants.ConstantsNotification.NOTIFICATION_DONE
import com.example.reminders.constants.ConstantsNotification.NOTIFICATION_SNOOZE
import com.example.reminders.constants.ConstantsNotification.PRE_ALARM_TITLE_NOTIFICATION
import com.example.reminders.constants.ConstantsReminder.ID_REMINDER
import com.example.reminders.constants.ConstantsRequestCode.REQUEST_CODE_DONE
import com.example.reminders.constants.ConstantsRequestCode.REQUEST_CODE_SNOOZE
import com.example.reminders.data.Reminder
import com.example.reminders.model.Repository
import com.example.reminders.service.AlarmService
import com.example.reminders.util.IntervalUnit
import com.example.reminders.util.RandomUtil.getRandomInt
import io.karn.notify.internal.utils.Action
import java.util.*


/**
 * Receiver for alarms
 */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Check if it is a pre-alarm or a main alarm (it helps to see if it is repetitive)

        when (intent.action) {
            ACTION_SET_MAIN_ALARM -> {
                // Get idReminder from intent
                val idReminder = intent.getStringExtra(ID_REMINDER)

                // Get the reminder
                idReminder?.let {
                    Repository.getReminderDocument(idReminder).get().addOnSuccessListener {
                        it.toObject(Reminder::class.java)?.let { reminder ->
                            // Send notification
                            sendNotification(
                                context,
                                reminder.title,
                                reminder.notes,
                                idReminder
                            )

                            // If reminder is repetitive -> update it for a new alarm
                            if (reminder.repeatingDetails.isRepeating)
                                setRepetitiveAlarm(
                                    AlarmService(context),
                                    reminder.repeatingDetails.intervalUnit,
                                    reminder.repeatingDetails.interval
                                )
                        }
                    }
                }
            }

            ACTION_SIMPLE_ALARM -> {
                // Get necessary fields from intent
                val title = intent.getStringExtra(TITLE_ALARM)
                val description = intent.getStringExtra(DESCRIPTION_ALARM)

                // Send notification
                sendNotification(context, title, description)
            }
        }
    }
}

// TODO
private fun setRepetitiveAlarm(alarmService: AlarmService, timeUnit: IntervalUnit, timeValue: Long) {
    val cal = Calendar.getInstance().apply {
        // this.timeInMillis = timeInMillis + timeUnit.toMillis(timeValue)
        //Timber.d("Set alarm for next week same time - ${convertDate(this.timeInMillis)}")
    }
    //alarmService.setRepetitiveAlarm(cal.timeInMillis)
}

/**
 * Send a notification
 */
private fun sendNotification(
    context: Context,
    textTitle: String?,
    textContent: String?,
    idReminder: String? = null
) {
    // Create Snooze intent
    val snoozeIntent = createSnoozeIntent(context, idReminder)

    // Pending intent for snooze
    val pendingIntentSnooze = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE_SNOOZE,
        snoozeIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    // Create Done intent
    val doneIntent = Intent(context, NotificationReceiver::class.java)
    doneIntent.action = NOTIFICATION_DONE

    // Pending intent for done
    val pendingIntentDone = PendingIntent.getBroadcast(
        context,
        REQUEST_CODE_DONE,
        doneIntent,
        PendingIntent.FLAG_UPDATE_CURRENT
    )

    // build notification
    val builder =
        buildNotification(context, textTitle, textContent, pendingIntentSnooze, pendingIntentDone)

    with(NotificationManagerCompat.from(context)) {
        // notificationId is a unique int for each notification that you must define
        val notificationId = getRandomInt()
        snoozeIntent.putExtra(ID_NOTIFICATION, notificationId)

        notify(notificationId, builder.build())
    }
}

/**
 * Build the notification
 */
private fun buildNotification(
    context: Context, textTitle: String?, textContent: String?,
    pendingIntentSnooze: PendingIntent, pendingIntentDone: PendingIntent
): NotificationCompat.Builder {
    val builder = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle(textTitle)
        .setContentText(textContent)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    // If it is only a pre-alarm there is no need for snooze or done (for the moment)
    if (textTitle != PRE_ALARM_TITLE_NOTIFICATION) {
        builder.addAction(Action(R.drawable.ic_app_icon, NOTIFICATION_DONE, pendingIntentDone))
        builder.addAction(Action(R.drawable.ic_app_icon, NOTIFICATION_SNOOZE, pendingIntentSnooze))
    }

    return builder
}

/**
 * Create a snooze intent
 */
private fun createSnoozeIntent(
    context: Context,
    idReminder: String?
): Intent {
    val snoozeIntent = Intent(context, NotificationReceiver::class.java)
    snoozeIntent.action = NOTIFICATION_SNOOZE
    snoozeIntent.putExtra(ID_REMINDER, idReminder)

    return snoozeIntent
}
